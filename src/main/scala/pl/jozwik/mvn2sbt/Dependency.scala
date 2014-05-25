package pl.jozwik.mvn2sbt

import java.io.File

case class MavenDependency(groupId:String,artifactId:String,versionId:String)

case class Dependency(mavenDependency:MavenDependency,scope:Scope)

case class Project(mavenDependency:MavenDependency,projectType:ProjectType,dependencies:Seq[Dependency]=Seq(),path:Option[File]=None)

case class FileParentDependency(file:File,parent:Option[MavenDependency])