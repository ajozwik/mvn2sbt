package pl.jozwik.mvn2sbt

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import java.io.File

abstract class AbstractSpec extends AnyWordSpecLike with Matchers with LazyLogging {
  protected val target = new File("target")
}
