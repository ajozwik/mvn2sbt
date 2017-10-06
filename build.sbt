import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scoverage.ScoverageKeys

import scalariform.formatter.preferences.{SpacesAroundMultiImports, AlignSingleLineCaseStatements}

name := "mvn2sbt"

organization in ThisBuild := "pl.jozwik"

scalaVersion in ThisBuild := "2.11.11"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature","-Yrangepos")

scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature","-Yrangepos")

scalacOptions in Test ++= Seq("-Yrangepos")

releaseSettings

val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

val scalacheck = "org.scalacheck" %% "scalacheck" % "1.13.5"



libraryDependencies in ThisBuild ++= Seq(
  scalaLogging,
  scalacheck % "test",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6",
  "commons-io" % "commons-io" % "2.5"
)



lazy val `genscalaxb` = projectName("genscalaxb", "genscalaxb").settings(
  scalaxbPackageNames in (Compile, scalaxb) := Map(new URI("https://github.com/ajozwik/mvn2sbt") -> "pl.jozwik.gen",
    new URI("http://maven.apache.org/POM/4.0.0") -> "org.maven"),
  ScoverageKeys.coverageExcludedPackages:= "org.maven.*;pl.jozwik.gen.*;scalaxb.*"
)
.enablePlugins(ScalaxbPlugin)

lazy val `converter` = projectName("converter", "converter").enablePlugins(PackPlugin)
  .settings(packMain := Map("mvn2sbt" -> "pl.jozwik.mvn2sbt.Mvn2Sbt"))
  .dependsOn(`genscalaxb`)




def projectName(name: String, path: String): Project = Project(name, file(path)).settings(
  SbtScalariform.scalariformSettings,
  publishArtifact in(Compile, packageDoc) := false,
  sources in(Compile, doc) := Seq.empty,
  scalariformSettings,
  scapegoatVersion := "1.3.1",
  scapegoatIgnoredFiles := Seq(".*/target/.*"),
  ScalariformKeys.preferences := ScalariformKeys.preferences.value.
    setPreference(AlignSingleLineCaseStatements, true).
    setPreference(SpacesAroundMultiImports, false)
)


