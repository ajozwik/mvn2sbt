package pl.jozwik.mvn2sbt

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.maven.Plugin

import scala.util.{Success, Failure, Try}

case class SbtProjectContent(project: Project, path: String, libraries: Set[Dependency], dependsOn: Set[Dependency], information: ProjectInformation, settings: Set[String])

object SbtContent {
  val SCALA_VERSION_IN_GLOBAL = "scala.version"

  val PROHIBITED_CHARS = "."

  private[mvn2sbt] def resolversToOption(resolvers: Set[String]) = {

    val opt = if (resolvers.isEmpty) {
      None
    } else {
      val resolversString = resolvers.map { x =>
        s""" "$x" at "$x" """
      }.mkString(",",
          """,
          """.stripMargin, "")

      Some(resolversString)
    }
    s"""resolvers in Global ++= Seq(Resolver.mavenLocal${opt.getOrElse("")}""" + ")\n\n"
  }

  private[mvn2sbt] def changeNotSupportedSymbols(text: String) = {
    text.replaceAll(s"[$PROHIBITED_CHARS]", "_")
  }

}

case class SbtContent(private val projects: Seq[Project], private val hierarchy: Map[MavenDependency, ProjectInformation], private val rootDir: File) extends LazyLogging {

  import pl.jozwik.mvn2sbt.PluginConverter._
  import pl.jozwik.mvn2sbt.SbtContent._


  def buildSbtContentPluginContentAsString: (String, String) = {
    val buildSbtWriter = new StringBuilder
    val pluginsSbtWriter = new StringBuilder
    val defaultScalaVersion = System.getProperty(SCALA_VERSION_IN_GLOBAL, "2.11.2")
    buildSbtWriter.append( s"""scalaVersion in Global := "$defaultScalaVersion" """).append("\n\n")
    buildSbtWriter.append("def ProjectName(name: String,path:String): Project =  Project(name, file(path))").append("\n\n")
    val (contentOfPluginSbt, resolvers, buildSbtProjects) = projects.foldLeft((Set.empty[String], Set.empty[String], Map.empty[MavenDependency, SbtProjectContent])) {
      case ((accContent, accResolvers, stbProjectContent), p) =>
        val (projectContent, pluginsSbt, resolvers) = handleProject(p)
        (accContent ++ pluginsSbt, accResolvers ++ resolvers, stbProjectContent + (p.projectDependency -> projectContent))
    }


    buildSbtWriter.append(resolversToOption(resolvers))
    val optimizedMaps = optimizeProjects(buildSbtProjects)
    val (ps, libraries) = projectsToString(optimizedMaps)
    val lib = libraries.toSeq.sortBy { case (d, _) => d.groupId}
    lib.foreach {
      case (_, (d, variable)) =>
        val dependency = s""""${d.groupId}" % "${d.artifactId}" % "${d.versionId}""""
        val txt = s"""val $variable = $dependency"""
        buildSbtWriter.append(txt).append("\n\n")
    }

    ps.foreach(buildSbtWriter.append)

    pluginsSbtWriter.append(contentOfPluginSbt.mkString("\n\n"))
    (buildSbtWriter.toString(), pluginsSbtWriter.toString())
  }


  private def removeDuplicatedDependsOn(project: Project, dependsOn: Set[Dependency], sbtProjectsMap: Map[MavenDependency, SbtProjectContent]): Set[Dependency] = {
    val toRemove = dependsOn.flatMap {
      dependOn =>
        val setWithoutDep = dependsOn - dependOn

        dependOn.scope match {
          case Scope.test =>
            None
          case _ =>
            val projectContent = sbtProjectsMap(dependOn.mavenDependency)
            projectContent.dependsOn.find(d => setWithoutDep.contains(d) && d.scope != Scope.test)
        }
    }
    if (toRemove.isEmpty) {
      dependsOn
    } else {
      logger.debug(s"${project.projectDependency}: Remove $toRemove")
      removeDuplicatedDependsOn(project, dependsOn.diff(toRemove), sbtProjectsMap)
    }
  }


  private def optimizeProject(sbtProjectsMap: Map[MavenDependency, SbtProjectContent], content: SbtProjectContent): SbtProjectContent = {
    val optimizedLibraries = removeDuplicatedLibraries(sbtProjectsMap, content)
    val optimizedDependsOn = removeDuplicatedDependsOn(content.project, content.dependsOn, sbtProjectsMap)
    content.copy(libraries = optimizedLibraries, dependsOn = optimizedDependsOn)
  }

  private def removeDuplicatedLibraries(map: Map[MavenDependency, SbtProjectContent], content: SbtProjectContent): Set[Dependency] = {
    val parentDependencies = content.dependsOn.flatMap { dep =>
      if (dep.scope == Scope.compile) {
        map(dep.mavenDependency).libraries
      } else {
        Nil
      }
    }

    val parentDependenciesSet = parentDependencies.toSet
    val optimizedLibraries = content.libraries.filterNot {
      dep =>
        parentDependenciesSet.contains(dep) && dep.scope == Scope.compile && content.libraries.find(p => p.mavenDependency == dep.mavenDependency && p.scope != Scope.compile).isEmpty
    }
    optimizedLibraries
  }

  private def optimizeProjects(sbtProjectsMap: Map[MavenDependency, SbtProjectContent]): Map[MavenDependency, SbtProjectContent] = {
    val optimizedMap = sbtProjectsMap.map {
      case (name, content) =>
        val optimized = optimizeProject(sbtProjectsMap, content)
        if (optimized != content) {
          (name, optimized)
        } else {
          (name, content)
        }
    }
    if (optimizedMap == sbtProjectsMap) {
      optimizedMap
    } else {
      optimizeProjects(optimizedMap)
    }
  }


  private def projectsToString(projectsMap: Map[MavenDependency, SbtProjectContent]) = {
    val sorted = projectsMap.toSeq.sortBy { case (d, _) => d.artifactId}
    sorted.foldLeft((Seq.empty[String], Map.empty[GroupArtifact, (MavenDependency, String)])) {
      case ((accSeqString, accMap), (_, content)) =>
        val (project, newMap) = createBuildSbt(content, accMap)
        (project +: accSeqString, newMap)
    }
  }


  private def handleProject(p: Project): (SbtProjectContent, Set[String], Set[String]) = {

    val information = hierarchy(p.projectDependency)
    val relativePath = toRelativePath(information.projectPath, rootDir)

    val (dependsOn, libraries) = splitToDependsOnLibraries(p, information)

    val (settings, plugins, pluginDependencies) = handlePlugins(information.projectPath, information.plugins)


    val (pluginsDependsOnDependenciesSettings, pluginsDependsOnDependenciesSet) =
      DependencyToPluginConverter.addPluginForDependency(rootDir, information.projectPath, libraries)

    (SbtProjectContent(p, relativePath, pluginDependencies ++ libraries, dependsOn, information, pluginsDependsOnDependenciesSettings ++ settings), pluginsDependsOnDependenciesSet ++ plugins, information.resolvers)
  }


  private def splitToDependsOnLibraries(p: Project, information: ProjectInformation) = p.dependencies.partition {
    d =>
      val m = d.mavenDependency
      val contains = hierarchy.contains(m)
      val parentOption = hierarchy(p.projectDependency).parent
      val parentMatch = parentOption.fold(false) {
        parent =>
          parent == m
      }
      contains || parentMatch
  }

  private def handlePlugins(file: File, plugins: Seq[(PluginDescription, Plugin)]): (Set[String], Set[String], Set[Dependency]) = {
    plugins.foldLeft((Set[String](), Set.empty[String], Set[Dependency]())) { (tuple, p) =>
      val (accSett, accPlug, accPluginDependencies) = tuple
      val (pluginDescription, plugin) = p
      val endSettings = DependencyToPluginConverter.addSeqToArray(file.equals(rootDir))
      val settings = pluginDescription.sbtSetting.fold(accSett)(x => accSett + (x + endSettings))
      val customPluginSettings = pluginDescription.pomConfigurationToSbtConfiguration(rootDir, plugin)
      val plugins = accPlug +(pluginDescription.pluginsSbtPluginConfiguration, pluginDescription.extraRepository)
      (settings ++ customPluginSettings, plugins, accPluginDependencies ++ pluginDescription.dependencies)
    }
  }


  private def dependenciesToString(libraries: TraversableOnce[Dependency], cache: Map[GroupArtifact, (MavenDependency, String)]) = {
    val (depSeq, newCache) = libraries.foldLeft((Seq.empty[String], cache)) { case ((accLibSeq, accCache), d) =>
      val md = d.mavenDependency
      val groupArtifact = GroupArtifact(md.groupId, md.artifactId)
      val (lib, c) = accCache.get(groupArtifact) match {
        case Some((mavenDep, l)) =>
          val res = VersionComparator.compare(mavenDep.versionId,md.versionId)
          if (res >= 0) {
            (l, accCache)
          }
          else {
            (l, accCache + (groupArtifact ->(md, l)))
          }
        case None =>
          val lib = s"""`${md.groupId}_${md.artifactId}`"""
          (lib, accCache + (groupArtifact ->(md, lib)))
      }

      val opt = d.scope match {
        case Scope.system => None
        case Scope.compile => Some(lib)
        case test@Scope.test if d.classifierTests => Some( s"""$lib % "$test" classifier "tests" """)
        case x => Some( s"""$lib % "$x" """)
      }

      (opt.fold(accLibSeq)(l => l +: accLibSeq), c)
    }
    (depSeq.mkString("", ",\n   ", ""), newCache)
  }


  private def dependsOnToString(dependsOn: TraversableOnce[Dependency], information: ProjectInformation) = dependsOn.map { d =>
    val test = d.scope match {
      case Scope.test => s"""% "test -> test" """
      case _ => ""
    }
    s"""`${changeNotSupportedSymbols(d.mavenDependency.artifactId)}`$test"""
  }.mkString(",")

  private def sort(set: Set[Dependency]): TraversableOnce[Dependency] =
    set.toIndexedSeq.sorted(Ordering.by[Dependency, (String, String)](x => (x.mavenDependency.groupId, x.mavenDependency.artifactId)))


  private def createBuildSbt(sbtProjectContent: SbtProjectContent, cache: Map[GroupArtifact, (MavenDependency, String)]) = {
    val SbtProjectContent(project, path, libraries, dependsOn, information, settings) = sbtProjectContent

    val projectName = changeNotSupportedSymbols(project.projectDependency.artifactId)

    val (dependencies, newCache) = dependenciesToString(sort(libraries), cache)

    val dependsOnString = dependsOnToString(sort(dependsOn), information)

    val projectString = if (path.isEmpty) {
      s"""
        |version := "${project.projectDependency.versionId}"
        |
        |name := "${project.projectDependency.artifactId}"
        |
        |organization := "${project.projectDependency.groupId}"
        |
        |libraryDependencies in Global ++= Seq($dependencies)
        |
        |${settings.mkString("", "\n\n", "\n")}
""".stripMargin
    } else {
      val settingString = settings.mkString(".settings(", ").\nsettings(", ")")
      s"""lazy val `$projectName` = ProjectName("$projectName","$path").settings(
      |  libraryDependencies ++= Seq($dependencies),
      |    name := "${project.projectDependency.artifactId}",
      |    version := "${project.projectDependency.versionId}",
      |    organization := "${project.projectDependency.groupId}"
      |)$settingString.dependsOn($dependsOnString)
      |
""".stripMargin
    }

    (projectString, newCache)
  }


}
