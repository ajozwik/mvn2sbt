package pl.jozwik.mvn2sbt.pom2sbt

import java.io.File

import org.maven.{Plugin, Configuration4}
import pl.jozwik.mvn2sbt.{PomToSbtPluginConverter, PluginConverter}

import scala.xml.Node

object Jaxb2MavenPlugin extends Pom2Sbt {
  val artifactId = "jaxb2-maven-plugin"
  val plugin = "com.github.retronym.sbtxjc.SbtXjcPlugin.xjcSettings"
  val extraRepository = """addSbtPlugin("org.scala-sbt.plugins" % "sbt-xjc" % "0.5")"""
  val sbtSetting = """resolvers += Resolver.url("scalasbt" , url("http://scalasbt.artifactoryonline.com/scalasbt/repo"))(Resolver.ivyStylePatterns)"""
  val dependencies = Nil
  val converter = (rootDir: File, plugin: Plugin) => JaxbPluginConverter(rootDir).convert(plugin)
}


case class JaxbPluginConverter(rootDir: File) extends PomToSbtPluginConverter {

  import PluginConverter._

  def configurationToSet(confHead: Configuration4): Set[String] = {
    val node = extractElement(confHead, "packageName")
    val packageName = node.get.value.asInstanceOf[Node].text
    Set( """sources in (Compile, xjc) <<= sourceDirectory map (_ / "main" / "xsd" ** "*.xsd" get) """,
      s"""xjcCommandLine := Seq("-p","$packageName","-b",sourceDirectory.value.getAbsolutePath +"/main/xjb")""")
  }
}