resolvers += "bigtoast-github" at "http://bigtoast.github.com/repo/"

resolvers += "Sonatype Repository" at "https://oss.sonatype.org/content/groups/public"

addSbtPlugin("com.github.bigtoast" % "sbt-thrift" % "0.7")

resolvers += Resolver.url("scalasbt" , url("http://scalasbt.artifactoryonline.com/scalasbt/repo"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.ebiznext.sbt.plugins" % "sbt-cxf-wsdl2java" % "0.1.2")

addSbtPlugin("org.scala-sbt.plugins" % "sbt-xjc" % "0.5")