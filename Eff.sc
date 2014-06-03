import scala.io.Source
import sys.process._
import java.io.File

if (args.length == 0) {
  println( """Call "scala Eff.scala <rootDir1> <rootDir2>" """)
  sys.exit(-1)
} else {
  println( s"""You are called "scala Eff.scala ${args.mkString(" ")}" """)
}

val r = """<module>(.*)</module>""".r
def callEffectivePom(dir: File) {
  Process(Seq("mvn","-N", "help:effective-pom", "-Doutput=effective-pom.xml"), dir).!
  val pom = Source.fromFile(new File(dir, "pom.xml")).mkString
  (r findAllIn pom).map {
    case r(inside) => inside
  }.foreach {
    f =>
      val child = new File(dir, f)
      if (child.exists()) {
        callEffectivePom(child)
      }
  }

}

args.foreach { root =>

  val rootDir = new File(root)

  val inputTxt = new File(rootDir, "dependencyTree.txt")

  println(s"Create $inputTxt.")

  (Process(Seq("mvn", "dependency:tree"), rootDir) #> inputTxt).!

  println(s"$inputTxt created")

  callEffectivePom(rootDir)

}