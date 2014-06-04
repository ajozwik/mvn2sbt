package pl.jozwik.mvn2sbt

import java.nio.file.Paths
import java.io.File
import Mvn2Sbt._

class MultiSpec extends AbstractProjectSpec("multi")

class CxfSpec extends AbstractProjectSpec("cxf")

class Slf4jPomSpec extends AbstractProjectSpec("slf4j")

class ThriftSpec extends AbstractProjectSpec("thrift")


class LogbackPomSpec extends AbstractProjectSpec("logback")


abstract class AbstractProjectSpec(project:String,inputFile:String = DEPENDENCY_TREE_TXT)  extends AbstractSpec {


  getClass.getSimpleName should {

    s"Create sbt file for $project with file" in {
      val rootDir = Paths.get("exampleProjects",project).toFile
      val hierarchy = scanHierarchy(rootDir)
      hierarchy should not be Map.empty
      val projects = projectsFromFile(new File(rootDir,inputFile))
      createSbtFile(projects,hierarchy,rootDir,new File("target",project))

    }

  }
}
