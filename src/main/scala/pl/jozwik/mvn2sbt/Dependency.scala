package pl.jozwik.mvn2sbt

import java.io.File
import org.maven.Plugin

case class MavenDependency(groupId: String, artifactId: String, versionId: String)

case class Dependency(mavenDependency: MavenDependency, scope: Scope, classifierTests: Boolean = false)

case class Project(mavenDependency: MavenDependency, projectType: ProjectType, dependencies: Seq[Dependency] = Seq(), path: Option[File] = None)

case class ProjectInformation(projectPath: File, parent: Option[MavenDependency], resolvers: Set[String], plugins: (PluginEnum, Plugin)*)