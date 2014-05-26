package pl.jozwik.mvn2sbt

import java.io.{File, PrintWriter}
import com.typesafe.scalalogging.slf4j.LazyLogging


object SbtContent {
  def toPath(path: File, rootDir: File) = {
    val dir = path.getAbsolutePath
    val root = rootDir.getAbsolutePath
    val diff = dir.substring(root.length)
    if (diff.startsWith(File.separator)) {
      diff.substring(1)
    } else {
      diff
    }
  }
}

case class SbtContent(private val projects: Seq[Project], private val hierarchy: Map[MavenDependency, ProjectInformation], private val rootDir: File) extends LazyLogging {

  import SbtContent._

  def write(writer: PrintWriter) {
    writer.println(
      """
        |name :="root"
        |
        |version := "0.1-SNAPSHOTS"
        |
        |def ProjectName(name: String,path:String): Project =  Project(name, file(path))
        |
      """.stripMargin)
    projects.foreach { p =>
      val projectOutput = createProject(p)
      writer.write(projectOutput)
    }

  }

  private def createProject(p: Project) = {
    val projectName = p.mavenDependency.artifactId
    val information = hierarchy(p.mavenDependency)
    val path = toPath(information.projectPath, rootDir)

    val (dependsOn, libraries) = splitToDependsOnLibraries(p,information)

    val dependencies = toDependencies(libraries)

    val dependsOnString = toDependsOnString(dependsOn, information)

    val plugins = toPlugins(information.plugins)

    toProjectString(projectName, path, dependencies, dependsOnString, plugins)
  }

  private def splitToDependsOnLibraries(p: Project, information: ProjectInformation) = p.dependencies.partition { d =>
    val m = d.mavenDependency
    val contains = hierarchy.contains(m)
    val parentMatch = hierarchy(p.mavenDependency).parent match{
      case Some(parent) =>
        parent == m
      case _ => false
    }
    contains || parentMatch
  }

  private def toPlugins(plugins:Seq[PluginEnum]) = plugins.map(p => s".settings(${p.getSbtSetting})").mkString


  def toDependencies(libraries: Seq[Dependency]) = libraries.map { d =>
    val md = d.mavenDependency
    val scope = d.scope match {
      case Scope.compile => ""
      case x => s""" % "$x" """
    }
    s"""  "${md.groupId}" % "${md.artifactId}" % "${md.versionId}" $scope"""

  }.mkString("", ",\n   ", "")


  private def toDependsOnString(dependsOn: Seq[Dependency], information: ProjectInformation) =dependsOn.map { d =>
    val test = d.scope match {
      case Scope.test => """% "test -> test""""
      case _ => ""
    }
    s"""`${hierarchy(d.mavenDependency).projectPath.getName}`$test"""
  }.mkString(",")

  private def toProjectString(projectName: String, path: String, dependencies: String, dependsOnString: String, plugins: String) = if (path.isEmpty) {
    s"""|
        |libraryDependencies in Global ++= Seq($dependencies
        |)
        |
        """.stripMargin
  } else {
    s"""
      |
      |lazy val `$projectName` = ProjectName("$projectName","$path").settings(
      |  libraryDependencies ++= Seq($dependencies
      |    )
      |)$plugins.dependsOn($dependsOnString)
      |
    """.stripMargin
  }
}
