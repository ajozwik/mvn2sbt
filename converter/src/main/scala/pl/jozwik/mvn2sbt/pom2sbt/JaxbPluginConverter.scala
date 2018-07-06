package pl.jozwik.mvn2sbt.pom2sbt

import java.io.File

import org.maven.{Configuration4, Plugin}
import pl.jozwik.mvn2sbt.PomToSbtPluginConverter

class JaxbPluginConverter extends PomToSbtPluginConverter {

  protected[pom2sbt] def configurationToSet(confHead: Configuration4, rootDir: File)(implicit plugin: Plugin): Set[String] = {
    val packageParametersOption = toOption(confHead, "packageName", toPackageParametersOption)
    Set(
      Some("""sources in (Compile, xjc) <<= sourceDirectory map (_ / "main" / "xsd" ** "*.xsd" get) """),
      packageParametersOption).flatten
  }

  private def toPackageParametersOption(packageName: String) = {
    val packageParameters = s""""-p","$packageName","""
    s"""xjcCommandLine := Seq($packageParameters"-b",sourceDirectory.value.getAbsolutePath +"/main/xjb")"""
  }
}