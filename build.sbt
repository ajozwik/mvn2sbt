import ScalaxbKeys._

packSettings

packMain := Map("mvn2sbt" -> "pl.jozwik.mvn2sbt.Mvn2Sbt")

incOptions := incOptions.value.withNameHashing(true)

name := "mvn2sbt"

organization := "pl.jozwik"

version := "0.2.0"

scalaVersion  := "2.11.1"

scalacOptions ++= Seq( "-deprecation", "-unchecked", "-feature")

scalacOptions in Test ++= Seq("-Yrangepos")

libraryDependencies in Global ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2",
  "org.scalacheck" %% "scalacheck" % "1.11.4" % "test",
  "org.scalatest" %% "scalatest" % "2.1.7" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1",
  "commons-io" % "commons-io" % "2.4"
)

scalaxbSettings

sourceGenerators in Compile <+= scalaxb in Compile

packageName in scalaxb in Compile := "org.maven"

val readPluginSbt = taskKey[String]("Read plugins.sbt file.")

readPluginSbt := {
	val lineIterator = scala.io.Source.fromFile(new java.io.File("project","plugins.sbt")).getLines
        val linesWithValIterator = lineIterator.filter(line => line.contains("scalaxbVersion"))
        val versionString =  linesWithValIterator.mkString("\n").split("=")(1).trim
        val version = versionString.split("\n")(0) // only val declaration
	println(version)
	version
}

