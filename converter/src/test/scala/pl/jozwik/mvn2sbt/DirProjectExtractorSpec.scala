package pl.jozwik.mvn2sbt

import java.io.File

class DirProjectExtractorSpec extends AbstractSpec {


  "DirProjectExtractor " should {
    "Get first option " in {
      val first = Some("ee")
      val second = Some("tt")
      DirProjectExtractor.valueFromOptions(first, second) === first.get
      DirProjectExtractor.valueFromOptions(first, None) === first.get
    }

    "Get second option " in {
      val first = None
      val second = Some("tt")
      DirProjectExtractor.valueFromOptions(first, second) === second.get
    }

    "Throw exception " in {
      intercept[IllegalArgumentException] {
        DirProjectExtractor.valueFromOptions(None, None)
      }
    }

    "Empty pom file" in {
      DirProjectExtractor(new File(TestConstants.EXAMPLES_PROJECTS, "emptyPom")).projectsMap.forall {
        case (k, v) => v.resolvers.isEmpty
      } should be (true)
    }

  }

}
