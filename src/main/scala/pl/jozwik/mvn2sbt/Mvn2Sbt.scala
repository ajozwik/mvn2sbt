package pl.jozwik.mvn2sbt

import java.io.File
import scala.io.Source
import com.typesafe.scalalogging.slf4j.StrictLogging
import java.nio.file.Path

object Mvn2Sbt extends StrictLogging {


  def projectSeq(inputFile: File) = {
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
}
