package pl.jozwik.mvn2sbt

import org.maven.Plugin
import scala.xml.Node


object Converters {
  type Converter = Plugin => String

  def cxfConverter:Converter = plugin => {
    val execution = plugin.executions.get.execution
    val aa = execution.flatMap { ex =>
      val configuration4 = ex.configuration.get
      configuration4.any.find { r =>
        r.key == Some("wsdlOptions")
      } match{
        case Some(wsdlOptions) => extract(wsdlOptions.value.asInstanceOf[Node])
        case _ => Nil
      }

    }
    aa
    """cxf.wsdls := Seq(cxf.Wsdl((resourceDirectory in Compile).value / "wsdl/Acc.wsdl", Seq("-p",  wsclientPackage), None))"""
  }

  private def extract(elem: Node) = {
    (elem \ "wsdlOption").map(w => ((w \ "wsdl").text, (w \ "packagenames" \ "packagename").text))

  def thriftConverter:Converter = plugin => ""

  def groovyConverter:Converter = plugin => ""
}
