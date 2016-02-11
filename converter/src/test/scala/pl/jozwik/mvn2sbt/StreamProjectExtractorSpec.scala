package pl.jozwik.mvn2sbt

class StreamProjectExtractorSpec extends AbstractSpec {

  "StreamProjectExtractorSpec " should {
    "Do not support ok scope" in {
      val notSupportedScope = "+- org.codehaus.janino:janino:ok:2.6.1:ok"
      intercept[RuntimeException] {
        StreamProjectExtractor.addDependency(notSupportedScope, Seq())
      }
    }
  }

}

