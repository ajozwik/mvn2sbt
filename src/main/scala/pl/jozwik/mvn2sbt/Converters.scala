package pl.jozwik.mvn2sbt

import org.maven.Plugin
import scala.xml.Node


object Converters {
  type Converter = Plugin => Seq[String]

  def cxfConverter:Converter = plugin => {
    val execution = plugin.executions.get.execution
    val wsdlOptionSeq = execution.flatMap { ex =>
      val configuration4 = ex.configuration.get
      configuration4.any.find { r =>
        r.key == Some("wsdlOptions")
      } match{
        case Some(wsdlOptions) => extract(wsdlOptions.value.asInstanceOf[Node])
        case _ => Nil
      }

    }
    val wsdls = wsdlOptionSeq.map{ case(wsdl,packageName) =>
     s"""cxf.Wsdl("$wsdl", Seq("-p","$packageName"), None)"""
    }
    Seq(s"""cxf.wsdls :=Seq(${wsdls.mkString(",")})""")
  }

  private def extract(elem: Node) = {
    (elem \ "wsdlOption").map(w => ((w \ "wsdl").text, (w \ "packagenames" \ "packagename").text))
  }

  def thriftConverter:Converter = plugin => Nil

  def groovyConverter:Converter = plugin => Nil
}
