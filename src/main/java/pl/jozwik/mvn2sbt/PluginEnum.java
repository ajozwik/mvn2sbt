package pl.jozwik.mvn2sbt;


public enum PluginEnum {
    GROOVY("gmaven-plugin", "org.softnetwork.sbt.plugins.GroovyPlugin.groovy.settings: _*");
    private final String sbtSetting;
    private final String artifactId;

    private PluginEnum(String artifactId, String sbtSetting) {
        this.artifactId = artifactId;
        this.sbtSetting = sbtSetting;
    }

    public String getArtifactId() {
        return artifactId;
    }


    public String getSbtSetting() {
        return sbtSetting;
    }
}
