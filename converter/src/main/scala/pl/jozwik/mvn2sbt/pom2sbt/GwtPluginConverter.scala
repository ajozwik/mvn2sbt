package pl.jozwik.mvn2sbt.pom2sbt

import java.io.File

import org.maven.{Configuration4, Plugin}
import pl.jozwik.mvn2sbt.PomToSbtPluginConverter


class GwtPluginConverter extends PomToSbtPluginConverter {

  protected [pom2sbt] def configurationToSet(confHead: Configuration4, rootDir: File)(implicit plugin:Plugin): Set[String] = {
    val versionOption = plugin.version.map(version =>s"""gwtVersion := "$version" """)
    val moduleOption = toOption(confHead,"module",s => s"""gwtModules := List("$s")""" )
    val extraArgsOption = toOption(confHead,"extraJvmArgs",s => s"""javaOptions in Gwt += "$s" """)
    Set(extraArgsOption,moduleOption,versionOption).flatten
  }

}