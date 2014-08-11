package pl.jozwik.mvn2sbt.pom2sbt

import java.io.File

import org.maven.Configuration4
import pl.jozwik.mvn2sbt.PomToSbtPluginConverter

import scala.xml.{Node, NodeSeq}


class CxfPluginConverter extends PomToSbtPluginConverter {

  import pl.jozwik.mvn2sbt.PluginConverter._

  private val ignoredArgs = Set("-wsdlLocation", "-autoNameResolution", "-verbose")

  def configurationToSet(confHead: Configuration4, rootDir: File): Set[String] = {
    val cxfSeq: Node => Seq[String] = buildCxfSeq(rootDir)
    val defaultOptSeq = extractMap(confHead, "wsdl", cxfSeq, "defaultOptions").getOrElse("", Seq[String]())

    val wsdlOptionSeq = extractMap(confHead, "wsdl", cxfSeq, "wsdlOptions", "wsdlOption")

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
    element.fold(Map.empty[String, Seq[String]]) {
      node =>
        val nodeSeq = node.value.asInstanceOf[NodeSeq]
        toKeySeqMap(nodeSeq, key, buildSeq, elements: _*)
    }
  }

  def buildCxfSeq(rootDir: File)(node: Node): Seq[String] = {
    val packages = extractNode(node, "packagenames", "packagename").flatMap(n => Seq("-p", n.text))
    val extraArgs = extractNode(node, "extraargs", "extraarg").map(_.text).filterNot(ignoredArgs.contains)
    val bindings = extractNode(node, "bindingFiles", "bindingFile").flatMap(x => Seq("-b", toPath(x.text, rootDir)))
    val extra = remove(extraArgs, "-exsh", "-fe")
    packages ++ extra ++ bindings
  }

  private def remove(seq: Seq[String], names: String*): Seq[String] = {
    if (names.isEmpty) {
      seq
    } else {
      val index = seq.indexOf(names.head)
      val toRet = if (index == -1) {
        seq
      } else {
        val (before, after) = seq.splitAt(index)
        val (_, rest) = after.splitAt(2)
        before ++ rest
      }
      remove(toRet, names.tail: _*)
    }
  }


}
