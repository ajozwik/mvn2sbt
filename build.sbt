import ScalaxbKeys._

packSettings

packMain := Map("mvn2sbt" -> "pl.jozwik.mvn2sbt.Mvn2Sbt")

incOptions := incOptions.value.withNameHashing(true)

name := "mvn2sbt"

organization in Global := "pl.jozwik"

version in Global := "0.3.0"

scalaVersion in Global := "2.11.1"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

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



lazy val `genscalaxb` = ProjectName("genscalaxb","genscalaxb").settings(scalaxbSettings :_* ).settings(
  packageName in scalaxb in Compile := "org.maven",
  sourceGenerators in Compile <+= scalaxb in Compile
)


lazy val `converter` = ProjectName("converter","converter").settings(
  instrumentSettings :_*
).dependsOn(`genscalaxb`)








def ProjectName(name: String,path:String): Project =  Project(name, file(path))


