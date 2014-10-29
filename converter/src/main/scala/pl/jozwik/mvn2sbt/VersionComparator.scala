package pl.jozwik.mvn2sbt

import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

object VersionComparator extends LazyLogging {

  def computeLarge(a: String, b: String): String = {
    val aa = tokenize(a)
    val bb = tokenize(b)
    if (computeLarge(aa, bb)) {
      a
    } else {
      b
    }
  }

  private def computeLarge(a: Seq[Either[String, Long]], b: Seq[Either[String, Long]]): Boolean = (a, b) match {
    case (Seq(), _) =>
      false
    case (_, Seq()) =>
      true
    case (h1 +: t1, h2 +: t2) =>
      val res = compare(h1, h2)
      if (res == 0) {
        computeLarge(t1, t2)
      } else {
        res > 0
      }

  }

  private def tokenize(a: String): Seq[Either[String, Long]] = {
    a.split("\\.").map(x => Try(x.toLong) match {
      case Success(i) =>
        Right(i)
      case Failure(th) =>
        logger.error("", th)
        Left(x)
    })
  }

  private def compare(a: Either[String, Long], b: Either[String, Long]) = (a, b) match {
    case (Right(x), Right(y)) =>
      x.compareTo(y)
    case (_, Left(y)) =>
      compareString(a.fold(x => x, i => i.toString), y)
    case (Left(x), _) =>
      compareString(x, b.fold(x => x, i => i.toString))
  }

  private def split(s: String): Long = {
    def split(s: String, acc: Seq[Int]): Long = {
      if (s.isEmpty) {
        toLong(acc)
      }
      else {
        val c = s.charAt(0)
        if (c >= '0' && c <= '9') {
          split(s.substring(1), (c - '0') +: acc)
        } else {
          toLong(acc)
        }
      }
    }
    split(s, Seq())
  }

  private def toLong(seq: Seq[Int]) = {
    val (l, _) = seq.foldLeft((0, 1)) { case ((acc, deep), el) => (acc + el * deep, deep * 10)}
    l
  }

  private def compareString(a: String, b: String) = {
    val x = split(a)
    val y = split(b)
    if (x > 0 && y > 0) {
      val res = x.compareTo(y)
      if (res == 0) {
        a.compareTo(b)
      } else {
        res
      }
    } else if (x > 0) {
      1
    } else if (y > 0) {
      -1
    } else {
      a.compareTo(b)
    }

  }

}
