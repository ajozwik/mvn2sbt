resolvers += Resolver.sonatypeRepo("public")

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scalaxb" % "sbt-scalaxb" % "1.4.0")

addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.7.5")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.1.0")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")
