package pl.jozwik.mvn2sbt

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.maven.Plugin

import scala.collection.mutable

case class SbtProjectContent(
  project: Project,
  path: String,
  libraries: Set[Dependency],
  dependsOn: Set[Dependency],
  information: ProjectInformation,
  settings: Set[String]
)

object SbtContent {
  val SCALA_VERSION_IN_GLOBAL = "scala.version"
  val DEFAULT_SCALA_VERSION = "2.11.4"

  val PROHIBITED_CHARS = "."

  private[mvn2sbt] def resolversToOption(resolvers: Set[String]) = {

    val opt = if (resolvers.isEmpty) {
      None
    } else {
      val resolversString = resolvers.map { x =>
        s""" "$x" at "$x" """
      }.mkString(
        ",",
        """,
          """.stripMargin, ""
      )

      Some(resolversString)
    }
    s"""resolvers in Global ++= Seq(Resolver.mavenLocal${opt.getOrElse("")}""" + ")\n\n"
  }

  private[mvn2sbt] def changeNotSupportedSymbols(text: String) = {
    text.replaceAll(s"[$PROHIBITED_CHARS]", "_")
  }

  private def projectsToString(projectsMap: Map[MavenDependency, SbtProjectContent]) = {
    val sorted = projectsMap.toSeq.sortBy { case (d, _) => d.artifactId }
    sorted.foldLeft((Seq.empty[String], Map.empty[GroupArtifact, (MavenDependency, String)])) {
      case ((accSeqString, accMap), (_, content)) =>
        val (project, newMap) = createBuildSbt(content, accMap)
        (project +: accSeqString, newMap)
    }
  }

  private def createBuildSbt(sbtProjectContent: SbtProjectContent, cache: Map[GroupArtifact, (MavenDependency, String)]) = {
    val SbtProjectContent(project, path, libraries, dependsOn, information, settings) = sbtProjectContent

    val projectName = changeNotSupportedSymbols(project.projectDependency.artifactId)

    val (dependencies, newCache) = dependenciesToString(sort(libraries), cache)

    val dependsOnString = dependsOnToString(sort(dependsOn))

    val projectString = toProjectContent(project, path, settings, projectName, dependencies, dependsOnString)

    (projectString, newCache)
  }

  private def toProjectContent(project: Project, path: String, settings: Set[String], projectName: String, dependencies: String, dependsOnString: String) =
    if (path.isEmpty) {
      s"""
        |version := "${project.projectDependency.versionId}"
        |
        |name := "${project.projectDependency.artifactId}"
        |
        |organization := "${project.projectDependency.groupId}"
        |
        |libraryDependencies in Global ++= Seq($dependencies)
        |
        |${settings.mkString("", "\n\n", "\n")}
        |""".stripMargin
    } else {
      val settingString = settings.mkString(".settings(", ").\nsettings(", ")")
      s"""lazy val `$projectName` = ProjectName("$projectName","$path").settings(
      |  libraryDependencies ++= Seq($dependencies),
      |    name := "${project.projectDependency.artifactId}",
      |    version := "${project.projectDependency.versionId}",
      |    organization := "${project.projectDependency.groupId}"
      |)$settingString.dependsOn($dependsOnString)
      |
      |""".stripMargin
    }

  private def sort(set: Set[Dependency]): TraversableOnce[Dependency] =
    set.toIndexedSeq.sorted(Ordering.by[Dependency, (String, String)](x => (x.mavenDependency.groupId, x.mavenDependency.artifactId)))

  private def dependenciesToString(libraries: TraversableOnce[Dependency], cache: Map[GroupArtifact, (MavenDependency, String)]) = {
    val (depSeq, newCache) = libraries.foldLeft((Seq.empty[String], cache)) {
      case ((accLibSeq, accCache), d) =>
        val md = d.mavenDependency
        val groupArtifact = GroupArtifact(md.groupId, md.artifactId)
        val (alias, newAccCache) = createAliasForDependency(accCache, md, groupArtifact)

        val dependencyOption = toDependencyOption(d, alias)

        (dependencyOption.fold(accLibSeq)(l => l +: accLibSeq), newAccCache)
    }
    (depSeq.mkString("", ",\n   ", ""), newCache)
  }

  private def createAliasForDependency(accCache: Map[GroupArtifact, (MavenDependency, String)], md: MavenDependency, groupArtifact: GroupArtifact) =
    accCache.get(groupArtifact) match {
      case Some((mavenDep, l)) =>
        val res = VersionComparator.compare(mavenDep.versionId, md.versionId)
        if (res >= 0) {
          (l, accCache)
        } else {
          (l, accCache + (groupArtifact -> (md, l)))
        }
      case None =>
        val alias = s"""`${md.groupId}_${md.artifactId}`"""
        (alias, accCache + (groupArtifact -> (md, alias)))
    }

  private def toDependencyOption(d: Dependency, alias: String) = d.scope match {
    case Scope.system                           => None
    case Scope.compile                          => Some(alias)
    case test @ Scope.test if d.classifierTests => Some(s"""$alias % "$test" classifier "tests" """)
    case _                                      => Some(s"""$alias % "${d.scope}" """)
  }

