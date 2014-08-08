package pl.jozwik.mvn2sbt.pom2sbt

import pl.jozwik.mvn2sbt.PluginConverter

object GmavenPluginDesription extends Pom2Sbt {
  val artifactId = "gmaven-plugin"
  val plugin = "org.softnetwork.sbt.plugins.GroovyPlugin.groovy.settings"
  val extraRepository = """addSbtPlugin("org.softnetwork.sbt.plugins" % "sbt-groovy" % "0.1")"""
  val sbtSetting = """resolvers += "Biblio" at "http://mirrors.ibiblio.org/maven2" """
  val dependencies = Nil
  val converter = PluginConverter.defaultConverter
}
