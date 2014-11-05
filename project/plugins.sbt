resolvers += Resolver.sonatypeRepo("public")

resolvers += Classpaths.sbtPluginReleases

val scalaxbVersion = "1.1.2"

addSbtPlugin("org.scalaxb" % "sbt-scalaxb" % scalaxbVersion)

addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.6.2")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.9")

addSbtPlugin("com.sksamuel.scoverage" %% "sbt-coveralls" % "0.0.5")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")
