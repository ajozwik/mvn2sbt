package pl.jozwik.mvn2sbt

import java.io.File

class ConvertersSpec extends AbstractSpec{

  "Converters " should {
      "Extract supPath" in {
        val rootDir = new File(".")
        val subPath = "aaaaa"
        val subFile= new File(rootDir,subPath)
        val diff = PluginConverter.toPath(subFile,rootDir)
        diff should be(subPath)
      }
  }

}
