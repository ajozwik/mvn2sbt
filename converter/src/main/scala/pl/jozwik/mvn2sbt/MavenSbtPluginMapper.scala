package pl.jozwik.mvn2sbt

import java.io.File

import com.typesafe.scalalogging.StrictLogging
import org.maven.{Build, Plugin, Model}
import pl.jozwik.gen.{Dependencies, Converter}
import pl.jozwik.mvn2sbt.PluginConverter._

import scala.util.{Failure, Success, Try}

object MavenSbtPluginMapper extends StrictLogging {
  private final val EMPTY_SEQ = Seq.empty[(PluginDescription, Plugin)]
  final val DEFAULT_CONVERTERS_PATH = "/converters.xml"
  final val CONVERTERS_PATH = "converters.path"
  private[mvn2sbt] lazy val artifactIdToPluginDescriptionMap: Map[String, PluginDescription] = {
    val path = System.getProperty(CONVERTERS_PATH, DEFAULT_CONVERTERS_PATH)
    val stream = getClass.getResourceAsStream(path)
    import org.maven._
    val converters = Try(xml.XML.load(stream)) match {
      case Success(s) => scalaxb.fromXML[pl.jozwik.gen.Converters](s)
      case Failure(th) => logger.error(s"$path not found")
        throw th
    }
    stream.close()
    converters.converter.foldLeft(Map.empty[String, PluginDescription]) {
      (acc, converter) =>
        val pluginDescription = toPluginDescription(converter)
        acc + (converter.artifactId -> pluginDescription)
    }
  }

  private def toPluginDescription(converter: Converter) = {
    val artifactId = converter.artifactId
    val sbtSetting = converter.sbtSetting
    val pluginsSbtPluginConfiguration = converter.pluginsSbtPluginConfiguration.fold("")(d => toAddSbtString(d))
    val extraRepository = converter.extraRepository.fold("")(repo => s"resolvers += $repo")
    val dependencies = toDependencySeq(converter.dependencies)
    val pomConfigurationToSbtConfiguration: PluginConverter = toPluginConverter(converter.converter)

    PluginDescription(artifactId, sbtSetting, pluginsSbtPluginConfiguration, extraRepository, dependencies, pomConfigurationToSbtConfiguration)
  }

  private def toAddSbtString(dependencies: Dependencies): String = {
    dependencies.dependency.map(d => s"""addSbtPlugin("${d.groupId}" % "${d.artifactId}" % "${d.version}") """).mkString("\n\n")
  }

  private def toDependencySeq(dependencies: Option[Dependencies]) =
    dependencies.fold(Seq.empty[Dependency])(ds =>
      ds.dependency.map {
        d =>
          val mavenDependency = MavenDependency(d.groupId, d.artifactId, d.version)
          val scope = toScope(d.scope)
          Dependency(mavenDependency, scope)
      })

  private def toScope(scope: Option[String]) = {
    scope match {
      case Some(s) => Scope.valueOf(s)
      case _ => Scope.compile
    }
  }

  private def toPluginConverter(converterClass: String) = {
    if (converterClass == null || converterClass.isEmpty) {
      PluginConverter.defaultConverter
    } else {
      val clazz = Class.forName(converterClass)
      val converter = clazz.newInstance().asInstanceOf[PomToSbtPluginConverter]
      (rootDir: File, plugin: Plugin) => converter.convert(plugin, rootDir)
    }
  }
}

case class MavenSbtPluginMapper(model: Model) {

  import MavenSbtPluginMapper._

  val plugins: Seq[(PluginDescription, Plugin)] =
    model.build.fold(EMPTY_SEQ)(buildToPlugins)


  private def buildToPlugins(build: Build) =
    build.plugins.map(plugins => plugins.plugin).fold(EMPTY_SEQ) {
      pluginsSeq => pluginsSeq.flatMap(plugin => findPlugin(plugin).map(p => (p, plugin)))
    }

  private def findPlugin(plugin: Plugin): Option[PluginDescription] =
    artifactIdToPluginDescriptionMap.get(orEmpty(plugin.artifactId))


  private def orEmpty(opt: Option[String]) = opt.getOrElse("")
}
