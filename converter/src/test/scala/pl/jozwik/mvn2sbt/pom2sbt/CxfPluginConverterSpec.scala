package pl.jozwik.mvn2sbt.pom2sbt

import org.scalatest.prop.Checkers
import pl.jozwik.mvn2sbt.AbstractSpec

class CxfPluginConverterSpec extends AbstractSpec with Checkers{
  "CxfPluginConverterSpec " should {
    "Remove " in {
      check((header: List[String], tail: List[String]) =>{
        val toRemove = "elemet"
        val ignored = Seq("kota")
        val seq = header ++ ignored ++ Seq(toRemove) ++ tail
        val result = CxfPluginConverter.remove(seq,ignored:_*)
        result == (header ++ tail)
      })



    }
  }
}
