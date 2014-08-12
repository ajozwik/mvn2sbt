package pl.jozwik.mvn2sbt.pom2sbt

import java.io.File

import org.maven.Configuration4
import pl.jozwik.mvn2sbt.PomToSbtPluginConverter

import scala.xml.Node


class JaxbPluginConverter extends PomToSbtPluginConverter {

  import pl.jozwik.mvn2sbt.PluginConverter._

  protected [pom2sbt] def configurationToSet(confHead: Configuration4, rootDir: File): Set[String] = {
    val node = findElement(confHead, "packageName")
    val packageName = node.map(v => v.value.asInstanceOf[Node].text)
    val packageParameters = packageName.fold(""){
      pn => s""""-p","$pn,""""
    }
    Set( """sources in (Compile, xjc) <<= sourceDirectory map (_ / "main" / "xsd" ** "*.xsd" get) """,
      s"""xjcCommandLine := Seq($packageParameters"-b",sourceDirectory.value.getAbsolutePath +"/main/xjb")""")
  }
}