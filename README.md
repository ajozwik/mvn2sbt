#### Convert maven project (pom.xml files) to sbt project (build.sbt with plugins).


Logback and slf4j are used as reference projects.

Quick start:

1. Clone project
1. Run  `sbt pack`
1. Run target/pack/bin/mvn2sbt <mavenProject> <outputDir>
1. Copy <outputDir>/build.sbt to <mavenProject>/build.sbt
1. Copy <outputDir>/plugins.sbt to <mavenProject>/project/plugins.sbt
1. Go to <mavenProject> and run `sbt compile` for test

Now only groovy plugin is supported. Maven contains a huge numbers of plugins. 
The mapping from maven plugin to corresponding sbt plugin (with configuration) has to be done manually.

My roadmap is to support cxf/scala/apache-thrift plugins.

Project is still in phase alfa - source will be rebuilded in spare time.
