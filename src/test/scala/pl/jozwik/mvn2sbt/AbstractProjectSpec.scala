package pl.jozwik.mvn2sbt

import java.nio.file.Paths
import java.io.File


abstract class AbstractProjectSpec(project:String,inputFile:String = "input.txt")  extends AbstractSpec {
  import Mvn2Sbt._

  getClass.getSimpleName should {

    s"Create sbt file for $project with file" in {
      val rootDir = Paths.get(project).toFile
      val hierarchy = scanHierarchy(rootDir)
      hierarchy should not be Map.empty
      val projects = projectsFromFile(new File(rootDir,inputFile))
      createSbtFile(projects,hierarchy,rootDir,new File("target",project))

    }

  }
}
