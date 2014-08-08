package pl.jozwik.mvn2sbt

class LoadFileSpec extends AbstractSpec {
  "File " should {
    "Be loaded " in {
      val converters = MavenSbtPluginMapper.loadConverters()
      converters.foreach{case (k,v) => logger.debug(s"$k $v")}
    }
  }
}
