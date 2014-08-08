package pl.jozwik.mvn2sbt

import pl.jozwik.mvn2sbt.PluginConverter.PluginConverter
import pl.jozwik.mvn2sbt.pom2sbt._

object PluginDescription {

  def apply(artifactId: String): Option[PluginDescription] = mapper.get(artifactId)

  private val mapper = Seq(GmavenPluginDesription.pluginDescription,
    MavenThriftPluginDescription.pluginDescription,
    CxfCodegenPluginDescription.pluginDescription,
    MavenWarPluginDescription.pluginDescription,
    Jaxb2MavenPlugin.pluginDescription).map(d => (d.artifactId, d)).toMap

}

case class PluginDescription(
                              artifactId: String,
                              sbtSetting: String,
                              plugin: String,
                              extraRepository: String,
                              dependencies: Seq[Dependency],
                              pomConfigurationToSbtConfiguration: PluginConverter
                              )
