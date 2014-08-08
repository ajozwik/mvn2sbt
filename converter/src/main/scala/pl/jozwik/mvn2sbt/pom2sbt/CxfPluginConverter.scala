package pl.jozwik.mvn2sbt.pom2sbt

import java.io.File

import org.maven.Configuration4
import pl.jozwik.mvn2sbt.PomToSbtPluginConverter

import scala.xml.{Node, NodeSeq}


class CxfPluginConverter extends PomToSbtPluginConverter {

  import pl.jozwik.mvn2sbt.PluginConverter._

  private val ignoredArgs = Set("-wsdlLocation", "-autoNameResolution")

  def configurationToSet(confHead: Configuration4,rootDir:File): Set[String] = {
    val defaultOptSeq = extractMap(confHead, "wsdl", buildCxfSeq(rootDir), "defaultOptions").getOrElse("", Seq[String]())

    val wsdlOptionSeq = extractMap(confHead, "wsdl", buildCxfSeq(rootDir), "wsdlOptions", "wsdlOption")

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

  def buildCxfSeq(rootDir:File)(node: Node): Seq[String] = {
    val packages = extractNode(node, "packagenames", "packagename").flatMap(n => Seq("-p", n.text))
    val extraArgs = extractNode(node, "extraargs", "extraarg").map(_.text).filterNot(ignoredArgs.contains)
    val bindings = extractNode(node, "bindingFiles", "bindingFile").flatMap(x => Seq("-b", toPath(x.text, rootDir)))
    packages ++ extraArgs ++ bindings
  }


}
