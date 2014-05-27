package pl.jozwik.mvn2sbt;


public enum PluginEnum {
    GROOVY("gmaven-plugin", "org.softnetwork.sbt.plugins.GroovyPlugin.groovy.settings: _*",
             "addSbtPlugin(\"org.softnetwork.sbt.plugins\" % \"sbt-groovy\" % \"0.1\")",
            "resolvers += \"Biblio\" at \"http://mirrors.ibiblio.org/maven2\"");
    private final String sbtSetting;
    private final String artifactId;
    private final String plugin;
    private final String extraRepository;

    private PluginEnum(String artifactId, String sbtSetting,String plugin,String extraRepository) {
        this.artifactId = artifactId;
        this.sbtSetting = sbtSetting;
        this.plugin = plugin;
        this.extraRepository = extraRepository;
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
}
