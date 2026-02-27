import scala.io.Source
import scala.util.*
import sys.process.*
import java.io.File

val mavenCommand = "mvn"

val mvn =  Try(Process(Seq(mavenCommand,"-version")).!) match{
  case Success(_) =>
    "mvn"
  case Failure(th) =>
    "mvn.bat"
}





val parseModuleName = """<module>(.*)</module>""".r

def callEffectivePom(dir: File):Unit =  {
  val effectivePom = "effective-pom.xml"
  val result = Process(Seq(mvn, "-N", "help:effective-pom", s"-Doutput=$effectivePom"), dir).!
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

@main def main(args: String*): Unit = {
  println(s"Maven command is: $mvn")
  if (args.length == 0) {
    println("""Call "scala Eff.sc <rootDir1> [<rootDir2>]" """)
    sys.exit(-1)
  } else {
    println(s"""You are called "scala Eff ${args.mkString(" ")}" """)
  }

  args.foreach { root =>

    val rootDir = new File(root)

    val dependencyTree = new File(rootDir, "dependencyTree.txt")

    println(s"Creating $dependencyTree.")

    val result = (Process(Seq(mvn, "dependency:tree"), rootDir) #> dependencyTree).!

    wrongResult(result, dependencyTree)

    println(s"$dependencyTree created")

    callEffectivePom(rootDir)

  }
}

def wrongResult(result: Int, output: File):Unit = {
  if (result != 0) {
    println(s"See $output for errors, content below:\n\n\n\n")
    println("==================ERROR MESSAGE==========")
    scala.io.Source.fromFile(output).getLines().foreach(println)
    println("==================ERROR MESSAGE==========")
    sys.exit(result)
  }
}