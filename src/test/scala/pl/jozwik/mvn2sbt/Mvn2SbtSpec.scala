package pl.jozwik.mvn2sbt

import org.scalatest.{Matchers, WordSpecLike}
import java.io.File
import com.typesafe.scalalogging.slf4j.LazyLogging
import java.nio.file.Paths

class Mvn2SbtSpec extends WordSpecLike with Matchers with LazyLogging {

  import Mvn2Sbt._
  import StreamProjectExtractor._

  "Mvn2Sbt " should {

    "Parse project line" in {
      val g = "groupId"
      val v = "1.0-GEN"
      val a = "artifactId"
      val t = ProjectType.jar.name()
      val projectLine = s"$g:$a:$t:$v"
      val parsed = parseProjectLine(projectLine)
      logger.debug("{}", parsed)
      parsed should be(g, a, t,v)
    }

    "Parse dependency line without test-jar" in {
      testLine("org.apache.httpcomponents", "httpclient", "4.2.3", "compile",(g,a,v,s)=>s"+- $g:$a:jar:$v:$s")
    }

    "Parse dependency line with test-jar" in {
      testLine("org.apache.httpcomponents", "httpclient", "4.2.3", "compile",(g,a,v,s)=>s"+- $g:$a:test-jar:tests:$v:$s")
    }


    "From file " in {
      val projects = projectsFromFile(new File("input.txt"))
      projects should not be Nil
    }


  }

  private def testLine(g:String,a:String,v:String,s:String,f:(String,String,String,String)=>String) = {
    val line = f(g,a,v,s)
    val res = parseDependencyLine(line)
    val (groupId, artifactId, versionId, scope) = res
    (groupId, artifactId, versionId, scope) should be(g, a, v, s)
  }

}
