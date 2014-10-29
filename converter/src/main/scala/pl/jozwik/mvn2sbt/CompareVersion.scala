package pl.jozwik.mvn2sbt

import scala.util.{Failure, Success, Try}

object CompareVersion {
  def computeLarge(a: String, b: String): String = {
    val aa = toIntSeq(a)
    val bb = toIntSeq(b)
    if (computeLarge(aa, bb)) {
      a
    } else {
      b
    }
  }

  private def computeLarge(a: Seq[Int], b: Seq[Int]): Boolean = (a, b) match {
    case (Seq(), _) =>
      false
    case (_, Seq()) =>
      true
    case (h1 +: t1, h2 +: t2) if h1 == h2 =>
      computeLarge(t1, t2)
    case (h1 +: _, h2 +: _) =>
      h1 > h2
  }

  private def toIntSeq(a: String) = {
    a.split("\\.").map(x => Try(x.toInt) match {
      case Success(i) =>
        i
      case Failure(th) =>
        logger.error("", th)
        0
    })
  }

}
