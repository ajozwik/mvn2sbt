import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scoverage.ScoverageKeys

import scalariform.formatter.preferences.{SpacesAroundMultiImports, AlignSingleLineCaseStatements}

name := "mvn2sbt"

organization in ThisBuild := "pl.jozwik"

scalaVersion in ThisBuild := "2.12.9"

scapegoatVersion in ThisBuild := "1.3.10"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature","-Yrangepos")

scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature","-Yrangepos")

scalacOptions in Test ++= Seq("-Yrangepos")


val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

val scalacheck = "org.scalacheck" %% "scalacheck" % "1.14.0"



libraryDependencies in ThisBuild ++= Seq(
  scalaLogging,
  scalacheck % "test",
  "org.scalatest" %% "scalatest" % "3.0.8" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  "commons-io" % "commons-io" % "2.6"
)



lazy val `genscalaxb` = projectName("genscalaxb", "genscalaxb").settings(
  scalaxbPackageNames in (Compile, scalaxb) := Map(new URI("https://github.com/ajozwik/mvn2sbt") -> "pl.jozwik.gen",
    new URI("http://maven.apache.org/POM/4.0.0") -> "org.maven"),
  ScoverageKeys.coverageExcludedPackages:= "org.maven.*;pl.jozwik.gen.*;scalaxb.*"
)
.enablePlugins(ScalaxbPlugin)

lazy val `converter` = projectName("converter", "converter")
  .enablePlugins(PackPlugin)
  .settings(packMain := Map("mvn2sbt" -> "pl.jozwik.mvn2sbt.Mvn2Sbt"))
  .dependsOn(`genscalaxb`)




def projectName(name: String, path: String): Project = Project(name, file(path)).settings(
  scalariformAutoformat := true,
  publishArtifact in(Compile, packageDoc) := false,
  sources in(Compile, doc) := Seq.empty,
  scapegoatIgnoredFiles := Seq(".*/target/.*"),
  scalariformPreferences := scalariformPreferences.value.
    setPreference(AlignSingleLineCaseStatements, true).
    setPreference(SpacesAroundMultiImports, false)
)


