resolvers += Resolver.sonatypeRepo("public")

resolvers += Resolvers.typesafeRepo("releases")

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scalaxb" % "sbt-scalaxb" % "1.4.0")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.0.3")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.7.5")

addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.4")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")

libraryDependencies += "org.scalariform" %% "scalariform" % "0.1.8"

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")
