package pl.jozwik.mvn2sbt.pom2sbt

import org.scalacheck.Gen._
import org.scalacheck.Prop._
import pl.jozwik.mvn2sbt.AbstractSpec

class CxfPluginConverterSpec extends AbstractSpec {

  "CxfPluginConverterSpec " should {

    "Remove " in {
      forAll(alphaStr, alphaStr, listOf(alphaStr), listOf(alphaStr)) { (toRemove, ignored, header, tail) =>
        (ignored.nonEmpty && toRemove.nonEmpty) ==> {
          val seq    = header ++ Seq(ignored, toRemove) ++ tail
          val result = CxfPluginConverter.remove(seq, ignored)
          result ?= (header ++ tail)
        }
      }
    }

  }

}
