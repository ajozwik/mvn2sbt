package pl.jozwik.mvn2sbt.pom2sbt

import java.io.File

import org.maven.Configuration4
import pl.jozwik.mvn2sbt.PluginConverter.PluginConverter
import pl.jozwik.mvn2sbt.{Dependency, PomToSbtPluginConverter}

import scala.xml.{Node, NodeSeq}

object CxfCodegenPluginDescription extends Pom2Sbt {
  val artifactId = "cxf-codegen-plugin"
  val plugin = "cxf.settings"
  val extraRepository = """addSbtPlugin("com.ebiznext.sbt.plugins" % "sbt-cxf-wsdl2java" % "0.1.2")"""
  val sbtSetting: String = """resolvers += "Sonatype Repository" at "https://oss.sonatype.org/content/groups/public" """
  val dependencies: Seq[Dependency] = Nil
  val converter: PluginConverter = (rootDir, plugin) => CxfPluginConverter(rootDir).convert(plugin)
}


case class CxfPluginConverter(rootDir: File) extends PomToSbtPluginConverter {

  import pl.jozwik.mvn2sbt.PluginConverter._

  private val ignoredArgs = Set("-wsdlLocation", "-autoNameResolution")

  def configurationToSet(confHead: Configuration4): Set[String] = {
    val defaultOptSeq = extractMap(confHead, "wsdl", buildCxfSeq, "defaultOptions").getOrElse("", Seq[String]())

    val wsdlOptionSeq = extractMap(confHead, "wsdl", buildCxfSeq, "wsdlOptions", "wsdlOption")

    val wsdls = wsdlOptionSeq.map {
      case (wsdl, seq) =>
        val diff = toPath(wsdl, rootDir)
        val s = defaultOptSeq ++ seq
        s"""cxf.Wsdl(file("$diff"), Seq(${
          s.mkString("\"", "\",\"", "\"")
        }), "$diff")"""
    }
    Set( s"""cxf.wsdls :=Seq(${wsdls.mkString(",\n\t")})""")
  }

  def extractMap(confHead: Configuration4, key: String, buildSeq: (Node) => Seq[String], name: String, elements: String*): Map[String, Seq[String]] = {
    val element = extractElement(confHead, name)
    element match {
      case Some(node) =>
        val nodeSeq = node.value.asInstanceOf[NodeSeq]
        toKeySeqMap(nodeSeq, key, buildSeq, elements: _*)
      case _ =>
        Map.empty
    }
  }

  def buildCxfSeq(node: Node): Seq[String] = {
    val packages = extractNode(node, "packagenames", "packagename").flatMap(n => Seq("-p", n.text))
    val extraArgs = extractNode(node, "extraargs", "extraarg").map(_.text).filterNot(ignoredArgs.contains)
    val bindings = extractNode(node, "bindingFiles", "bindingFile").flatMap(x => Seq("-b", toPath(x.text, rootDir)))
    packages ++ extraArgs ++ bindings
  }


}
