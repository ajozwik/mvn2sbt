package pl.jozwik.mvn2sbt
case class MavenDepedency(groupId:String,artifactId:String,versionId:String)

case class Dependency(mavenDepedency:MavenDepedency,scope:Scope)

case class Project(mavenDepedency:MavenDepedency,projectType:ProjectType,dependencies:Seq[Dependency]=Seq())