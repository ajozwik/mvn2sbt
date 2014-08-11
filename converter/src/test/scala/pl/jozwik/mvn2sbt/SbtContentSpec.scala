package pl.jozwik.mvn2sbt


class SbtContentSpec extends AbstractSpec {

  "SbtContentSpec " should {
    "Return None for empty list" in {
      SbtContent.resolversToOption(Set.empty).indexOf(",") should be(-1)
    }
  }

}
