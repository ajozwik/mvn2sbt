package pl.jozwik.mvn2sbt

import java.io.File

import com.typesafe.scalalogging.StrictLogging


object DependencyToPluginConverter extends StrictLogging {

  val TESTNG_XML = "src/test/resources/testng.xml"

  def addSeqToArray(equal: Boolean) =
    if (equal) {
      ""
    } else {
      " :_* "
    }


  def addPluginForDependency(rootPath: File, path: File, dependencies: Set[Dependency]) = dependencies.foldLeft((Set[String](), Set[String]())) {
    (tuple, dependency) =>
      dependency.mavenDependency match {
        case MavenDependency("org.testng", _, _) =>
          val endSettings = addSeqToArray(path.equals(rootPath))
          val relativePath = PluginConverter.toRelativePath(path, rootPath)
          val rp = if (relativePath.nonEmpty) {
            relativePath + "/" + TESTNG_XML
          } else {
            TESTNG_XML
          }
          logger.info("add {} to project", rp)
          (tuple._1 +(s"de.johoop.testngplugin.TestNGPlugin.testNGSettings$endSettings", s"""testNGSuites := Seq("$rp")"""),
            tuple._2 + """addSbtPlugin("de.johoop" % "sbt-testng-plugin" % "3.0.2")""")
        case _ => tuple
      }
  }


}


