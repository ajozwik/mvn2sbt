package pl.jozwik.mvn2sbt

import java.io.{File, PrintWriter}

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.IOUtils

import scala.io.Source

object Mvn2Sbt extends StrictLogging {
  val SBT_VERSION = "sbt.version"
  val BUILD_PROPERTIES = "build.properties"
  val DEFAULT_SBT_VERSION = "0.13.7"
  val BUILD_SBT = "build.sbt"
  val PROJECT = "project"
  val PLUGINS_SBT = "plugins.sbt"
  val DEPENDENCY_TREE_TXT = "dependencyTree.txt"

  private def projectsFromFile(inputFile: File) = {
    val source = Source.fromFile(inputFile)
    try {
      iteratorToProjects(source.getLines())
    } finally {
      source.close()
    }
  }

  private def iteratorToProjects(iterator: TraversableOnce[String]) = StreamProjectExtractor(iterator).projects

  def scanHierarchy(rootDir: File): Map[MavenDependency, ProjectInformation] = DirProjectExtractor(rootDir).projectsMap

  private def createSbtFile(projectsWithoutPath: Seq[Project], hierarchy: Map[MavenDependency, ProjectInformation], rootDir: File, outputDir: File) = {
    outputDir.mkdirs()
    val buildSbt = new File(outputDir, BUILD_SBT)
    val projectDir = new File(outputDir, PROJECT)
    projectDir.mkdirs()
    val buildProperties = new File(projectDir, BUILD_PROPERTIES)
    val pluginsSbt = new File(projectDir, PLUGINS_SBT)
    val content = SbtContent(projectsWithoutPath, hierarchy, rootDir)
    writeToFile(buildSbt, content.sbtContent)
    writeToFile(pluginsSbt, content.pluginContent)
    val sbtVersion = System.getProperty(SBT_VERSION, DEFAULT_SBT_VERSION)
    writeToFile(buildProperties, s"sbt.version=$sbtVersion")
  }

  private def handleRootDir(rootDir: File, outputDir: File) {
    val hierarchy = scanHierarchy(rootDir)
    val projectsWithoutPath = projectsFromFile(new File(rootDir, DEPENDENCY_TREE_TXT))
    createSbtFile(projectsWithoutPath, hierarchy, rootDir, outputDir)
  }

  private def writeToFile(file: File, content: String) = Some(new PrintWriter(file)).foreach {
    writer =>
      try {
        writer.write(content)
      } finally {
        IOUtils.closeQuietly(writer)
      }
  }

  private def toAbsolutePath(f: File) = f.getAbsolutePath

  def main(args: Array[String]) {

    val out = if (args.length == 1) {
      args(0)
    } else if (args.length == 2) {
      args(1)
    } else {
      throw new IllegalArgumentException("Use <rootDirWithMavenProject> [<outputDir>]")
    }
    val rootDir = new File(args(0))
    val outputPath = new File(out)
    logger.debug(s"Start with ${toAbsolutePath(rootDir)}, output to ${toAbsolutePath(outputPath)}")
    if (rootDir.isDirectory) {
      handleRootDir(rootDir, outputPath)
      logger.debug(s"""Go to $outputPath and copy $BUILD_SBT to $rootDir and $PLUGINS_SBT to ${new File(rootDir, "project")}""")
    }

  }
}