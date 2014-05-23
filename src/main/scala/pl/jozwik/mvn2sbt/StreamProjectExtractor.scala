package pl.jozwik.mvn2sbt

import com.typesafe.scalalogging.slf4j.StrictLogging
import scala.util.{Failure, Success, Try}

object StreamProjectExtractor extends StrictLogging{
  final val PROJECT_START: String = "--- maven-dependency-plugin:"
  final val INFO = "[INFO] "
  final val START_DEPENDENCY = "+- "
  final val PROJECT_END = "------------------------------"
  final val BLANK_LINE = true

  def parseProjectLine(line: String) = {
    toOrderedTuple((0,1,2,3),line.split(":"))
  }

  def parseDependencyLine(line: String) = {
    val text =  line.split("\\s+")(1)
    val array = text.split(":")
    val (g, a, v, s) = if (array.length == 5) {
      (0, 1, 3, 4)
    } else {
      (0, 1, 4, 5)
    }
    toOrderedTuple((g, a, v, s),array)
  }

  private def toOrderedTuple(order:(Int,Int,Int,Int),array:Array[String]) = {
    val (a,b,c,d) = order
    (array(a), array(b), array(c), array(d))
  }

  def addToAcc(acc: (Seq[Project], Seq[Dependency], Boolean, Boolean), line: String): (Seq[Project], Seq[Dependency], Boolean, Boolean) = {
    val (projects, dependencies, started, blankLine) = acc
    if (line.startsWith(PROJECT_START)) {
      (projects, dependencies, true, false)
    } else if (line.startsWith(START_DEPENDENCY)) {
      val (groupId, artifactId, versionId, scope) = parseDependencyLine(line)
      val sc = Try(Scope.valueOf(scope)) match {
        case Success(el) => el
        case Failure(exp) =>
          logger.error(line)
          throw exp
      }
      val dependency = Dependency(MavenDependency(groupId, artifactId, versionId), sc)
      (projects, dependency +: dependencies, false, false)
    } else if (line.startsWith(PROJECT_END) && blankLine) {
      val project = Try(projects.head) match{
        case Success(p) => p
        case Failure(th) =>
          logger.error(line)
          throw th
      }
      (project.copy(dependencies = dependencies) +: projects.tail, Seq(), false, false)
    } else if (started) {
      val (groupId, artifactId, projectType, versionId) = parseProjectLine(line)
      val project = Project(MavenDependency(groupId, artifactId, versionId), ProjectType.valueOf(projectType))
      (project +: projects, Seq(), false, false)
    } else {
      (projects, dependencies, false, line.trim.isEmpty && projects.nonEmpty)
    }
  }
}

case class StreamProjectExtractor(private val iterator:TraversableOnce[String]) {
  import StreamProjectExtractor._
  val (projects, _, _, _) = iterator.flatMap { line =>
    if (line.startsWith(INFO)) {
      Some(line.substring(INFO.length))
    } else {
      None
    }
  }.foldLeft((Seq[Project](), Seq[Dependency](), false, false))(addToAcc)
}
