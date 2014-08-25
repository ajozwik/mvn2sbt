import ScalaxbKeys._

incOptions := incOptions.value.withNameHashing(true)

name := "mvn2sbt"

organization in Global := "pl.jozwik"

version in Global := "0.3.1"

scalaVersion in Global := "2.11.2"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

scalacOptions in Test ++= Seq("-Yrangepos")

libraryDependencies in Global ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test",
  "org.scalatest" %% "scalatest" % "2.2.2" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.2",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2",
  "commons-io" % "commons-io" % "2.4"
)



lazy val `genscalaxb` = ProjectName("genscalaxb", "genscalaxb").settings(scalaxbSettings: _*).settings(
  packageNames in scalaxb in Compile := Map(new URI("https://github.com/ajozwik/mvn2sbt") -> "pl.jozwik.gen",
    new URI("http://maven.apache.org/POM/4.0.0") -> "org.maven"),
  sourceGenerators in Compile <+= scalaxb in Compile
)


lazy val `converter` = ProjectName("converter", "converter").settings(
  instrumentSettings: _*
).settings(CoverallsPlugin.coverallsSettings: _*).settings(xerial.sbt.Pack.packSettings: _*).settings(
    packMain := Map("mvn2sbt" -> "pl.jozwik.mvn2sbt.Mvn2Sbt")
  ).dependsOn(`genscalaxb`)



def ProjectName(name: String, path: String) = Project(name, file(path))


