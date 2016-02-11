package pl.jozwik.mvn2sbt

import pl.jozwik.mvn2sbt.PluginConverter.PluginConverter

case class PluginDescription(
  artifactId: String,
  sbtSetting: Option[String],
  pluginsSbtPluginConfiguration: String,
  extraRepository: String,
  dependencies: Seq[Dependency],
  pomConfigurationToSbtConfiguration: PluginConverter
)
