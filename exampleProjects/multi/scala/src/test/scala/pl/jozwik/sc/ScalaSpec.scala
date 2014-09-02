package pl.jozwik.sc

import pl.jozwik.cxf.{Cxf,CxfSpec}
import pl.jozwik.cxf2.TestUtils

class ScalaSpec{

  println(s"Hello world ${Cxf.CXF} ${TestUtils.VERSION}")

  new CxfSpec().testSpec()
}