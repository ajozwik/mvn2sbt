resolvers += Resolver.sonatypeRepo("public")

resolvers += Classpaths.sbtPluginReleases

resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("org.scalaxb" % "sbt-scalaxb" % "1.5.2")

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")

addSbtPlugin("org.scoverage" % "sbt-coveralls" % "1.2.5")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")

addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.11")

addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.9")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

libraryDependencies += "org.scalariform" %% "scalariform" % "0.1.8"

//addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.9")

addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.12")
