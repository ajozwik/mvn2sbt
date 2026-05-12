package pl.jozwik.mvn2sbt

import org.apache.commons.io.FileUtils

import java.io.File

object MainTest {

  @main def main(): Unit = {
    val file    = new File(TestConstants.EXAMPLES_PROJECTS, "logback").getAbsolutePath
    val outFile = new File(System.getProperty("java.io.tmpdir"), "logback")
    val out     = outFile.getAbsolutePath
    FileUtils.deleteDirectory(outFile)
    Mvn2Sbt.main(file, out)
  }

}
