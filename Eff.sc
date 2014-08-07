import scala.io.Source
import sys.process._
import java.io.File


if (args.length == 0) {
  println( """Call "scala Eff <rootDir1> [<rootDir2>]" """)
  sys.exit(-1)
} else {
  println( s"""You are called "scala Eff ${args.mkString(" ")}" """)
}

val parseModuleName = """<module>(.*)</module>""".r

def callEffectivePom(dir: File) {
  val effectivePom = "effective-pom.xml"
  val result = Process(Seq("mvn", "-N", "help:effective-pom", s"-Doutput=$effectivePom"), dir).!
  wrongResult(result, new File(dir, effectivePom))
  val pom = Source.fromFile(new File(dir, "pom.xml")).mkString
  (parseModuleName findAllIn pom).map {
    case parseModuleName(inside) => inside
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

  val dependencyTree = new File(rootDir, "dependencyTree.txt")

  println(s"Creating $dependencyTree.")

  val result = (Process(Seq("mvn", "dependency:tree"), rootDir) #> dependencyTree).!

  wrongResult(result, dependencyTree)

  println(s"$dependencyTree created")

  callEffectivePom(rootDir)

}


def wrongResult(result: Int, output: File) {
  if (result != 0) {
    println(s"See $output for errors")
    sys.exit(result)
  }
}