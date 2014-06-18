package pl.jozwik.mvn2sbt

import org.maven.Model

class MavenSbtPluginMapperSpec extends AbstractSpec {

  "MavenSbtPluginMapper " should {
    "Return None " in {
      val model = Model(Map.empty)
      MavenSbtPluginMapper(model).plugins should be(Seq())
    }
  }
}
