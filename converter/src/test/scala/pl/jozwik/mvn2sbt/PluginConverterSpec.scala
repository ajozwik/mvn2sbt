package pl.jozwik.mvn2sbt

import org.maven.Plugin

class PluginConverterSpec extends AbstractSpec {

  "PluginConverter " should {

    "None for empty executions " in {
      PluginConverter.findConfiguration(Plugin()) shouldBe empty
    }
  }

}
