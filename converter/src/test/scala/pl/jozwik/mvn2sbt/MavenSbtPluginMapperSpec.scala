package pl.jozwik.mvn2sbt

import org.maven.{Build, Model}

class MavenSbtPluginMapperSpec extends AbstractSpec {

  "MavenSbtPluginMapper " should {
    "Return None " in {
      val model = Model()
      MavenSbtPluginMapper(model).plugins shouldBe Symbol("empty")
    }

    "Without plugins " in {
      val build = Build()
      MavenSbtPluginMapper.buildToPlugins(build) shouldBe MavenSbtPluginMapper.EMPTY_SEQ
    }
  }

}
