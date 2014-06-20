package pl.jozwik.mvn2sbt


class SbtContentSpec extends AbstractSpec {

  "SbtContentSpec " should {
    "Return None for empty list" in {
      SbtContent.resolversToOption(Set()).indexOf(",") should be (-1)
    }
  }

}
