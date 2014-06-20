package pl.jozwik.mvn2sbt

import java.io.File

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.maven.Plugin

case class SbtProjectContent(project: Project, path: String, libraries: Set[Dependency], dependsOn: Set[Dependency], information: ProjectInformation, settings: Set[String])

object SbtContent{
  private[mvn2sbt] def resolversToOption(resolvers: Set[String]) = {
    val start = "resolvers in Global ++= Seq(Resolver.mavenLocal"
    val stop = ")\n\n"
    val opt = if (resolvers.isEmpty) {
      None
    } else {
      val resolversString = resolvers.map { x =>
        val name = doubleQuote(x)
        s"$name at $name"
      }.mkString(",",
          """,
          """.stripMargin, "")

      Some(resolversString)
    }
    start + opt.getOrElse("") + stop
  }

  private def doubleQuote(str: Any) = "\"" + str + "\""
}

case class SbtContent(private val projects: Seq[Project], private val hierarchy: Map[MavenDependency, ProjectInformation], private val rootDir: File) extends LazyLogging {

  import PluginConverter._
  import SbtContent._

  private def optimizeDependsOn(set: Set[Dependency], map: Map[MavenDependency, SbtProjectContent]): Set[Dependency] = {
    val toRemove = set.flatMap {
      dep =>
        val setWithoutDep = set - dep
        map(dep.mavenDependency).dependsOn.find(d =>
          setWithoutDep.contains(d))
    }
    if (toRemove.isEmpty) {
      set
    } else {
      optimizeDependsOn(set.diff(toRemove), map)
    }
  }



  private def optimizeProject(map: Map[MavenDependency, SbtProjectContent], content: SbtProjectContent): SbtProjectContent = {
    val optimizedLibraries = optimizeLibraries(map, content)
    val optimizedDependsOn = optimizeDependsOn(content.dependsOn, map)
    content.copy(libraries = optimizedLibraries, dependsOn = optimizedDependsOn)
  }

  private def optimizeLibraries(map: Map[MavenDependency, SbtProjectContent], content: SbtProjectContent): Set[Dependency] = {
    val parentDependencies = content.dependsOn.flatMap { dep =>
      map(dep.mavenDependency).libraries
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

  private def optimizeProjects(depMap: Map[MavenDependency, SbtProjectContent]): Map[MavenDependency, SbtProjectContent] = {
    val optimizedMap = depMap.map {
      case (name, content) =>
        val optimized = optimizeProject(depMap, content)
        if (optimized != content) {
          (name, optimized)
        } else {
          (name, content)
        }
    }
    if (optimizedMap == depMap) {
      optimizedMap
    } else {
      optimizeProjects(optimizedMap)
    }
  }

  def buildSbtContentPluginContentAsString: (String, String) = {
    val buildSbtWriter = new StringBuilder
    val pluginsSbtWriter = new StringBuilder

    buildSbtWriter.append("def ProjectName(name: String,path:String): Project =  Project(name, file(path))\n\n")
    val (contentOfPluginSbt, resolvers, buildSbtProjects) = projects.foldLeft((Set[String](), Set[String](), Map.empty[MavenDependency, SbtProjectContent])) { (acc, p) =>
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
    val path = toPath(information.projectPath, rootDir)

    val (dependsOn, libraries) = splitToDependsOnLibraries(p, information)

    val (settings, plugins, pluginDependencies) = handlePlugins(information.projectPath, information.plugins)


    val (pluginsDependsOnDependenciesSettings, pluginsDependsOnDependenciesSet) =
      DependencyToPluginConverter.convert(rootDir, information.projectPath, libraries)

    (SbtProjectContent(p, path, pluginDependencies ++ libraries, dependsOn, information, pluginsDependsOnDependenciesSettings ++ settings), pluginsDependsOnDependenciesSet ++ plugins, information.resolvers)
  }


  private def splitToDependsOnLibraries(p: Project, information: ProjectInformation) = p.dependencies.partition { d =>
    val m = d.mavenDependency
    val contains = hierarchy.contains(m)
    val parentMatch = hierarchy(p.projectDependency).parent match {
      case Some(parent) =>
        parent == m
      case _ => false
    }
    contains || parentMatch
  }

  private def handlePlugins(file: File, plugins: Seq[(PluginDescription, Plugin)]): (Set[String], Set[String], Set[Dependency]) = {
    plugins.foldLeft((Set[String](), Set.empty[String], Set[Dependency]())) { (tuple, p) =>
      val (accSett, accPlug, accPluginDependencies) = tuple
      val (pluginDescription, plugin) = p
      val endSettings = DependencyToPluginConverter.addSeqToArray(file.equals(rootDir))
      val settings = accSett + (pluginDescription.sbtSetting + endSettings)
      val customPluginSettings = pluginDescription.pomConfigurationToSbtConfiguration(rootDir, plugin)
      val plugins = accPlug +(pluginDescription.plugin, pluginDescription.extraRepository)
      (settings ++ customPluginSettings, plugins, accPluginDependencies ++ pluginDescription.dependencies)
    }
  }


  def dependenciesToString(libraries: TraversableOnce[Dependency]) = libraries.flatMap { d =>
    val md = d.mavenDependency
    val lib = s"${doubleQuote(md.groupId)} % ${doubleQuote(md.artifactId)} % ${doubleQuote(md.versionId)}"
    d.scope match {
      case Scope.system => None
      case Scope.compile => Some(lib)
      case x@Scope.test if d.classifierTests => Some( s"""$lib % ${doubleQuote(x)} classifier ${doubleQuote("tests")}""")
      case x => Some(s"$lib % ${doubleQuote(x)}")
    }

  }.mkString("", ",\n   ", "")


  private def dependsOnToString(dependsOn: TraversableOnce[Dependency], information: ProjectInformation) = dependsOn.map { d =>
    val test = d.scope match {
      case Scope.test => s"% ${doubleQuote("test -> test")}"
      case _ => ""
    }
    s"""`${d.mavenDependency.artifactId}`$test"""
  }.mkString(",")

  def sort(set: Set[Dependency]): TraversableOnce[Dependency] = {
    set.toIndexedSeq.sorted(Ordering.by[Dependency,(String,String)](x => (x.mavenDependency.groupId,x.mavenDependency.artifactId)))
  }

  private def createBuildSbt(sbtProjectContent: SbtProjectContent) = {
    val SbtProjectContent(project, path, libraries, dependsOn, information, settings) = sbtProjectContent

    val projectName = project.projectDependency.artifactId

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
        |${settings.mkString("", "\n\n", "")}
""".stripMargin
    } else {
      val settingString = settings.mkString(".settings(", ").settings(", ")")
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
