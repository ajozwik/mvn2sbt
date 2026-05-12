package pl.jozwik.mvn2sbt

class StreamProjectExtractorSpec extends AbstractSpec {

  val notSupportedScope = "+- org.codehaus.janino:janino:ok:2.6.1:ok"

  "StreamProjectExtractorSpec " should {
    "Do not support ok scope" in {

      intercept[RuntimeException] {
        val fakeDep = MavenDependency("", "", "")
        StreamProjectExtractor.addDependency(notSupportedScope, Seq(Project(fakeDep, ProjectType.jar)))

      }
    }

    "Empty projects" in {
      StreamProjectExtractor.addDependency("", Seq.empty) shouldBe (Seq.empty, false)
    }
  }

}
