package pl.jozwik.mvn2sbt

import com.typesafe.scalalogging.slf4j.StrictLogging
import scala.util.{Failure, Success, Try}

object StreamProjectExtractor extends StrictLogging {
  final val PROJECT_START: String = "--- maven-dependency-plugin:"
  final val INFO = "[INFO] "
  final val START_DEPENDENCY = "+- "

  def parseProjectLine(line: String) = {
    toOrderedTuple((0, 1, 2, 3,false), line.split(":"))
  }

  def parseDependencyLine(line: String) = {
    val text = line.split("\\s+")(1)
    val array = text.split(":")
    val (g, a, v, s,tests) = if (array.length == 5) {
      (0, 1, 3, 4,false)
    } else {
      (0, 1, 4, 5,true)
    }
    toOrderedTuple((g, a, v, s,tests), array)
  }

  private def toOrderedTuple(order: (Int, Int, Int, Int, Boolean), array: Array[String]) = {
    val (a, b, c, d,tests) = order
    (array(a), array(b), array(c), array(d),tests)
  }


  private def addProject(line: String, projects: Seq[Project]): (Seq[Project], Boolean) = {
    val (groupId, artifactId, projectType, versionId,_) = parseProjectLine(line)
    val project = Project(MavenDependency(groupId, artifactId, versionId), ProjectType.valueOf(projectType))
    (project +: projects, false)
  }

  private[mvn2sbt] def addDependency(line: String, projects: Seq[Project]): (Seq[Project], Boolean) = {
    val (groupId, artifactId, versionId, scope,tests) = parseDependencyLine(line)
    val sc = Try(Scope.valueOf(scope)) match {
      case Success(el) => el
      case Failure(exp) =>
        logger.error(line)
        throw exp
    }
    val dependency = Dependency(MavenDependency(groupId, artifactId, versionId), sc,tests)
    val project = projects.head
    val projectWithNewDependency = project.copy(dependencies = project.dependencies + dependency )
    (projectWithNewDependency +: projects.tail, false)
  }
}

case class StreamProjectExtractor(private val iterator: TraversableOnce[String]) {

  import StreamProjectExtractor._

  val projects = {
    val (p, _) = cutInfo.foldLeft((Seq[Project](), false)) {
      (acc: (Seq[Project], Boolean), line: String) =>
        val (accProjects, started) = acc
        handleLine(accProjects, line, started)
    }
    p.reverse
  }

  private def handleLine(accProjects: Seq[Project], line: String, started: Boolean): (Seq[Project], Boolean) = {
    if (line.startsWith(PROJECT_START)) {
      (accProjects, true)
    } else if (line.startsWith(START_DEPENDENCY)) {
      addDependency(line, accProjects)
    } else if (started) {
      addProject(line, accProjects)
    } else {
      (accProjects, false)
    }
  }

  private def cutInfo = iterator.flatMap {
    line =>
      if (line.startsWith(INFO)) {
        Some(line.substring(INFO.length))
      } else {
        None
      }
  }
}
