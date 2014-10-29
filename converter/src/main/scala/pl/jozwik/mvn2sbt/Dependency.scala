package pl.jozwik.mvn2sbt

import java.io.File

import org.maven.Plugin

case class GroupArtifact(groupId: String, artifactId: String)

case class MavenDependency(groupId: String, artifactId: String, versionId: String)

case class Dependency(mavenDependency: MavenDependency, scope: Scope, classifierTests: Boolean = false)

case class Project(projectDependency: MavenDependency, projectType: ProjectType, dependencies: Set[Dependency] = Set.empty, path: Option[File] = None)

case class ProjectInformation(projectPath: File, parent: Option[MavenDependency], resolvers: Set[String], plugins: (PluginDescription, Plugin)*)