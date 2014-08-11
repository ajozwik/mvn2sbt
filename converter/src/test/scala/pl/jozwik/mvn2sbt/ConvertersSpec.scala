package pl.jozwik.mvn2sbt

import java.io.File

import pl.jozwik.mvn2sbt.pom2sbt.CxfPluginConverter

import scalaxb.DataRecord

class ConvertersSpec extends AbstractSpec {

  "Converters " should {
    "Extract supPath" in {
      val rootDir = new File(".")
      val subPath = "aaaaa"
      val subFile = new File(rootDir, subPath)
      val diff = PluginConverter.toPath(subFile, rootDir)
      diff should be(subPath)
    }

    "Empty elements handling " in {
      import org.maven._
      val name = "aaa"
      val any = DataRecord[Exclusion](Exclusion())
      val conf4 = Configuration4(any)
      PluginConverter.findElement(conf4, name) shouldBe (None)
    }

  }

}
