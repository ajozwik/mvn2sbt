package pl.jozwik.mvn2sbt

import java.io.File

import com.typesafe.scalalogging.slf4j.StrictLogging


object DependencyConverter extends StrictLogging {

  val TESTNG_XML: String = "src/test/resources/testng.xml"

  def addSeqToArray(equal: Boolean) =
    if (equal) {
      ""
    } else {
      " :_* "
    }


  def convert(rootPath: File, path: File, dependencies: Seq[Dependency]) = dependencies.foldLeft((Set[String](), Set[String]())) { (tuple, dependency) =>
    dependency.mavenDependency match {
      case MavenDependency("org.testng", _, _) =>
        val endSettings = addSeqToArray(path.equals(rootPath))
        val relativePath = PluginConverter.toPath(path, rootPath)
        val rp = if (relativePath.nonEmpty) {
          relativePath + "/" + TESTNG_XML
        } else {
          TESTNG_XML
        }
        logger.info("add {} to project", rp)
        (tuple._1 +(s"de.johoop.testngplugin.TestNGPlugin.testNGSettings$endSettings", s"""testNGSuites := Seq[String]("$rp")"""),
          tuple._2 + """addSbtPlugin("de.johoop" % "sbt-testng-plugin" % "3.0.0")""")
      case _ => tuple
    }
  }


}


