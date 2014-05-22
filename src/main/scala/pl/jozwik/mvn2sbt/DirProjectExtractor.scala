package pl.jozwik.mvn2sbt

import java.nio.file.Path

object DirProjectExtractor{
  final val POM_XML = "pom.xml"
}

case class DirProjectExtractor(rootDir:Path) {
  import DirProjectExtractor._

  val projectsMap = {
    val pom = rootDir.toFile.listFiles().find(f => f.getName == POM_XML)
    pom match{
      case Some(p) =>
      case None =>
    }
    ???
  }
}
