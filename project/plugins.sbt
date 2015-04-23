resolvers += Resolver.sonatypeRepo("public")

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scalaxb" % "sbt-scalaxb" % "1.3.0")

addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.6.2")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.4")

addSbtPlugin("com.sksamuel.scoverage" %% "sbt-coveralls" % "0.0.5")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")
