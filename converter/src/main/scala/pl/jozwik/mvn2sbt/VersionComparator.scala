package pl.jozwik.mvn2sbt

import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

/** See http://docs.codehaus.org/display/MAVEN/Versioning
  */
object VersionComparator extends LazyLogging {

  def compare(a: String, b: String): Int = {
    val aa = tokenize(a)
    val bb = tokenize(b)
    compare(aa, bb)
  }

  private def compare(a: Seq[Either[String, Long]], b: Seq[Either[String, Long]]): Int = (a, b) match {
    case (Seq(), Seq()) =>
      0
    case (Seq(), Right(_) +: t) =>
      -1
    case (Seq(), Left(_) +: t) =>
      1
    case (Right(_) +: t, Seq()) =>
      1
    case (Left(_) +: t, Seq()) =>
      -1
    case (h1 +: t1, h2 +: t2) =>
      val res = compare(h1, h2)
      if (res == 0) {
        compare(t1, t2)
      }
      else {
        res
      }

  }

  private def tokenize(a: String): Seq[Either[String, Long]] = a
    .split("\\.|-")
    .toIndexedSeq
    .map(x =>
      Try(x.toLong) match {
        case Success(i) =>
          Right(i)
        case Failure(th) =>
          logger.trace(s"${th.getMessage}")
          Left(x)
      }
    )

  private def compare(a: Either[String, Long], b: Either[String, Long]) = (a, b) match {
    case (Right(x), Right(y)) =>
      x.compareTo(y)
    case (_, Left(y)) =>
      compareWithPrefix(a.fold(x => x, i => i.toString), y)
    case (Left(x), _) =>
      compareWithPrefix(x, b.fold(x => x, i => i.toString))
  }

  private def split(s: Seq[Char]): (Option[Long], Seq[Char]) = {
    def split(s: Seq[Char], acc: Seq[Int]): (Option[Long], Seq[Char]) = s match {
      case c +: t if c.toInt >= '0' && c.toInt <= '9' =>
        split(t, (c.toInt - '0') +: acc)
      case _ =>
        (toOptionLong(acc), s)
    }
    split(s, Seq.empty)
  }

  private def toOptionLong(seq: Seq[Int]): Option[Long] = {
    if (seq.isEmpty) {
      None
    }
    else {
      val (l, _) = seq.foldLeft((0, 1)) { case ((acc, deep), el) => (acc + el * deep, deep * 10) }
      Some(l)
    }
  }

  private def compareWithPrefix(a: String, b: String) = {
    val (optA, restA) = split(a)
    val (optB, restB) = split(b)
    (optA, optB) match {
      case (Some(x), Some(y)) =>
        val res = x.compareTo(y)
        if (res == 0) {
          compareNonDigitsStrings(restA, restB)
        }
        else {
          res
        }
      case (Some(x), _) =>
        1
      case (_, Some(y)) =>
        -1
      case _ =>
        compareNonDigitsStrings(restA, restB)
    }
  }

  private def compareNonDigitsStrings(a: Seq[Char], b: Seq[Char]): Int = (a, b) match {
    case (Seq(), Seq()) =>
      0
    case (Seq(), _) =>
      1
    case (_, Seq()) =>
      -1
    case _ =>
      String.copyValueOf(a.toArray).compareTo(String.copyValueOf(b.toArray))
  }

}
