package pl.jozwik.mvn2sbt

import java.io.File

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.maven.Plugin


case class SbtContent(private val projects: Seq[Project], private val hierarchy: Map[MavenDependency, ProjectInformation], private val rootDir: File) extends LazyLogging {

  import pl.jozwik.mvn2sbt.PluginConverter._

  def write: (String, String) = {
    val buildSbtWriter = new StringBuilder
    val pluginsSbtWriter = new StringBuilder

    buildSbtWriter.append("def ProjectName(name: String,path:String): Project =  Project(name, file(path))\n\n")
    val (contentOfPluginSbt, resolvers, buildSbtProjects) = projects.foldLeft((Set[String](), Set[String](), new StringBuilder)) { (acc, p) =>
      val (accContent, accResolvers, sb) = acc
      val (projectContent, pluginsSbt, resolvers) = handleProject(p)
      (accContent ++ pluginsSbt, accResolvers ++ resolvers, sb.append(projectContent))
    }
    buildSbtWriter.append(resolversToOption(resolvers))
    buildSbtWriter.append(buildSbtProjects)
    pluginsSbtWriter.append(contentOfPluginSbt.mkString("\n\n"))
    (buildSbtWriter.toString(), pluginsSbtWriter.toString())
  }

  private def resolversToOption(resolvers: Set[String]) = {
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

  private def pluginsDependsOnDependencies(file: File, seq: Seq[Dependency]) = {
    DependencyConverter.convert(rootDir, file, seq)

  }


  private def handleProject(p: Project): (String, Set[String], Set[String]) = {
    val projectName = p.mavenDependency.artifactId
    val information = hierarchy(p.mavenDependency)
    val path = toPath(information.projectPath, rootDir)

    val (dependsOn, libraries) = splitToDependsOnLibraries(p, information)

    val (settings, plugins, pluginDependencies) = handlePlugins(information.projectPath, information.plugins)

    val dependencies = dependenciesToString(pluginDependencies ++ libraries)

    val dependsOnString = dependsOnToString(dependsOn, information)

    val (pluginsDependsOnDependenciesSettings, pluginsDependsOnDependenciesSet) = pluginsDependsOnDependencies(information.projectPath, libraries)

    (createBuildSbt(p, projectName, path, dependencies, dependsOnString, pluginsDependsOnDependenciesSettings ++ settings), pluginsDependsOnDependenciesSet ++ plugins, information.resolvers)
  }


  private def splitToDependsOnLibraries(p: Project, information: ProjectInformation) = p.dependencies.partition { d =>
    val m = d.mavenDependency
    val contains = hierarchy.contains(m)
    val parentMatch = hierarchy(p.mavenDependency).parent match {
      case Some(parent) =>
        parent == m
      case _ => false
    }
    contains || parentMatch
  }

  private def handlePlugins(file: File, plugins: Seq[(PluginDescription, Plugin)]): (Set[String], Set[String], Seq[Dependency]) = {
    plugins.foldLeft((Set[String](), Set.empty[String], Seq[Dependency]())) { (tuple, p) =>
      val (accSett, accPlug, accPluginDependencies) = tuple
      val (pluginDescription, plugin) = p
      val endSettings = DependencyConverter.addSeqToArray(file.equals(rootDir))
      val settings = accSett + (pluginDescription.sbtSetting + endSettings)
      val customPluginSettings = pluginDescription.pomConfigurationToSbtConfiguration(rootDir, plugin)
      val plugins = accPlug +(pluginDescription.plugin, pluginDescription.extraRepository)
      (settings ++ customPluginSettings, plugins, pluginDescription.dependencies ++ accPluginDependencies)
    }
  }




  def dependenciesToString(libraries: Seq[Dependency]) = libraries.flatMap { d =>
    val md = d.mavenDependency
    val lib = s"${doubleQuote(md.groupId)} % ${doubleQuote(md.artifactId)} % ${doubleQuote(md.versionId)}"
    d.scope match {
      case Scope.system => None
      case Scope.compile => Some(lib)
      case x@Scope.test if d.classifierTests => Some( s"""$lib % ${doubleQuote(x)} classifier ${doubleQuote("tests")}""")
      case x => Some(s"$lib % ${doubleQuote(x)}")
    }

  }.mkString("", ",\n   ", "")


  private def dependsOnToString(dependsOn: Seq[Dependency], information: ProjectInformation) = dependsOn.map { d =>
    val test = d.scope match {
      case Scope.test => s"% ${doubleQuote("test -> test")}"
      case _ => ""
    }
    s"""`${d.mavenDependency.artifactId}`$test"""
  }.mkString(",")

  private def createBuildSbt(p: Project, projectName: String, path: String, dependencies: String, dependsOnString: String, settings: Set[String]) = {

    if (path.isEmpty) {
      s"""
        |version := "${p.mavenDependency.versionId}"
        |
        |name := "${p.mavenDependency.artifactId}"
        |
        |organization := "${p.mavenDependency.groupId}"
        |
        |libraryDependencies in Global ++= Seq($dependencies)
        |
        |${settings.mkString("", "\n\n", "")}
""".stripMargin
    } else {
      val settingString = settings.mkString(".settings(", ").settings(", ")")
      s"""lazy val `$projectName` = ProjectName("$projectName","$path").settings(
      |  libraryDependencies ++= Seq($dependencies),
      |    name := "${p.mavenDependency.artifactId}",
      |    version := "${p.mavenDependency.versionId}",
      |    organization := "${p.mavenDependency.groupId}"
      |)$settingString.dependsOn($dependsOnString)
      |
""".stripMargin
    }
  }


  private def doubleQuote(str: Any) = "\"" + str + "\""
}
