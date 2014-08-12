package pl.jozwik.mvn2sbt

import org.maven.{Build, Model}

class MavenSbtPluginMapperSpec extends AbstractSpec {

  "MavenSbtPluginMapper " should {
    "Return None " in {
      val model = Model(Map.empty)
      MavenSbtPluginMapper(model).plugins should be ('empty)
    }

    "Without plugins " in {
      val build = Build()
      MavenSbtPluginMapper.buildToPlugins(build) shouldBe MavenSbtPluginMapper.EMPTY_SEQ
    }
  }
}
