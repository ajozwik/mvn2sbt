package pl.jozwik.mvn2sbt

import org.maven.{Configuration4, Plugin}
import scala.xml.{NodeSeq, Node}
import scalaxb.DataRecord

import java.io.File
import PluginConverter._

object PluginConverter {

  def toPath(path: File, rootDir: File): String = {
    val dir = path.getCanonicalPath
    val root = rootDir.getCanonicalPath
    val diff = dir.substring(root.length)
    if (diff.startsWith(File.separator)) {
      diff.substring(1)
    } else {
      diff
    }
  }

  def toPath(path: String, rootDir: File): String = toPath(new File(path), rootDir)

  type PluginConverter = (File, Plugin) => Set[String]

  val cxfConverter: PluginConverter = (rootDir, plugin) => CxfPluginConverter(rootDir).convert(plugin)

  val defaultConverter: PluginConverter = (rootDir, plugin) => Set()

  val thriftConverter = defaultConverter

  val groovyConverter = defaultConverter

  val warConverter = defaultConverter

  val jaxbConverter: PluginConverter = (rootDir, plugin) => JaxbPluginConverter(rootDir).convert(plugin)

  def extractElement(confHead: Configuration4, name: String): Option[DataRecord[Any]] = {
    confHead.any.find { r =>
      r.key match {
        case Some(n) => n == name
        case _ => false
      }
    }
  }

  def extractNode(elem: Node, first: String, names: String*): NodeSeq =
    names.foldLeft(elem \ first)((acc, name) => acc \ name)

  def toKeySeqMap(elem: Node, key: String, buildSeq: (Node) => Seq[String], elements: String*): Map[String, Seq[String]] = {
    val nodeSeq = elements.foldLeft(elem.asInstanceOf[NodeSeq])((acc, name) => acc \ name)
    toKeySeqMap(nodeSeq, key, buildSeq)
  }

  private def toKeySeqMap(nodeSeq: NodeSeq, key: String, buildSeq: (Node) => Seq[String]): Map[String, Seq[String]] =
    nodeSeq.foldLeft(Map.empty[String, Seq[String]]) {
      (acc, node) =>
        acc + (extractNode(node, key).text -> buildSeq(node))
    }

}

case class JaxbPluginConverter(rootDir: File) extends PomToSbtPluginConverter {

  def convert(plugin: Plugin): Set[String] = extractConfiguration(plugin) match {
    case (Some(confHead: Configuration4) :: tail) =>
      val node = extractElement(confHead, "packageName")
      val packageName = node.get.value.asInstanceOf[Node].text
      Set( """sources in (Compile, xjc) <<= sourceDirectory map (_ / "main" / "xsd" ** "*.xsd" get) """, s"""xjcCommandLine := Seq("-p","$packageName","-b",sourceDirectory.value.getAbsolutePath +"/main/xjb")""")
    case _ =>
      Set()
  }
}


case class CxfPluginConverter(rootDir: File) extends PomToSbtPluginConverter {


  private val ignoredArgs = Set("-wsdlLocation", "-autoNameResolution")

  def convert(plugin: Plugin): Set[String] = extractConfiguration(plugin) match {
    case (Some(confHead: Configuration4) :: tail) =>
      val defaultOptSeq = extractMap(confHead, "wsdl",buildCxfSeq, "defaultOptions").getOrElse("", Seq[String]())

      val wsdlOptionSeq = extractMap(confHead, "wsdl",buildCxfSeq, "wsdlOptions", "wsdlOption")

      val wsdls = wsdlOptionSeq.map {
        case (wsdl, seq) =>
          val diff = toPath(wsdl, rootDir)
          val s = defaultOptSeq ++ seq
          s"""cxf.Wsdl(file("$diff"), Seq(${
            s.mkString("\"", "\",\"", "\"")
          }), "$diff")"""
      }

      Set( s"""cxf.wsdls :=Seq(${wsdls.mkString(",\n\t")})""")
    case _ =>
      Set.empty
  }

  def extractMap(confHead: Configuration4, key: String, buildSeq: (Node) => Seq[String], name: String, elements: String*): Map[String, Seq[String]] = extractElement(confHead, name) match {
    case Some(node) =>
      val cast = node.value.asInstanceOf[Node]
      toKeySeqMap(cast, key, buildSeq, elements: _*)
    case _ => Map.empty
  }


  def buildCxfSeq(node: Node): Seq[String] = {
    val packages = extractNode(node, "packagenames", "packagename").flatMap(n => Seq("-p", n.text))
    val extraArgs = extractNode(node, "extraargs", "extraarg").map(_.text).filterNot(ignoredArgs.contains)
    val bindings = extractNode(node, "bindingFiles", "bindingFile").flatMap(x => Seq("-b", toPath(x.text, rootDir)))
    packages ++ extraArgs ++ bindings
  }


}


sealed trait PomToSbtPluginConverter {
  def convert(plugin: Plugin): Set[String]

  private[mvn2sbt] def extractConfiguration(plugin: Plugin) = {
    val execution = plugin.executions.get.execution
    execution.map { ex => ex.configuration}
  }

}
