package pl.jozwik.mvn2sbt

import java.io.{File, PrintWriter}


case class SbtContent(private val projects: Seq[Project], hierarchy: Map[MavenDependency, File]) {


  def write(writer: PrintWriter) {
    writer.println(
      """
        |name :="root"
        |
        |version := "0.1-SNAPSHOTS"
        |
        |def ProjectName(name: String): Project = (
        |  Project(name, file(name))
        |  )
        |
      """.stripMargin)
    projects.foreach { p =>
      val (project,dependsOn) = createProject(p)
      writer.write(project)
    }

  }

  private def createProject(p: Project) = {
    val projectName = s"""${p.mavenDependency.artifactId}"""
    val dependencies = p.dependencies.map { d =>
      val m = d.mavenDependency
      val scope = d.scope match {
        case compile => ""
        case x => s"%$x"
      }
      s"""  "${m.groupId}"%"${m.artifactId}"%"${m.versionId}$scope""""
    }.mkString("", ",\n   ", "")

    s"""
      |
      |lazy val `$projectName` = ProjectName("$projectName").settings(
      |  libraryDependencies ++= Seq(
      |$dependencies
      |    )
      |)
      |
    """.stripMargin
  }

}
