package pl.jozwik.mvn2sbt

import com.typesafe.scalalogging.StrictLogging
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

import java.io.File

abstract class AbstractSpec extends AnyWordSpecLike with Matchers with StrictLogging with ScalaCheckDrivenPropertyChecks {
  protected val target = new File("target")
}
