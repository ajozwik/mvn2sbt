package pl.jozwik.mvn2sbt.pom2sbt

import pl.jozwik.mvn2sbt._

object MavenWarPluginDescription extends Pom2Sbt {
  val artifactId = "maven-war-plugin"
  val plugin = "webSettings"
  val extraRepository = """addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "0.9.0")"""
  val sbtSetting = ""
  val dependencies = Seq(new Dependency(new MavenDependency("org.eclipse.jetty", "jetty-webapp", "9.1.0.v20131115"), Scope.container),
    new Dependency(new MavenDependency("org.eclipse.jetty", "jetty-plus", "9.1.0.v20131115"), Scope.container))
  val converter = PluginConverter.defaultConverter
}