  private def dependsOnToString(dependsOn: TraversableOnce[Dependency]) = dependsOn.map { d =>
    val test = d.scope match {
      case Scope.test => """% "test -> test" """
      case _          => ""
    }
    s"""`${changeNotSupportedSymbols(d.mavenDependency.artifactId)}`$test"""
  }.mkString(",")

}

case class SbtContent(private val projects: Seq[Project], private val hierarchy: Map[MavenDependency, ProjectInformation], private val rootDir: File)
    extends LazyLogging {

  import PluginConverter._
  import SbtContent._
  import OptimizeProject._

  val (sbtContent, pluginContent) = {

    val (contentOfPluginSbt, resolvers, projectContents, aliases) = projectsToStringCollections

    val defaultScalaVersion = System.getProperty(SCALA_VERSION_IN_GLOBAL, DEFAULT_SCALA_VERSION)
    val buildSbtWriter = new mutable.StringBuilder(s"""scalaVersion in Global := "$defaultScalaVersion" """).append("\n\n")
    buildSbtWriter.append("def ProjectName(name: String,path:String): Project =  Project(name, file(path))").append("\n\n")
    buildSbtWriter.append(resolversToOption(resolvers))
    aliases.foreach {
      case (_, (d, variable)) =>
        val dependency = s""""${d.groupId}" % "${d.artifactId}" % "${d.versionId}""""
        buildSbtWriter.append(s"val $variable = $dependency\n\n")
    }
    projectContents.foreach(buildSbtWriter.append)
    (buildSbtWriter.toString(), contentOfPluginSbt.mkString("\n\n"))
  }

  private def projectsToStringCollections: (Set[String], Set[String], Seq[String], Seq[(GroupArtifact, (MavenDependency, String))]) = {
    val (contentOfPluginSbt, resolvers, optimizedMaps) = handleProjects
    val (ps, libraries) = projectsToString(optimizedMaps)
    val aliases = libraries.toSeq.sortBy { case (d, _) => d.groupId }
    (contentOfPluginSbt, resolvers, ps, aliases)
  }

  private def handleProjects: (Set[String], Set[String], Map[MavenDependency, SbtProjectContent]) = {
    val (contentOfPluginSbt, resolvers, buildSbtProjects) =
      projects.foldLeft((Set.empty[String], Set.empty[String], Map.empty[MavenDependency, SbtProjectContent])) {
        case ((accContent, accResolvers, stbProjectContent), p) =>
          val (projectContent, pluginsSbt, resolvers) = handleProject(p)
          (accContent ++ pluginsSbt, accResolvers ++ resolvers, stbProjectContent + (p.projectDependency -> projectContent))
      }
    val optimizedMaps = optimizeProjects(buildSbtProjects)
    (contentOfPluginSbt, resolvers, optimizedMaps)
  }

  private def handleProject(p: Project): (SbtProjectContent, Set[String], Set[String]) = {
    val information = hierarchy(p.projectDependency)
    val relativePath = toRelativePath(information.projectPath, rootDir)
    val (dependsOn, libraries) = splitToDependsOnLibraries(p)
    val (settings, plugins, pluginDependencies) = handlePlugins(information.projectPath, information.plugins)
    val (pluginsDependsOnDependenciesSettings, pluginsDependsOnDependenciesSet) =
      DependencyToPluginConverter.addPluginForDependency(rootDir, information.projectPath, libraries)
    (
      SbtProjectContent(p, relativePath, pluginDependencies ++ libraries, dependsOn, information, pluginsDependsOnDependenciesSettings ++ settings),
      pluginsDependsOnDependenciesSet ++ plugins,
      information.resolvers
    )
  }

  private def splitToDependsOnLibraries(p: Project) = p.dependencies.partition {
    d =>
      val m = d.mavenDependency
      val contains = hierarchy.contains(m)
      val parentOption = hierarchy(p.projectDependency).parent
      val parentMatch = parentOption.fold(false) {
        parent =>
          parent == m
      }
      contains || parentMatch
  }

  private def handlePlugins(file: File, plugins: Seq[(PluginDescription, Plugin)]): (Set[String], Set[String], Set[Dependency]) = {
    plugins.foldLeft((Set.empty[String], Set.empty[String], Set.empty[Dependency])) { (tuple, p) =>
      val (accSett, accPlug, accPluginDependencies) = tuple
      val (pluginDescription, plugin) = p
      val endSettings = DependencyToPluginConverter.addSeqToArray(file.equals(rootDir))
      val settings = pluginDescription.sbtSetting.fold(accSett)(x => accSett + (x + endSettings))
      val customPluginSettings = pluginDescription.pomConfigurationToSbtConfiguration(rootDir, plugin)
      val plugins = accPlug + (pluginDescription.pluginsSbtPluginConfiguration, pluginDescription.extraRepository)
      (settings ++ customPluginSettings, plugins, accPluginDependencies ++ pluginDescription.dependencies)
    }
  }

}
