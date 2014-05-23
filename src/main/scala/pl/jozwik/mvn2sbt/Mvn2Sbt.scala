package pl.jozwik.mvn2sbt

import java.io.File
import scala.io.Source
import com.typesafe.scalalogging.slf4j.StrictLogging
import java.nio.file.{Paths, Path}

object Mvn2Sbt extends StrictLogging {
  final val BUILD_SBT = "build.sbt"

  def projectsFromFile(inputFile: File) = {
    val it = fileToIterator(inputFile)
    iteratorToProjects(it)
  }

  def fromMavenCommand(rootDir: Path) = {
    import sys.process._
    val stream = Process(Seq("mvn", "dependency:tree"), rootDir.toFile).lineStream
    iteratorToProjects(stream)
  }

  private def iteratorToProjects(iterator: TraversableOnce[String]) = StreamProjectExtractor(iterator).projects


  private def fileToIterator(location: File) = {
    Source.fromFile(location).getLines()
  }

  def scanHierarchy(rootDir:Path):Map[MavenDepedency,Path] = DirProjectExtractor(rootDir).projectsMap



  def createSbtFile(projects:Seq[Project],hierarchy:Map[MavenDepedency,Path]) = {
???
  }


  def run(rootDir:Path){
    val projects = fromMavenCommand(rootDir)
    val hierarchy = scanHierarchy(rootDir)
    createSbtFile(projects,hierarchy)
  }

  def sbtFile(rootDir:Path) = Paths.get(rootDir.toFile.getAbsolutePath,BUILD_SBT)
}
