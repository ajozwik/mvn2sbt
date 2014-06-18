package pl.jozwik.mvn2sbt

import java.io.File

class Mvn2SbtSpec extends AbstractSpec {

  import StreamProjectExtractor._

  "Mvn2Sbt " should {

    "Parse project line" in {
      val g = "groupId"
      val v = "1.0-GEN"
      val a = "artifactId"
      val t = ProjectType.jar.name()
      val projectLine = s"$g:$a:$t:$v"
      val parsed = parseProjectLine(projectLine)
      parsed should be(g, a, t, v, false)
    }

    "Parse dependency line without test-jar" in {
      testLine("org.apache.httpcomponents", "httpclient", "4.2.3", "compile", (g, a, v, s) => s"+- $g:$a:jar:$v:$s")
    }

    "Parse dependency line with test-jar" in {
      testLine("org.apache.httpcomponents", "httpclient", "4.2.3", "test", (g, a, v, s) => s"+- $g:$a:test-jar:tests:$v:$s")
    }


    "Expects parameters" in {
      intercept[IllegalArgumentException] {
        Mvn2Sbt.main(Array())
      }
    }

    "Wrong maven project root directory" in {
      intercept[IllegalStateException] {
        Mvn2Sbt.scanHierarchy(new File("target"))
      }
    }

//    "Wrong pom file" in {
//      intercept[RuntimeException] {
//        Mvn2Sbt.scanHierarchy(new File(TestConstants.EXAMPLES_PROJECTS,"brokenPom"))
//      }
//    }

    "Wrong pom file" in {
      intercept[RuntimeException] {
        Mvn2Sbt.main(Array(new File(TestConstants.EXAMPLES_PROJECTS,"brokenPom").getAbsolutePath))
      }
    }

  }

  private def testLine(g: String, a: String, v: String, s: String, f: (String, String, String, String) => String) = {
    val line = f(g, a, v, s)
    val res = parseDependencyLine(line)
    val (groupId, artifactId, versionId, scope, _) = res
    (groupId, artifactId, versionId, scope) should be(g, a, v, s)
  }

}
