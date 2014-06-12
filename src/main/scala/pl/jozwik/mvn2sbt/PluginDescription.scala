package pl.jozwik.mvn2sbt

import pl.jozwik.mvn2sbt.PluginConverter.PluginConverter

object PluginDescription {

  def apply(artifactId: String): Option[PluginDescription] = mapper.get(artifactId)

  private val mapper = Map("gmaven-plugin" -> PluginDescription(
    "gmaven-plugin",
    "org.softnetwork.sbt.plugins.GroovyPlugin.groovy.settings",
    "addSbtPlugin(\"org.softnetwork.sbt.plugins\" % \"sbt-groovy\" % \"0.1\")",
    "resolvers += \"Biblio\" at \"http://mirrors.ibiblio.org/maven2\"",
    Nil,
    PluginConverter.groovyConverter
  ), "maven-thrift-plugin" -> PluginDescription(
    "maven-thrift-plugin",
    "com.github.bigtoast.sbtthrift.ThriftPlugin.thriftSettings",
    "addSbtPlugin(\"com.github.bigtoast\" % \"sbt-thrift\" % \"0.7\")",
    "resolvers += \"bigtoast-github\" at \"http://bigtoast.github.com/repo/\"",
    List(new Dependency(new MavenDependency("commons-lang", "commons-lang", "2.6"), Scope.compile, false)),
    PluginConverter.thriftConverter
  ), "cxf-codegen-plugin" -> PluginDescription(
    "cxf-codegen-plugin",
    "cxf.settings",
    "addSbtPlugin(\"com.ebiznext.sbt.plugins\" % \"sbt-cxf-wsdl2java\" % \"0.1.2\")",
    "resolvers += \"Sonatype Repository\" at \"https://oss.sonatype.org/content/groups/public\"",
    Nil,
    PluginConverter.cxfConverter
  ), "maven-war-plugin" -> PluginDescription(
    "maven-war-plugin",
    "webSettings",
    "addSbtPlugin(\"com.earldouglas\" % \"xsbt-web-plugin\" % \"0.9.0\")",
    "",
    List(new Dependency(new MavenDependency("org.eclipse.jetty", "jetty-webapp", "9.1.0.v20131115"), Scope.container, false),
      new Dependency(new MavenDependency("org.eclipse.jetty", "jetty-plus", "9.1.0.v20131115"), Scope.container, false)),
    PluginConverter.warConverter
  ))

}

case class PluginDescription(
                              artifactId: String,
                              sbtSetting: String,
                              plugin: String,
                              extraRepository: String,
                              dependencies: List[Dependency],
                              pomConfigurationToSbtConfiguration: PluginConverter
                              )
