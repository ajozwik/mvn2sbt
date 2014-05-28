package pl.jozwik.mvn2sbt

import java.nio.file.Paths
import java.io.File
import scalaxb.DataRecord
import scala.xml.{Node, Elem}


class Wsdl extends AbstractSpec {
  "A " should {
    "b " in {
      val pomXml = Paths.get(System.getProperty("user.home"), "workspaceJob", "wsc",
        "iraq-wsc", "infrastructure", "icc-stubs").toFile
      val xmlFromFile = xml.XML.loadFile(new File(pomXml, DirProjectExtractor.POM_XML))
      val model = scalaxb.fromXML[org.maven.Model](xmlFromFile)
      val plugins = model.build.get.pluginManagement.get.plugins.get.plugin
      val cxfPlugin = plugins.find(plugin => plugin.artifactId == Some(PluginEnum.CXF.getArtifactId)).get
      val execution = cxfPlugin.executions.get.execution
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

    }
  }

  private def extract(elem: Node) = {
    (elem \ "wsdlOption").map(w => ((w \ "wsdl").text, (w \ "packagenames" \ "packagename").text))

  }
}
