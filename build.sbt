import ScalaxbKeys._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scoverage.ScoverageKeys

import scalariform.formatter.preferences.{SpacesAroundMultiImports, AlignSingleLineCaseStatements}

name := "mvn2sbt"

organization in Global := "pl.jozwik"

version in Global := "0.5"

scalaVersion in Global := "2.11.8"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature","-Yrangepos")

scalacOptions in Global ++= Seq("-deprecation", "-unchecked", "-feature","-Yrangepos")

scalacOptions in Test ++= Seq("-Yrangepos")

releaseSettings

val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

val scalacheck = "org.scalacheck" %% "scalacheck" % "1.12.5"



libraryDependencies in Global ++= Seq(
  scalaLogging,
  scalacheck % "test",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "commons-io" % "commons-io" % "2.4"
)



lazy val `genscalaxb` = projectName("genscalaxb", "genscalaxb").settings(scalaxbSettings: _*).settings(
  packageNames in scalaxb in Compile := Map(new URI("https://github.com/ajozwik/mvn2sbt") -> "pl.jozwik.gen",
    new URI("http://maven.apache.org/POM/4.0.0") -> "org.maven"),
  sourceGenerators in Compile += (scalaxb in Compile).taskValue,
  ScoverageKeys.coverageExcludedPackages:= "org.maven.*;pl.jozwik.gen.*;scalaxb.*"
)


lazy val `converter` = projectName("converter", "converter").settings(xerial.sbt.Pack.packSettings: _*)
  .settings(packMain := Map("mvn2sbt" -> "pl.jozwik.mvn2sbt.Mvn2Sbt"))
  .dependsOn(`genscalaxb`)




def projectName(name: String, path: String): Project = Project(name, file(path)).settings(
  SbtScalariform.scalariformSettings,
  publishArtifact in(Compile, packageDoc) := false,
  sources in(Compile, doc) := Seq.empty,
  scalariformSettings,
  scapegoatVersion := "1.1.1",
  scapegoatIgnoredFiles := Seq(".*/target/.*"),
  ScalariformKeys.preferences := ScalariformKeys.preferences.value.
    setPreference(AlignSingleLineCaseStatements, true).
    setPreference(SpacesAroundMultiImports, false)
)


