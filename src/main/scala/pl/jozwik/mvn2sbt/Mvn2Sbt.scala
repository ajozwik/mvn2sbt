package pl.jozwik.mvn2sbt

import java.io.{Writer, File, PrintWriter}
import scala.io.Source
import com.typesafe.scalalogging.slf4j.StrictLogging
import java.nio.file.{Paths, Path}
import org.apache.commons.io.IOUtils
import org.maven.Plugin

object Mvn2Sbt extends StrictLogging {
  final val BUILD_SBT = "build.sbt"
  final val PLUGINS_SBT = "plugins.sbt"

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


  def createSbtFile(projectsWithoutPath: Seq[Project], hierarchy: Map[MavenDependency, ProjectInformation], rootDir: File, outputDir: File) = {
    outputDir.mkdirs()
    val buildSbt = new File(outputDir, BUILD_SBT)
    val pluginsSbt = new File(outputDir, PLUGINS_SBT)
    writeToFiles(buildSbt, pluginsSbt, SbtContent(projectsWithoutPath, hierarchy, rootDir).write)
  }


  def run(rootDir: File, outputDir: File) {
    val hierarchy = scanHierarchy(rootDir)
    val projectsWithoutPath = fromMavenCommand(rootDir)


    createSbtFile(projectsWithoutPath, hierarchy, rootDir, outputDir)
  }


  def sbtFile(rootDir: Path) = Paths.get(rootDir.toFile.getAbsolutePath, BUILD_SBT)


  private def writeToFiles(buildSbt: File, pluginsSbt: File, f: (Writer, Writer) => Unit) = {
    Some((new PrintWriter(buildSbt), new PrintWriter(pluginsSbt))).foreach { case (bpw, ppw) =>
      try {
        f(bpw, ppw)
      } finally {
        IOUtils.closeQuietly(bpw)
        IOUtils.closeQuietly(ppw)
      }
    }
  }

  def main(args: Array[String]) {

    def toAbsolutePath(f:File) = f.getAbsolutePath

    if (args.length == 2) {
      val rootDir = new File(args(0))
      val outputPath = new File(args(1))
      println(s"Start with ${toAbsolutePath(rootDir)}, output to ${toAbsolutePath(outputPath)}")
      if (rootDir.isDirectory) {
        run(rootDir, outputPath)
        println(s"""Go to $outputPath and copy $BUILD_SBT to $rootDir and $PLUGINS_SBT to ${new File(rootDir,"project")}""")
        sys.exit()
      }
    }
    logger.error("Use <rootDirWithMavenProject> <outputDir>")
  }
}