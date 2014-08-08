package pl.jozwik.mvn2sbt.pom2sbt

import pl.jozwik.mvn2sbt.PluginConverter._
import pl.jozwik.mvn2sbt._

object MavenThriftPluginDescription extends Pom2Sbt {
  val artifactId = "maven-thrift-plugin"
  val plugin = "com.github.bigtoast.sbtthrift.ThriftPlugin.thriftSettings"
  val extraRepository = """addSbtPlugin("com.github.bigtoast" % "sbt-thrift" % "0.7")"""
  val sbtSetting = """resolvers += "bigtoast-github" at "http://bigtoast.github.com/repo/" """
  val dependencies = Seq(new Dependency(new MavenDependency("commons-lang", "commons-lang", "2.6"), Scope.compile))
  val converter: PluginConverter = PluginConverter.defaultConverter
}
