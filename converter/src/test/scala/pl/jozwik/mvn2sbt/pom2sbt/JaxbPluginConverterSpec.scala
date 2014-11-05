package pl.jozwik.mvn2sbt.pom2sbt

import java.io.File

import org.maven.{Plugin, Configuration4}
import pl.jozwik.mvn2sbt.AbstractSpec

class JaxbPluginConverterSpec extends AbstractSpec {

  "JaxbPluginConverterSpec " should {
    "Not contains -p" in {
      implicit val plugin = Plugin()
      val conf4 = Configuration4()
      val set = new JaxbPluginConverter().configurationToSet(conf4, new File("fake"))
      set.forall(s => !(s contains "-p")) shouldBe true
    }
  }
}
