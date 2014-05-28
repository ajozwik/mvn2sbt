package pl.jozwik.mvn2sbt

import org.maven.{Configuration4, Plugin}
import scala.xml.{NodeSeq, Node}
import scalaxb.DataRecord


object Converters {
  type Converter = Plugin => Seq[String]

  def cxfConverter: Converter = plugin => {
    val execution = plugin.executions.get.execution
    val configuration4 = execution.map { ex =>
      ex.configuration.get
    }
    val confHead = configuration4.head

    val defaultOptions: Map[String, Seq[String]] = extractElement(confHead, "defaultOptions") match {
      case Some(node) => extractWsdlOption(node.value.asInstanceOf[Node])
      case _ => Map.empty
    }

    val wsdlOptionSeq: Map[String, Seq[String]] = extractElement(confHead, "wsdlOptions") match {
      case Some(wsdlOptions) => extractWsdlOption(wsdlOptions.value.asInstanceOf[Node])
      case _ => Map.empty
    }

    val wsdls = wsdlOptionSeq.map {
      case (wsdl, seq) =>
        val s = defaultOptions.getOrElse(wsdl, Nil) ++ seq
        s"""cxf.Wsdl("$wsdl", Seq(${s.mkString("\"", "\",\"", "\"")}), None)"""
    }

    Seq( s"""cxf.wsdls :=Seq(${wsdls.mkString(",\n\t")})""")
  }

  def extractElement(confHead: Configuration4, name: String): Option[DataRecord[Any]] = {
    confHead.any.find { r =>
      r.key == Some(name)
    }
  }

  private def extractNode(elem: Node, first: String, names: String*): NodeSeq = {
    names.foldLeft(elem \ first)((acc, name) => acc \ name)
  }


  private def extractWsdlOption(elem: Node) = {
    val wsdlOption = extractNode(elem, "wsdlOption")
    wsdlOptionFromNode(wsdlOption)
  }

  private def wsdlOptionFromNode(wsdlOption: NodeSeq): Map[String, Seq[String]] = {
    wsdlOption.map(w => (extractNode(w, "wsdl").text, buildSeq(w))).toMap
  }

  private def buildSeq(node: Node) = {
    buildPackage(node) ++ buildExtraArgs(node) ++ buildBindings(node)
  }

  private def buildPackage(node: Node) = extractNode(node, "packagenames", "packagename").flatMap(n => Seq("-p", n.text))

  private def buildExtraArgs(node: Node) = extractNode(node, "extraargs", "extraarg").map(_.text)

  private def buildBindings(node: Node) =
    extractNode(node, "bindingFiles", "bindingFile").map(_.text)

  def thriftConverter: Converter = plugin => Nil

  def groovyConverter: Converter = plugin => Nil
}
