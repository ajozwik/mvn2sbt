package pl.jozwik.mvn2sbt.pom2sbt

import java.io.File

import org.maven.{Plugin, Configuration4}
import pl.jozwik.mvn2sbt.{ReflectionUtils, PomToSbtPluginConverter}

import scala.xml.{Node, NodeSeq}

object CxfPluginConverter {

  private[pom2sbt] final val ignored = Seq("-exsh", "-fe")

  private[pom2sbt] def remove(seq: Seq[String], names: String*): Seq[String] = names match {
    case Seq() =>
      seq
    case h +: t =>
      val index = seq.indexOf(h)
      val toRet = if (index == -1) {
        seq
      } else {
        val (before, after) = seq.splitAt(index)
        val (_, rest) = after.splitAt(2)
        before ++ rest
      }
      remove(toRet, t: _*)
  }
}

class CxfPluginConverter extends PomToSbtPluginConverter {

  import CxfPluginConverter._
  import pl.jozwik.mvn2sbt.PluginConverter._

  private val ignoredArgs = Set("-wsdlLocation", "-autoNameResolution", "-verbose")

  def configurationToSet(confHead: Configuration4, rootDir: File)(implicit plugin: Plugin): Set[String] = {
    val cxfSeqClosure: Node => Seq[String] = buildCxfSeq(rootDir)
    val defaultOptSeq = createKeySeqMap(confHead, "wsdl", cxfSeqClosure, "defaultOptions").getOrElse("", Seq.empty[String])

    val wsdlOptionSeq = createKeySeqMap(confHead, "wsdl", cxfSeqClosure, "wsdlOptions", "wsdlOption")

    val wsdls = wsdlOptionSeq.map {
      case (wsdl, seq) =>
        val diff = toPath(wsdl, rootDir)
        val s = defaultOptSeq ++ seq
        s"""cxf.Wsdl(file("$diff"), Seq(${
          s.mkString("\"", "\",\"", "\"")
        }), "$diff")"""
    }
    Set(s"""cxf.wsdls :=Seq(${wsdls.mkString(",\n\t")})""")
  }

  def createKeySeqMap(confHead: Configuration4, key: String, buildSeq: (Node) => Seq[String], name: String, elements: String*): Map[String, Seq[String]] = {
    val element = findElement(confHead, name)
    element.fold(Map.empty[String, Seq[String]]) {
      node =>
        val nodeSeq = ReflectionUtils.castTo[NodeSeq](node.value)
        toKeySeqMap(nodeSeq, key, buildSeq, elements: _*)
    }
  }

  def buildCxfSeq(rootDir: File)(node: Node): Seq[String] = {
    val packages = findNode(node, "packagenames", "packagename").flatMap(n => Seq("-p", n.text))
    val bindings = findNode(node, "bindingFiles", "bindingFile").flatMap(x => Seq("-b", toPath(x.text, rootDir)))
    val extraArgs = findNode(node, "extraargs", "extraarg").map(_.text).filterNot(ignoredArgs.contains)
    val extra = remove(extraArgs, ignored: _*)
    packages ++ extra ++ bindings
  }

}
