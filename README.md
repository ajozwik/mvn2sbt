Mvn2Sbt [![Build Status](https://travis-ci.org/ajozwik/mvn2sbt.svg?branch=master "Build Status")](https://travis-ci.org/ajozwik/mvn2sbt)[![Coverage Status](https://coveralls.io/repos/ajozwik/mvn2sbt/badge.png)](https://coveralls.io/r/ajozwik/mvn2sbt)
=========
**Convert maven project (pom.xml files) to sbt project (build.sbt with plugins).**

Logback and slf4j are used as reference projects.

Quick start:

1. Clone project
1. Run converter/target/pack/bin/mvn2sbt <mavenProject> <outputDir>
1. Copy <outputDir>/build.sbt to <mavenProject>/build.sbt
1. Copy <outputDir>/plugins.sbt to <mavenProject>/project/plugins.sbt
1. Go to <mavenProject> and run `sbt compile` for test

Supported plugins:

Without parameters ( default configuration)

 * gmaven-plugin
 * maven-thrift-plugin
 * war plugin
 
With basic configuration
 
 * cxf-codegen-plugin - with some options


Project is still in phase alfa - source will be rebuilt in spare time.



