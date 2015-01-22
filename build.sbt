import ScalaxbKeys._

incOptions := incOptions.value.withNameHashing(true)

name := "mvn2sbt"

organization in Global := "pl.jozwik"

version in Global := "0.4"

scalaVersion in Global := "2.11.5"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature","-Yrangepos")

scalacOptions in Global ++= Seq("-deprecation", "-unchecked", "-feature","-Yrangepos")

scalacOptions in Test ++= Seq("-Yrangepos")

releaseSettings

val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

val scalacheck = "org.scalacheck" %% "scalacheck" % "1.12.1"

libraryDependencies in Global ++= Seq(
  scalaLogging,
  scalacheck % "test",
  "org.scalatest" %% "scalatest" % "2.2.3" % "test",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.3",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
  "commons-io" % "commons-io" % "2.4"
)



lazy val `genscalaxb` = ProjectName("genscalaxb", "genscalaxb").settings(scalaxbSettings: _*).settings(
  packageNames in scalaxb in Compile := Map(new URI("https://github.com/ajozwik/mvn2sbt") -> "pl.jozwik.gen",
    new URI("http://maven.apache.org/POM/4.0.0") -> "org.maven"),
  sourceGenerators in Compile <+= scalaxb in Compile
)


lazy val `converter` = ProjectName("converter", "converter").settings(CoverallsPlugin.coverallsSettings: _*).settings(xerial.sbt.Pack.packSettings: _*)
  .settings(packMain := Map("mvn2sbt" -> "pl.jozwik.mvn2sbt.Mvn2Sbt"))
  .dependsOn(`genscalaxb`)




def ProjectName(name: String, path: String) = Project(name, file(path))


