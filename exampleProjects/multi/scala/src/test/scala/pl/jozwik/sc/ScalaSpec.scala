package pl.jozwik.sc

import pl.jozwik.cxf.{Cxf,CxfSpec}

class ScalaSpec{

  println(s"Hello world ${Cxf.CXF} ")

  new CxfSpec().testSpec()
}