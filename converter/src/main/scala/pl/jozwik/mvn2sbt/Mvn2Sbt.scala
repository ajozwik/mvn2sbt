package pl.jozwik.mvn2sbt

import java.io.{File, PrintWriter}
import scala.io.Source
import com.typesafe.scalalogging.slf4j.StrictLogging
import java.nio.file.{Paths, Path}
import org.apache.commons.io.IOUtils

object Mvn2Sbt extends StrictLogging {
  final val BUILD_SBT = "build.sbt"
  final val PROJECT = "project"
  final val PLUGINS_SBT = "plugins.sbt"
  final val DEPENDENCY_TREE_TXT = "dependencyTree.txt"

  def projectsFromFile(inputFile: File) = {
    val it = fileToIterator(inputFile)
    iteratorToProjects(it)
  }


  private def iteratorToProjects(iterator: TraversableOnce[String]) = StreamProjectExtractor(iterator).projects


  private def fileToIterator(location: File) = {
    Source.fromFile(location).getLines()
  }

  def scanHierarchy(rootDir: File) = DirProjectExtractor(rootDir).projectsMap


  def createSbtFile(projectsWithoutPath: Seq[Project], hierarchy: Map[MavenDependency, ProjectInformation], rootDir: File, outputDir: File) = {
    outputDir.mkdirs()
    val buildSbt = new File(outputDir, BUILD_SBT)
    val projectDir = new File(outputDir, PROJECT)
    projectDir.mkdirs()
    val pluginsSbt = new File(projectDir, PLUGINS_SBT)
    val (buildSbtContent, pluginSbtContent) = SbtContent(projectsWithoutPath, hierarchy, rootDir).buildSbtContentPluginContentAsString
    writeToFile(buildSbt, buildSbtContent)
    writeToFile(pluginsSbt, pluginSbtContent)
  }


  def run(rootDir: File, outputDir: File) {
    val hierarchy = scanHierarchy(rootDir)
    val projectsWithoutPath = projectsFromFile(new File(rootDir, DEPENDENCY_TREE_TXT))
    createSbtFile(projectsWithoutPath, hierarchy, rootDir, outputDir)
  }


  def sbtFile(rootDir: Path) = Paths.get(rootDir.toFile.getAbsolutePath, BUILD_SBT)


  private def writeToFile(file: File, content: String) = Some(new PrintWriter(file)).foreach {
    writer =>
      try {
        writer.write(content)
      } finally {
        IOUtils.closeQuietly(writer)
      }
  }


  def main(args: Array[String]) {

    def toAbsolutePath(f: File) = f.getAbsolutePath
    val out = if (args.length == 1) {
      args(0)
    } else if (args.length == 2) {
      args(1)
    } else {
      sys.error("Use <rootDirWithMavenProject> [<outputDir>]")
    }
    val rootDir = new File(args(0))
    val outputPath = new File(out)
    println(s"Start with ${toAbsolutePath(rootDir)}, output to ${toAbsolutePath(outputPath)}")
    if (rootDir.isDirectory) {
      run(rootDir, outputPath)
      println( s"""Go to $outputPath and copy $BUILD_SBT to $rootDir and $PLUGINS_SBT to ${new File(rootDir, "project")}""")
    }

  }
}