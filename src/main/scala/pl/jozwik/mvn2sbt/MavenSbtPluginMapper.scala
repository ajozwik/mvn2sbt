package pl.jozwik.mvn2sbt

import org.maven.{Build, Plugin, Model}

private object MavenSbtPluginMapper {
  final val EMPTY_SEQ = Seq[(PluginDescription, Plugin)]()
}

case class MavenSbtPluginMapper(model: Model) {

  import MavenSbtPluginMapper.EMPTY_SEQ

  val plugins: Seq[(PluginDescription, Plugin)] =
    model.build.fold(EMPTY_SEQ)(buildToPlugins)


  private def buildToPlugins(build: Build) =
    build.plugins.map(plugins => plugins.plugin).fold(EMPTY_SEQ) {
      pluginsSeq => pluginsSeq.flatMap(plugin => findPlugin(plugin).map(p => (p, plugin)))
    }

  private def findPlugin(plugin: Plugin): Option[PluginDescription] =
    PluginDescription(orEmpty(plugin.artifactId))


  private def orEmpty(opt: Option[String]) = opt.getOrElse("")
}
