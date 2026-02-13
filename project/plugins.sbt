resolvers += Resolver.sonatypeRepo("public")

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.2.13")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")
addSbtPlugin("org.scalameta"   % "sbt-scalafmt"       % "2.5.6")
addSbtPlugin("org.scalaxb" % "sbt-scalaxb" % "1.12.4")
addSbtPlugin("org.scoverage"   % "sbt-coveralls"      % "1.3.15")
addSbtPlugin("org.scoverage"   % "sbt-scoverage"      % "2.4.2")
addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.23")
