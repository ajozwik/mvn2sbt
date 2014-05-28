package pl.jozwik.mvn2sbt

import org.maven.{Configuration4, Plugin}
import scala.xml.Node


object Converters {
  type Converter = Plugin => Seq[String]

  def cxfConverter: Converter = plugin => {
    val execution = plugin.executions.get.execution
    val configuration4 = execution.map { ex =>
      ex.configuration.get
    }
    val confHead = configuration4.head
    val wsdlOptionSeq = extractElement(confHead, "wsdlOptions") match {
      case Some(wsdlOptions) => extract(wsdlOptions.value.asInstanceOf[Node])
      case _ => Nil
    }

    val wsdls = wsdlOptionSeq.map {
      case (wsdl, packageName, extraOptions) =>
        val pn = extraOptions ++ {
          if (packageName.nonEmpty) {
            Seq("-p", packageName)
          } else Nil
        }

        s"""cxf.Wsdl("$wsdl", Seq(${pn.mkString("\"", "\",\"", "\"")}), None)"""
    }

    Seq( s"""cxf.wsdls :=Seq(${wsdls.mkString(",")})""")
  }

  def extractElement(confHead: Configuration4, name: String) = {
    confHead.any.find { r =>
      r.key == Some(name)
    }
  }

  private def extractNode(elem: Node, first: String, names: String*) = {
    names.foldLeft(elem \ first)((acc, name) => acc \ name)
  }


  private def extract(elem: Node, names: String*) = {
    val wsdlOption = extractNode(elem, "wsdlOption")
    wsdlOption.map(w => (extractNode(w, "wsdl").text, extractNode(w, "packagenames", "packagename").text,
      extractNode(w, "extraargs", "extraarg").map{_.text}))
  }

  def thriftConverter: Converter = plugin => Nil

  def groovyConverter: Converter = plugin => Nil
}
