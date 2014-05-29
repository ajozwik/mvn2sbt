package pl.jozwik.mvn2sbt

import java.io.{Writer, File}
import com.typesafe.scalalogging.slf4j.LazyLogging
import scala.collection.JavaConversions._
import org.maven.Plugin


case class SbtContent(private val projects: Seq[Project], private val hierarchy: Map[MavenDependency, ProjectInformation], private val rootDir: File) extends LazyLogging {

  import Converters._

  def write(buildSbtWriter: Writer, pluginsSbtWriter: Writer) {
    buildSbtWriter.write(
      """
        |
        |def ProjectName(name: String,path:String): Project =  Project(name, file(path))
        |
      """.stripMargin)
    val (contentOfPluginSbt,resolvers) = projects.foldLeft((Set[String](),Set[String]())) { (acc,p) =>
      val (accContent,accResolvers) = acc
      val (buildSbt, pluginsSbt,resolvers) = handleProject(p)
      buildSbtWriter.write(buildSbt)
      (accContent ++ pluginsSbt,accResolvers ++ resolvers)
    }
    if(resolvers.nonEmpty){
      buildSbtWriter.write("resolvers++=Seq(")
    buildSbtWriter.write(resolvers.map(x =>
      s""""$x" at "$x"""").mkString(",\n"))
      buildSbtWriter.write(")\n\n")
    }

    pluginsSbtWriter.write(contentOfPluginSbt.mkString("\n\n"))
  }

  private def handleProject(p: Project):(String,Set[String],Set[String]) = {
    val projectName = p.mavenDependency.artifactId
    val information = hierarchy(p.mavenDependency)
    val path = toPath(information.projectPath, rootDir)

    val (dependsOn, libraries) = splitToDependsOnLibraries(p, information)

    val (settings, plugins, pluginDependencies) = handlePlugins(information.plugins)

    val dependencies = dependenciesToString(pluginDependencies ++ libraries)

    val dependsOnString = dependsOnToString(dependsOn, information)



    (createBuildSbt(p, projectName, path, dependencies, dependsOnString, settings), plugins,information.resolvers)
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

  private def handlePlugins(plugins: Seq[(PluginEnum, Plugin)]): (Seq[String], Set[String], Seq[Dependency]) = {
    plugins.foldLeft((Seq[String](), Set.empty[String], Seq[Dependency]())) { (tuple, p) =>
      val (accSett, accPlug, accPluginDependencies) = tuple
      val (pluginEnum, plugin) = p
      val settings =  s"${pluginEnum.getSbtSetting}" +: accSett
      val customPluginSettings = pluginEnum.getFunction()(rootDir,plugin)
      val plugins = accPlug + (pluginEnum.getPlugin, pluginEnum.getExtraRepository)
      (settings ++ customPluginSettings , plugins, pluginEnum.getDependencies ++ accPluginDependencies)
    }
  }


  def dependenciesToString(libraries: Seq[Dependency]) = libraries.map { d =>
    val md = d.mavenDependency
    val scope = d.scope match {
      case Scope.compile => ""
      case x => s""" % "$x" """
    }
    s"""  "${md.groupId}" % "${md.artifactId}" % "${md.versionId}" $scope"""

  }.mkString("", ",\n   ", "")


  private def dependsOnToString(dependsOn: Seq[Dependency], information: ProjectInformation) = dependsOn.map { d =>
    val test = d.scope match {
      case Scope.test => """% "test -> test""""
      case _ => ""
    }
    s"""`${d.mavenDependency.artifactId}`$test"""
  }.mkString(",")

  private def createBuildSbt(p: Project, projectName: String, path: String, dependencies: String, dependsOnString: String, settings: Seq[String]) = {

    if (path.isEmpty) {
      s"""|
        |version := "${p.mavenDependency.versionId}"
        |
        |name := "${p.mavenDependency.artifactId}"
        |
        |organization := "${p.mavenDependency.groupId}"
        |
        |libraryDependencies in Global ++= Seq($dependencies
        |)
        |
        |${settings.mkString("", "\n\n", "")}
        """.stripMargin
    } else {
      val s = settings.mkString(".settings(", ").settings(", ")")
      s"""
      |
      |lazy val `$projectName` = ProjectName("$projectName","$path").settings(
      |  libraryDependencies ++= Seq($dependencies
      |    ),
      |    name := "${p.mavenDependency.artifactId}",
      |    version := "${p.mavenDependency.versionId}",
      |    organization := "${p.mavenDependency.groupId}"
      |)$s.dependsOn($dependsOnString)
      |
    """.stripMargin
    }
  }

}
