package pl.jozwik.mvn2sbt

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.maven.Plugin

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

  private def optimizeDependsOn(project:Project,dependsOn: Set[Dependency], sbtProjectsMap: Map[MavenDependency, SbtProjectContent]): Set[Dependency] = {
    val toRemove = dependsOn.flatMap {
      dependOn =>
        val setWithoutDep = dependsOn - dependOn
        dependOn.scope match {
          case Scope.test =>
            None
          case _ =>
            val project = sbtProjectsMap(dependOn.mavenDependency)
            project.dependsOn.find(d => setWithoutDep.contains(d))
        }
    }
    if (toRemove.isEmpty) {
      dependsOn
    } else {
      logger.debug(s"${project.projectDependency}: Remove $toRemove")
      optimizeDependsOn(project,dependsOn.diff(toRemove), sbtProjectsMap)
    }
  }


  private def optimizeProject(sbtProjectsMap: Map[MavenDependency, SbtProjectContent], content: SbtProjectContent): SbtProjectContent = {
    val optimizedLibraries = optimizeLibraries(sbtProjectsMap, content)
    val optimizedDependsOn = optimizeDependsOn(content.project,content.dependsOn, sbtProjectsMap)
    content.copy(libraries = optimizedLibraries, dependsOn = optimizedDependsOn)
  }

  private def optimizeLibraries(map: Map[MavenDependency, SbtProjectContent], content: SbtProjectContent): Set[Dependency] = {
    val parentDependencies = content.dependsOn.flatMap { dep =>
      if (dep.scope == Scope.compile) {
        map(dep.mavenDependency).libraries
      } else {
        Nil
      }
    }

    val parentDependenciesSet = parentDependencies.toSet
    val optimizedLibraries = content.libraries.flatMap { dep =>
      if (parentDependenciesSet.contains(dep) && dep.scope == Scope.compile && content.libraries.find(p => p.mavenDependency == dep.mavenDependency && p.scope != Scope.compile).isEmpty) {
        None
      } else {
        Some(dep)
      }
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

  def buildSbtContentPluginContentAsString: (String, String) = {
    val buildSbtWriter = new StringBuilder
    val pluginsSbtWriter = new StringBuilder
    val defaultScalaVersion = System.getProperty(SCALA_VERSION_IN_GLOBAL, "2.11.2")
    buildSbtWriter.append( s"""scalaVersion in Global := "$defaultScalaVersion" """).append("\n\n")
    buildSbtWriter.append("def ProjectName(name: String,path:String): Project =  Project(name, file(path))").append("\n\n")
    val (contentOfPluginSbt, resolvers, buildSbtProjects) = projects.foldLeft((Set.empty[String], Set.empty[String], Map.empty[MavenDependency, SbtProjectContent])) {
      (acc, p) =>
        val (accContent, accResolvers, stbProjectContent) = acc
        val (projectContent, pluginsSbt, resolvers) = handleProject(p)
        (accContent ++ pluginsSbt, accResolvers ++ resolvers, stbProjectContent + (p.projectDependency -> projectContent))
    }


    buildSbtWriter.append(resolversToOption(resolvers))
    val optimizedMaps = optimizeProjects(buildSbtProjects)
    writeProjects(buildSbtWriter, optimizedMaps)

    pluginsSbtWriter.append(contentOfPluginSbt.mkString("\n\n"))
    (buildSbtWriter.toString(), pluginsSbtWriter.toString())
  }

  private def writeProjects(sb: StringBuilder, projectsMap: Map[MavenDependency, SbtProjectContent]) {
    val sorted = projectsMap.toSeq.sortBy((a) => a._1.artifactId)
    sorted.foreach {
      case (name, content) => sb.append(createBuildSbt(content))
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


  def dependenciesToString(libraries: TraversableOnce[Dependency]) = libraries.flatMap { d =>
    val md = d.mavenDependency
    val lib = s""" "${md.groupId}" % "${md.artifactId}" % "${md.versionId}" """
    d.scope match {
      case Scope.system => None
      case Scope.compile => Some(lib)
      case test@Scope.test if d.classifierTests => Some( s"""$lib % "$test" classifier "tests" """)
      case x => Some( s"""$lib % "$x" """)
    }

  }.mkString("", ",\n   ", "")


  private def dependsOnToString(dependsOn: TraversableOnce[Dependency], information: ProjectInformation) = dependsOn.map { d =>
    val test = d.scope match {
      case Scope.test => s"""% "test -> test" """
      case _ => ""
    }
    s"""`${changeNotSupportedSymbols(d.mavenDependency.artifactId)}`$test"""
  }.mkString(",")

  def sort(set: Set[Dependency]): TraversableOnce[Dependency] = {
    set.toIndexedSeq.sorted(Ordering.by[Dependency, (String, String)](x => (x.mavenDependency.groupId, x.mavenDependency.artifactId)))
  }

  private def createBuildSbt(sbtProjectContent: SbtProjectContent) = {
    val SbtProjectContent(project, path, libraries, dependsOn, information, settings) = sbtProjectContent

    val projectName = changeNotSupportedSymbols(project.projectDependency.artifactId)

    val dependencies = dependenciesToString(sort(libraries))

    val dependsOnString = dependsOnToString(sort(dependsOn), information)

    if (path.isEmpty) {
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
  }


}
