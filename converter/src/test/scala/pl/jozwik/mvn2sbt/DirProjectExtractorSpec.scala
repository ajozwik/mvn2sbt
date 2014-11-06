package pl.jozwik.mvn2sbt

import java.io.File

class DirProjectExtractorSpec extends AbstractSpec {

  val first = Some("ee")
  val second = Some("tt")
  "DirProjectExtractor " should {
    "Get first option " in {
      compare(DirProjectExtractor.valueFromOptions(first, second), first)
    }
    "Get first option2" in {
      compare(DirProjectExtractor.valueFromOptions(first, None), first)
    }

    "Get second option " in {
      val none = None
      compare(DirProjectExtractor.valueFromOptions(none, second), second)
    }

    "Throw exception " in {
      intercept[IllegalArgumentException] {
        DirProjectExtractor.valueFromOptions(None, None)
      }
    }

    "Empty pom file" in {
      DirProjectExtractor(new File(TestConstants.EXAMPLES_PROJECTS, "emptyPom")).projectsMap.forall {
        case (k, v) => v.resolvers.isEmpty
      } should be(true)
    }
    "Empty option " in {
     intercept[RuntimeException]{
       DirProjectExtractor.extractOption(None)
     }
    }
  }

  private def compare(str: String, option: Option[String]) {
    Some(str) should be(option)
  }

}
