def ProjectName(name: String,path:String): Project =  Project(name, file(path))

resolvers in Global ++= Seq(Resolver.mavenLocal,"http://snapshots.maven.codehaus.org/maven2" at "http://snapshots.maven.codehaus.org/maven2",
          "http://repo.maven.apache.org/maven2" at "http://repo.maven.apache.org/maven2")


version := "1.1.2"

name := "multi"

organization := "pl.jozwik"

libraryDependencies in Global ++= Seq("junit" % "junit" % "4.10" % "test")


lazy val `cxf2` = ProjectName("cxf2","cxf").settings(
  libraryDependencies ++= Seq("junit" % "junit" % "4.10" % "test",
   "org.apache.thrift" % "libthrift" % "0.9.1"),
    name := "cxf2",
    version := "1.0.0",
    organization := "pl.jozwik"
).settings(cxf.settings :_* ).settings(cxf.wsdls :=Seq(cxf.Wsdl(file("cxf/src/main/resources/axis2/Subs.wsdl"), Seq("-b","cxf/src/main/resources/axis2/binding.xml","-p","pl.jozwik.subs","-impl"), "cxf/src/main/resources/axis2/Subs.wsdl"),
	cxf.Wsdl(file("cxf/src/main/resources/axis2/Acc.wsdl"), Seq("-b","cxf/src/main/resources/axis2/binding.xml","-p","pl.jozwik.acc","-impl"), "cxf/src/main/resources/axis2/Acc.wsdl"))).dependsOn()

lazy val `thrift11` = ProjectName("thrift11","thrift1").settings(
  libraryDependencies ++= Seq("commons-lang" % "commons-lang" % "2.6",
   "junit" % "junit" % "4.10" % "test",
   "ch.qos.logback" % "logback-classic" % "1.1.2",
   "org.testng" % "testng" % "6.8.8" % "test",
   "commons-lang" % "commons-lang" % "2.6",
   "org.apache.thrift" % "libthrift" % "0.9.1"),
    name := "thrift11",
    version := "1.0.0",
    organization := "pl.jozwik"
).settings(de.johoop.testngplugin.TestNGPlugin.testNGSettings :_* ).settings(com.github.bigtoast.sbtthrift.ThriftPlugin.thriftSettings :_* ).dependsOn(`cxf2`).settings(testNGSuites := Seq[String]("thrift1/src/test/resources/testng.xml"))

lazy val `thrift22` = ProjectName("thrift22","thrift2").settings(
  libraryDependencies ++= Seq("commons-lang" % "commons-lang" % "2.6",
   "junit" % "junit" % "4.10" % "test"),
    name := "thrift22",
    version := "1.0.0",
    organization := "pl.jozwik"
).settings(com.github.bigtoast.sbtthrift.ThriftPlugin.thriftSettings :_* ).dependsOn(`thrift11`,`cxf2`)

