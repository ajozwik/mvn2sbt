package pl.jozwik.mvn2sbt.pom2sbt

import pl.jozwik.mvn2sbt.PluginConverter.PluginConverter
import pl.jozwik.mvn2sbt.{Dependency, PluginDescription}

trait Pom2Sbt {

  val converter:PluginConverter

  val artifactId: String

  val sbtSetting: String

  val plugin: String

  val extraRepository: String

  val dependencies: Seq[Dependency]

  def pluginDescription:PluginDescription = PluginDescription (
    artifactId,
    sbtSetting,
    plugin,
    extraRepository,
    dependencies,
    converter
  )
}
