package pl.jozwik.mvn2sbt

import org.maven.{Configuration4, Plugin}
import scala.xml.{NodeSeq, Node}
import scalaxb.DataRecord

import java.io.File

object PluginConverter {

  def toPath(path: File, rootDir: File): String = {
    val dir = path.getCanonicalPath
    val root = rootDir.getCanonicalPath
    val diff = dir.substring(root.length)
    if (diff.startsWith(File.separator)) {
      diff.substring(1)
    } else {
      diff
    }
  }

  def toPath(path: String, rootDir: File): String = toPath(new File(path), rootDir)

  type PluginConverter = (File, Plugin) => Set[String]

  val defaultConverter = (rootDir: File, plugin: Plugin) => Set.empty[String]


  def extractElement(confHead: Configuration4, name: String): Option[DataRecord[Any]] = {
    confHead.any.find { r =>
      r.key match {
        case Some(n) => n == name
        case _ => false
      }
    }
  }

  def extractNode(elem: Node, first: String, names: String*): NodeSeq = {
    val child = elem \ first
    names.foldLeft(child)((acc, name) => acc \ name)
  }

  def toKeySeqMap(elem: NodeSeq, key: String, buildSeq: (Node) => Seq[String], elements: String*): Map[String, Seq[String]] = {
    val nodeSeq = elements.foldLeft(elem)((acc, name) => acc \ name)
    toKeySeqMap(nodeSeq, key, buildSeq)
  }

  private def toKeySeqMap(nodeSeq: NodeSeq, key: String, buildSeq: (Node) => Seq[String]): Map[String, Seq[String]] =
    nodeSeq.foldLeft(Map.empty[String, Seq[String]]) {
      (acc, node) =>
        acc + (extractNode(node, key).text -> buildSeq(node))
    }

}


trait PomToSbtPluginConverter {
  final def convert(plugin: Plugin,rootDir:File): Set[String] = extractConfiguration(plugin) match {
    case (Some(confHead: Configuration4) :: tail) =>
      configurationToSet(confHead,rootDir)
    case _ => Set.empty
  }

  protected def configurationToSet(confHead: Configuration4,rootDir:File): Set[String]

  private[mvn2sbt] def extractConfiguration(plugin: Plugin) = {
    val execution = plugin.executions.get.execution
    execution.map { ex => ex.configuration}
  }

}
