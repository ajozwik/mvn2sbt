package pl.jozwik.mvn2sbt

import java.io.{File, PrintWriter}


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

case class SbtContent(private val projects: Seq[Project], private val hierarchy: Map[MavenDependency, File], private val rootDir: File) {

  import SbtContent._

  def write(writer: PrintWriter) {
    writer.println(
      """
        |name :="root"
        |
        |version := "0.1-SNAPSHOTS"
        |
        |def ProjectName(name: String,path:String): Project = (
        |  Project(name, file(path))
        |  )
        |
      """.stripMargin)
    projects.foreach { p =>
      val projectOutput = createProject(p)
      writer.write(projectOutput)
    }

  }

  private def createProject(p: Project) = {
    val projectName = p.mavenDependency.artifactId
    val path = toPath(hierarchy(p.mavenDependency), rootDir)

    val (dependsOn, ld) = p.dependencies.partition { d =>
      val m = d.mavenDependency
      hierarchy.contains(m)
    }

    val dependencies = ld.map { d =>
      val m = d.mavenDependency
      val scope = d.scope match {
        case Scope.compile => ""
        case x => s""" % "$x" """
      }
      s"""  "${m.groupId}" % "${m.artifactId}" % "${m.versionId}" $scope"""

    }.mkString("", ",\n   ", "")

    val dependsOnString = dependsOn.map { d =>
      val test = d.scope match {
        case Scope.test => """% "test -> test""""
        case _ => ""
      }
      s"""`${hierarchy(d.mavenDependency).getName}`$test"""
    }.mkString(",")
    if (path.isEmpty) {
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
      |).dependsOn($dependsOnString)
      |
    """.stripMargin
    }
  }


}
