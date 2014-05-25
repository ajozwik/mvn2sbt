package pl.jozwik.mvn2sbt

import com.typesafe.scalalogging.slf4j.LazyLogging
import java.io.File
import org.maven.{Parent, Model}

object DirProjectExtractor {
  final val POM_XML = "pom.xml"
}

case class DirProjectExtractor(rootDir: File) extends LazyLogging {

  import DirProjectExtractor._

  val projectsMap = toProjectMap(rootDir, None)

  private def toProjectMap(dir: File, parent: Option[MavenDependency]) =
    dir.listFiles().find(f => f.getName == POM_XML) match {
      case Some(pomXml) =>
        val xmlFromFile = xml.XML.loadFile(pomXml)
        val pomModel = scalaxb.fromXML[org.maven.Model](xmlFromFile)
        addToMap(dir, pomModel, parent)
      case None => sys.error(s"$POM_XML file missing in ${dir.getAbsolutePath}")
    }

  def toPomParent(parent: Option[Parent]) = parent match {
    case Some(p) => Some(MavenDependency(p.groupId.get,p.artifactId.get,p.version.get))
    case _ => None
  }

  private def addToMap(dir: File, pomModel: Model, parent: Option[MavenDependency]): Map[MavenDependency, FileParentDependency] = {
    val groupId = valueFromOptions(pomModel.groupId, parent.map(_.groupId))
    val version = valueFromOptions(pomModel.version, parent.map(_.versionId))
    val dependency = MavenDependency(groupId, pomModel.artifactId.get, version)
    val pomParent = toPomParent(pomModel.parent)
    val currMap = Map(dependency -> FileParentDependency(dir,pomParent))
    pomModel.modules match {
      case None => currMap
      case Some(modules) =>
        val seq = modules.module.map(m => toProjectMap(new File(dir, m), Some(dependency)))
        seq.foldLeft(currMap)((map, acc) => map ++ acc)
    }
  }

  private def valueFromOptions(option: Option[String], default: Option[String]) =
    (option, default) match {
      case (Some(value), _) => value
      case (_, Some(defaultValue)) => defaultValue
      case _ => sys.error("Wrong configuration")
    }
}
