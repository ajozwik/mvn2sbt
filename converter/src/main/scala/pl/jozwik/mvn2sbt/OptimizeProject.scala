package pl.jozwik.mvn2sbt

import com.typesafe.scalalogging.LazyLogging

object OptimizeProject extends LazyLogging{


  def optimizeProjects(sbtProjectsMap: Map[MavenDependency, SbtProjectContent]): Map[MavenDependency, SbtProjectContent] = {
    val optimizedMap = sbtProjectsMap.map {
      case (name, content) =>
        val optimized = OptimizeProject.optimizeProject(sbtProjectsMap, content)
        if (optimized != content) {
          (name, optimized)
        } else {
          (name, content)
        }
    }
    if (optimizedMap == sbtProjectsMap) {
      optimizedMap
    } else {
      optimizeProjects(optimizedMap)
    }
  }

  def optimizeProject(sbtProjectsMap: Map[MavenDependency, SbtProjectContent], content: SbtProjectContent): SbtProjectContent = {
    val optimizedLibraries = removeDuplicatedLibraries(sbtProjectsMap, content)
    val optimizedDependsOn = removeDuplicatedDependsOn(content.project, content.dependsOn, sbtProjectsMap)
    content.copy(libraries = optimizedLibraries, dependsOn = optimizedDependsOn)
  }


  private def removeDuplicatedDependsOn(project: Project, dependsOn: Set[Dependency], sbtProjectsMap: Map[MavenDependency, SbtProjectContent]): Set[Dependency] = {
    val toRemove = dependsOn.flatMap {
      dependOn =>
        val setWithoutDep = dependsOn - dependOn

        dependOn.scope match {
          case Scope.test =>
            None
          case _ =>
            val projectContent = sbtProjectsMap(dependOn.mavenDependency)
            projectContent.dependsOn.find(d => setWithoutDep.contains(d) && d.scope != Scope.test)
        }
    }
    if (toRemove.isEmpty) {
      dependsOn
    } else {
      logger.debug(s"${project.projectDependency}: Remove $toRemove")
      removeDuplicatedDependsOn(project, dependsOn.diff(toRemove), sbtProjectsMap)
    }
  }



  private def removeDuplicatedLibraries(map: Map[MavenDependency, SbtProjectContent], content: SbtProjectContent): Set[Dependency] = {
    val parentDependencies = content.dependsOn.flatMap { dep =>
      if (dep.scope == Scope.compile) {
        map(dep.mavenDependency).libraries
      } else {
        Nil
      }
    }
    val parentDependenciesSet = parentDependencies.toSet
    val optimizedLibraries = content.libraries.filterNot {
      dep =>
        parentDependenciesSet.contains(dep) && dep.scope == Scope.compile && content.libraries.find(p => p.mavenDependency == dep.mavenDependency && p.scope != Scope.compile).isEmpty
    }
    optimizedLibraries
  }
}
