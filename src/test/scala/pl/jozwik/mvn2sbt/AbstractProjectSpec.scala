package pl.jozwik.mvn2sbt

import java.nio.file.Paths
import java.io.File
import Mvn2Sbt._

abstract class AbstractProjectSpec(project:String,inputFile:String = INPUT_TXT)  extends AbstractSpec {


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
