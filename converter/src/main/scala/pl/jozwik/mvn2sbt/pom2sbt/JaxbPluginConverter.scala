package pl.jozwik.mvn2sbt.pom2sbt

import java.io.File

import org.maven.Configuration4
import pl.jozwik.mvn2sbt.PomToSbtPluginConverter

import scala.xml.Node


class JaxbPluginConverter extends PomToSbtPluginConverter {

  import pl.jozwik.mvn2sbt.PluginConverter._

  def configurationToSet(confHead: Configuration4, rootDir: File): Set[String] = {
    val node = extractElement(confHead, "packageName")
    val packageName = node.get.value.asInstanceOf[Node].text
    Set( """sources in (Compile, xjc) <<= sourceDirectory map (_ / "main" / "xsd" ** "*.xsd" get) """,
      s"""xjcCommandLine := Seq("-p","$packageName","-b",sourceDirectory.value.getAbsolutePath +"/main/xjb")""")
  }
}