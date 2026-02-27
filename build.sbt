import scoverage.ScoverageKeys

name := "mvn2sbt"

ThisBuild / organization := "pl.jozwik"

ThisBuild / scalaVersion     := "2.13.18"
ThisBuild / scapegoatVersion := "3.3.2"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Yrangepos")

ThisBuild / scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xsource:3") ++ (CrossVersion.partialVersion(
  scalaVersion.value
) match {
  case Some((2, _)) =>
    Seq("-Yrangepos")
  case _ =>
    Seq()
})

Test / scalacOptions ++= Seq("-Yrangepos")

val scalaTestVersion = "3.2.19"

val scalaLogging                        = "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.6"
val `org.scalatestplus_scalacheck-1-19` = "org.scalatestplus"          %% "scalacheck-1-19" % s"$scalaTestVersion.0" % "test"
//val scalacheck                          = "org.scalacheck"             %% "scalacheck"      % "1.19.0"
val `javax.xml.bind_javax.xml.bind-api` = "javax.xml.bind" % "jaxb-api" % "2.3.1"

ThisBuild / libraryDependencies ++= Seq(
  `javax.xml.bind_javax.xml.bind-api`,
  scalaLogging,
//  scalacheck                % "test",
  `org.scalatestplus_scalacheck-1-19`,
  "org.scalatest"          %% "scalatest"                % scalaTestVersion % "test",
  "ch.qos.logback"          % "logback-classic"          % "1.3.16",
  "org.scala-lang.modules" %% "scala-xml"                % "2.4.0",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0",
  "commons-io"              % "commons-io"               % "2.21.0"
)

lazy val `genscalaxb` = projectName("genscalaxb", "genscalaxb")
  .settings(
    Compile / scalaxb / scalaxbPackageNames := Map(
      new URI("https://github.com/ajozwik/mvn2sbt") -> "pl.jozwik.gen",
      new URI("http://maven.apache.org/POM/4.0.0")  -> "org.maven"
    ),
    ScoverageKeys.coverageExcludedPackages := "org.maven.*;pl.jozwik.gen.*;scalaxb.*"
  )
  .enablePlugins(ScalaxbPlugin)

lazy val `converter` = projectName("converter", "converter")
  .enablePlugins(PackPlugin)
  .settings(packMain := Map("mvn2sbt" -> "pl.jozwik.mvn2sbt.Mvn2Sbt"))
  .dependsOn(`genscalaxb`)

def projectName(name: String, path: String): Project = Project(name, file(path)).settings(
  Compile / packageDoc / publishArtifact := false,
  Compile / doc / sources                := Seq.empty,
  scapegoatIgnoredFiles                  := Seq(".*/target/.*")
)
