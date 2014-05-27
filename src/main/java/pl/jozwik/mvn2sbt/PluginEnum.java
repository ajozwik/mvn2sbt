package pl.jozwik.mvn2sbt;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum PluginEnum {
    GROOVY("gmaven-plugin", "org.softnetwork.sbt.plugins.GroovyPlugin.groovy.settings: _*",
            "addSbtPlugin(\"org.softnetwork.sbt.plugins\" % \"sbt-groovy\" % \"0.1\")",
            "resolvers += \"Biblio\" at \"http://mirrors.ibiblio.org/maven2\"", Collections.<Dependency>emptyList()),
    THRIFT("maven-thrift-plugin", "com.github.bigtoast.sbtthrift.ThriftPlugin.thriftSettings",
            "addSbtPlugin(\"com.github.bigtoast\" % \"sbt-thrift\" % \"0.7\")", "resolvers += \"bigtoast-github\" at \"http://bigtoast.github.com/repo/\"",
            Arrays.asList(new Dependency(new MavenDependency("commons-lang", "commons-lang", "2.6"),Scope.compile)));
    private final String sbtSetting;
    private final String artifactId;
    private final String plugin;
    private final String extraRepository;
    private final List<Dependency> dependencies;

    private PluginEnum(String artifactId, String sbtSetting, String plugin, String extraRepository, List<Dependency> dependencies) {
        this.artifactId = artifactId;
        this.sbtSetting = sbtSetting;
        this.plugin = plugin;
        this.extraRepository = extraRepository;
        this.dependencies = dependencies;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getSbtSetting() {
        return sbtSetting;
    }

    public String getPlugin() {
        return plugin;
    }

    public String getExtraRepository() {
        return extraRepository;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }
}
