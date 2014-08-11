Mvn2Sbt [![Build Status](https://travis-ci.org/ajozwik/mvn2sbt.svg?branch=master "Build Status")](https://travis-ci.org/ajozwik/mvn2sbt)[![Coverage Status](https://coveralls.io/repos/ajozwik/mvn2sbt/badge.png)](https://coveralls.io/r/ajozwik/mvn2sbt)
=========
**Convert maven project (pom.xml files) to sbt project (build.sbt with plugins).**

Logback and slf4j are used as reference projects.

Quick start:

1. Clone project
1. Run scala Eff mavenProject, where rootDir - directory with your maven project
1. Run converter/target/pack/bin/mvn2sbt &lt;mavenProject&gt; [outputDir] [-Dscala.version=&lt;2.11.2&gt;]
1. Optional (if outputDir specified): copy <outputDir>/build.sbt to &lt;mavenProject&gt;/build.sbt
1. Optional (if outputDir specified): copy <outputDir>/plugins.sbt to &lt;mavenProject&gt;/project/plugins.sbt
1. Go to <mavenProject> and run `sbt compile` for test

Supported plugins:

Without parameters ( default configuration)

 * gmaven-plugin
 * maven-thrift-plugin
 * maven-war-plugin
 * cxf-codegen-plugin
 * testng plugin (as dependency)
 
Project is still in phase alfa - source will be rebuilt in spare time.



