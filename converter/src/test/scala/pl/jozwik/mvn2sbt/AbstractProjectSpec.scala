package pl.jozwik.mvn2sbt

import java.io.File
import java.nio.file.Paths

import pl.jozwik.mvn2sbt.Mvn2Sbt._


object TestConstants {
  final val EXAMPLES_PROJECTS = "exampleProjects"
}

class MultiSpec extends AbstractProjectSpec("multi")

class CxfSpec extends AbstractProjectSpec("cxf") {
  override protected def checkBuildSbtContent(content: String) {
    content should not include "-exsh"
    content should not include "-fe"
  }
}

class CxfEmptySpec extends AbstractProjectSpec("cxf_empty")

class RootTestngSpec extends AbstractProjectSpec("root_testng") {
  override protected def checkBuildSbtContent(content: String) {
    content should include("testng")
  }
}

class Slf4jPomSpec extends AbstractProjectSpec("slf4j")

class ThriftSpec extends AbstractProjectSpec("thrift")


class LogbackPomSpec extends AbstractProjectSpec("logback")


abstract class AbstractProjectSpec(project: String) extends AbstractSpec {


  private def checkBuildSbtContent(buildSbt: File) {
    val source = scala.io.Source.fromFile(buildSbt)
    val content = try {
      source.mkString
    } finally {
      source.close()
    }
    checkBuildSbtContent(content)
  }

  protected def checkBuildSbtContent(content: String) {

  }


  getClass.getSimpleName should {

    s"Create sbt file for $project with file" in {
      val rootDir = Paths.get(TestConstants.EXAMPLES_PROJECTS, project).toFile
      val output = new File("target", project)
      Mvn2Sbt.main(Array(rootDir.getAbsolutePath, output.getAbsolutePath))
      val buildSbt = new File(output, BUILD_SBT)
      buildSbt.exists() should be(true)
      checkBuildSbtContent(buildSbt)
    }

  }
}
