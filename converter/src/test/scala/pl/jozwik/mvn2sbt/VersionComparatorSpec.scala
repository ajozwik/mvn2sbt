package pl.jozwik.mvn2sbt

class VersionComparatorSpec extends AbstractSpec {

  private val seq = Seq(
    ("1.1.2", "1.1.2", 0),
    ("1.1.2", "1.2", -1),
    ("1.1.2", "1.2.0", -1),
    ("1.3-SNAPSHOT", "1.3-SNAPSHOT", 0),
    ("4","3.9.9.9",1),
    ("0.0.68-SNAPSHOT","0.0.67-SNAPSHOT",1),
    ("1","1.0.1",-1),
    ("1.0","1",1),
    ("1.01","1.02",-1)
  )


  "version " should {
    seq.foreach {
      case (a, b, result) =>
        s"compare versions $a $b $result" in {
          VersionComparator.compare(a, b) should equal(result)
        }
    }

    seq.foreach {
      case (a, b, result) =>
        s"invert $a $b $result" in {
          VersionComparator.compare(b, a) should equal(-result)
        }
    }

  }
}
