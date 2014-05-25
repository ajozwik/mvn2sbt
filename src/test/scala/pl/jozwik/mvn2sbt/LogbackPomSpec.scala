package pl.jozwik.mvn2sbt

import org.scalatest.{Matchers, WordSpecLike}
import com.typesafe.scalalogging.slf4j.LazyLogging
import java.nio.file.Paths
import java.io.File

class LogbackPomSpec extends WordSpecLike with Matchers with LazyLogging {

  import Mvn2Sbt._

  "LogbackPomSpec " should {

    "Integration with file" in {
      val rootDir = Paths.get("logback").toFile
      val hierarchy = scanHierarchy(rootDir)
      hierarchy should not be Map.empty
      val projects = projectsFromFile(new File(rootDir,"input.txt"))
      createSbtFile(projects,hierarchy,rootDir,new File("target","logback-build.sbt"))

    }

  }


}
