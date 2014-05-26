package pl.jozwik.mvn2sbt

import java.io.File
import scala.io.Source
import com.typesafe.scalalogging.slf4j.StrictLogging
import java.nio.file.{Paths, Path}
import java.io.PrintWriter

object Mvn2Sbt extends StrictLogging {
  final val BUILD_SBT = "build.sbt"

  def projectsFromFile(inputFile: File) = {
    val it = fileToIterator(inputFile)
    iteratorToProjects(it)
  }

  def fromMavenCommand(rootDir: File) = {
    import sys.process._
    val stream = Process(Seq("mvn", "dependency:tree"), rootDir).lineStream
    iteratorToProjects(stream)
  }

  private def iteratorToProjects(iterator: TraversableOnce[String]) = StreamProjectExtractor(iterator).projects


  private def fileToIterator(location: File) = {
    Source.fromFile(location).getLines()
  }

  def scanHierarchy(rootDir: File) = DirProjectExtractor(rootDir).projectsMap


  def createSbtFile(projectsWithoutPath: Seq[Project], hierarchy: Map[MavenDependency, ProjectInformation], rootDir: File, outputFile: File) = {

    outputFile.delete()

    writeToFile(outputFile, SbtContent(projectsWithoutPath, hierarchy, rootDir).write)
  }


  def run(rootDir: File, outputFile: File) {
    val hierarchy = scanHierarchy(rootDir)
    val projectsWithoutPath = fromMavenCommand(rootDir)


    createSbtFile(projectsWithoutPath, hierarchy, rootDir, outputFile)
  }


  def sbtFile(rootDir: Path) = Paths.get(rootDir.toFile.getAbsolutePath, BUILD_SBT)


  private def writeToFile(file: File, f: (PrintWriter) => Unit) = {
    Some(new PrintWriter(file)).foreach { pw => try {
      f(pw)
    } finally {
      pw.close()
    }
    }
  }

  def main(args: Array[String]) {
    if (args.length == 2) {
      val rootDir = new File(args(0))
      val outputPath = new File(args(1))
      if (rootDir.isDirectory) {
        run(rootDir, outputPath)
        sys.exit()
      }
    }
    logger.error("Use <rootDirWithMavenProject> <outputSbtFileName>")
  }
}