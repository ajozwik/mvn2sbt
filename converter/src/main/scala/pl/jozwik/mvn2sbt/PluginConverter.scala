package pl.jozwik.mvn2sbt

import java.io.File

import org.maven.{Configuration4, Plugin}
import pl.jozwik.mvn2sbt.PluginConverter._

import scala.xml.{Node, NodeSeq}
import scalaxb.DataRecord

object PluginConverter {

  def toRelativePath(path: File, rootDir: File): String = {
    val dir = path.getCanonicalPath
    val root = rootDir.getCanonicalPath
    val diff = dir.substring(root.length)
    if (diff.startsWith(File.separator)) {
      diff.substring(1)
    } else {
      diff
    }
  }

  def toPath(path: String, rootDir: File): String = toRelativePath(new File(path), rootDir)

  type PluginConverter = (File, Plugin) => Set[String]

  val defaultConverter = (rootDir: File, plugin: Plugin) => Set.empty[String]

  def findElement(confHead: Configuration4, name: String): Option[DataRecord[Any]] = {
    confHead.any.find { r =>
      r.key.fold(false) {
        n =>
          n == name
      }
    }
  }

  def findNode(elem: Node, first: String, names: String*): NodeSeq = {
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
        acc + (findNode(node, key).text -> buildSeq(node))
    }

}

trait PomToSbtPluginConverter {

  final def convert(plugin: Plugin, rootDir: File): Set[String] = findConfiguration(plugin) match {
    case (Some(confHead: Configuration4) +: tail) =>
      configurationToSet(confHead, rootDir)(plugin)
    case _ => Set.empty
  }

  protected def configurationToSet(confHead: Configuration4, rootDir: File)(implicit plugin: Plugin): Set[String]

  private[mvn2sbt] def findConfiguration(plugin: Plugin) = {
    plugin.executions match {
      case Some(executions) =>
        executions.execution.map { ex => ex.configuration }
      case None =>
        Seq.empty
    }
  }

  protected final def toOption(confHead: Configuration4, name: String, f: (String) => String) = {
    val node = findElement(confHead, name)
    val moduleName = node.map(v => ReflectionUtils.castTo[Node](v.value).text)
    moduleName.map(s => f(s))
  }

}
