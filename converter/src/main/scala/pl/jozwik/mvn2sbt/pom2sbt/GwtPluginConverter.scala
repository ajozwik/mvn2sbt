package pl.jozwik.mvn2sbt.pom2sbt

import java.io.File

import org.maven.{Plugin, Configuration4}
import pl.jozwik.mvn2sbt.PomToSbtPluginConverter

import scala.xml.Node


object GwtPluginConverter {
  final val DEFAULT_GWT_VERSION = "2.3.0"
}

class GwtPluginConverter extends PomToSbtPluginConverter {

  import pl.jozwik.mvn2sbt.PluginConverter._

  protected [pom2sbt] def configurationToSet(confHead: Configuration4, rootDir: File)(implicit plugin:Plugin): Set[String] = {
    import GwtPluginConverter._
    val version = plugin.version.getOrElse(DEFAULT_GWT_VERSION)
    val node = findElement(confHead, "module")
    val moduleName = node.map(v => v.value.asInstanceOf[Node].text)
    val moduleOption = moduleName.fold(Set.empty[String])(s => Set(s"""gwtModules := List("$s")"""))
    moduleOption ++ Set( s"""gwtVersion := "$version" """)
  }
}