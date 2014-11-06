package pl.jozwik.mvn2sbt

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.maven.{Model, Parent}

import scala.util.{Failure, Success, Try}

object DirProjectExtractor extends LazyLogging {
  val EFFECTIVE_POM_XML = "effective-pom.xml"

  private[mvn2sbt] def valueFromOptions(option: Option[String], default: Option[String]) =
    (option, default) match {
      case (Some(value), _) =>
        value
      case (_, Some(defaultValue)) =>
        defaultValue
      case _ =>
        throw new IllegalArgumentException("Both None")
    }

  private[mvn2sbt] def extractOption[T](op: Option[T]): T = op match {
    case Some(t) => t
    case None => sys.error("None")
  }

  private[mvn2sbt] def createProjectMap(dir: File, pomModel: Model, parent: Option[MavenDependency]): Map[MavenDependency, ProjectInformation] = {
    val groupId = valueFromOptions(pomModel.groupId, parent.map(_.groupId))
    val version = valueFromOptions(pomModel.version, parent.map(_.versionId))
    val dependency = MavenDependency(groupId, extractOption(pomModel.artifactId), version)
    val pomParent = toPomParent(pomModel.parent)
    val plugins = MavenSbtPluginMapper(pomModel).plugins
    val resolversOptions = pomModel.repositories.map(r => r.repository.flatMap(_.url))
    val resolvers = resolversOptions.fold(Seq.empty[String]) {
      seq => seq
    }
    val currMap = Map(dependency -> ProjectInformation(dir, pomParent, resolvers.toSet, plugins: _*))
    pomModel.modules match {
      case None =>
        currMap
      case
        Some(modules) =>
        val seq = modules.module.map(m => toProjectMap(new File(dir, m), Some(dependency)))
        seq.foldLeft(currMap)((map, acc) => map ++ acc)
    }
  }

  private def toPomParent(parent: Option[Parent]) = parent.map(
    p => MavenDependency(extractOption(p.groupId), extractOption(p.artifactId), extractOption(p.version))
  )

  private def toProjectMap(dir: File, parent: Option[MavenDependency]) = {
    val pomOption = dir.listFiles.find(f => f.getName == EFFECTIVE_POM_XML)
    pomOption.fold(throw new IllegalStateException( s"""$EFFECTIVE_POM_XML file missing in ${dir.getAbsolutePath}, run "scala Eff.sc ${dir.getAbsolutePath}" first""")) {
      (pomXml) =>
        handlePomFile(pomXml, parent)
    }
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

}

case class DirProjectExtractor(rootDir: File) {

  import pl.jozwik.mvn2sbt.DirProjectExtractor._

  val projectsMap = toProjectMap(rootDir, None)


}
