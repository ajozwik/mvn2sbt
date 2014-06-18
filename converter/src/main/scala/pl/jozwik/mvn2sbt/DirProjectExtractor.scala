package pl.jozwik.mvn2sbt

import com.typesafe.scalalogging.slf4j.LazyLogging
import java.io.File
import org.maven.{Parent, Model}
import scala.util.{Try, Success, Failure}

object DirProjectExtractor {
  final val POM_XML = "effective-pom.xml"
}

case class DirProjectExtractor(rootDir: File) extends LazyLogging {

  import DirProjectExtractor._

  val projectsMap = toProjectMap(rootDir, None)

  private def toProjectMap(dir: File, parent: Option[MavenDependency]) =
    dir.listFiles().find(f => f.getName == POM_XML) match {
      case Some(pomXml) =>
        handlePomFile(pomXml, parent)
      case None => throw new IllegalStateException(s"""$POM_XML file missing in ${dir.getAbsolutePath}, run "scala Eff.sc ${dir.getAbsolutePath}" first""")
    }

  private def handlePomFile(pomXml: File, parent: Option[MavenDependency]) = {
    val xmlFromFile = Try(xml.XML.loadFile(pomXml)) match {
      case Success(pom) => pom
      case Failure(th) =>
        logger.error(s"${pomXml.getAbsolutePath} failed to be parse")
        throw th
    }
    val pomModel = scalaxb.fromXML[org.maven.Model](xmlFromFile)
    createProjectMap(pomXml.getParentFile, pomModel, parent)
  }

  def toPomParent(parent: Option[Parent]) = parent match {
    case Some(p) => Some(MavenDependency(p.groupId.get, p.artifactId.get, p.version.get))
    case _ => None
  }


  private def createProjectMap(dir: File, pomModel: Model, parent: Option[MavenDependency]): Map[MavenDependency, ProjectInformation] = {
    val groupId = valueFromOptions(pomModel.groupId, parent.map(_.groupId))
    val version = valueFromOptions(pomModel.version, parent.map(_.versionId))
    val dependency = MavenDependency(groupId, pomModel.artifactId.get, version)
    val pomParent = toPomParent(pomModel.parent)
    val plugins = MavenSbtPluginMapper(pomModel).plugins
    val resolvers = pomModel.repositories.map(r => r.repository.flatMap(_.url)) match {
      case Some(seq) => seq
      case _ => Seq.empty[String]
    }
    val currMap = Map(dependency -> ProjectInformation(dir, pomParent, resolvers.toSet, plugins: _*))
    pomModel.modules match {
      case None => currMap
      case Some(modules) =>
        val seq = modules.module.map(m => toProjectMap(new File(dir, m), Some(dependency)))
        seq.foldLeft(currMap)((map, acc) => map ++ acc)
    }
  }

  private def valueFromOptions(option: Option[String], default: Option[String]) =
    (option, default) match {
      case (Some(value), _) =>
        value
      case (_, Some(defaultValue)) =>
        defaultValue
      case _ =>
        sys.error("Wrong configuration")
    }
}
