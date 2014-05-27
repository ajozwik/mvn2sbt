package pl.jozwik.mvn2sbt

import org.maven.{Plugin, Model}


case class MavenSbtPluginMapper(model: Model) {

  val plugins: Seq[PluginEnum] = {
    model.build match {
      case Some(build) => build.plugins.map(plugins => plugins.plugin) match {
        case Some(pluginsSeq) => pluginsSeq.flatMap(plugin => findPlugin(plugin))
        case _ => Nil
      }
      case _ => Nil
    }
  }

  private def findPlugin(plugin: Plugin) = {
    PluginEnum.values().find(p =>
      p.getArtifactId == plugin.artifactId.getOrElse(""))
  }
}
