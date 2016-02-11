package pl.jozwik.mvn2sbt

import org.scalacheck.Prop
import org.scalacheck.Gen
import Prop.AnyOperators

class SbtContentSpec extends AbstractSpec {

  "SbtContentSpec " should {
    "Return None for empty list" in {
      SbtContent.resolversToOption(Set.empty) should not contain ","
    }

    "Replace not supported " in {
      Prop.forAll(Gen.oneOf(SbtContent.PROHIBITED_CHARS.toCharArray), Gen.alphaStr, Gen.alphaStr) {
        (prohibitedChar, header, tail) =>
          val text = s"$header$prohibitedChar$tail"
          val expected = s"${header}_$tail"
          SbtContent.changeNotSupportedSymbols(text) ?= expected
      }

    }
  }

}
