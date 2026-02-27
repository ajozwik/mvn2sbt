Mvn2Sbt 

[![Build Status](https://travis-ci.com/ajozwik/mvn2sbt.svg?branch=master "Build Status")]([https://travis-ci.com/ajozwik/mvn2sbt](https://app.travis-ci.com/ajozwik/mvn2sbt.svg))
[![Coverage Status](https://coveralls.io/repos/ajozwik/mvn2sbt/badge.png)](https://coveralls.io/r/ajozwik/mvn2sbt)
[![Codacy Badge](https://www.codacy.com/project/badge/c2c836104f254cafa3f4c34dc5243400)](https://www.codacy.com)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

=========

**Convert maven multi module project (pom.xml files) to sbt project (build.sbt with plugins).**

Logback and slf4j are used as reference projects.

Quick start:

1. Clone mvn2sbt. 
1. Run `scala Eff.scala mavenProject`, where mavenProject - directory with your maven project to prepare for conversion.
1. Run `sbt ' ; set javaOptions +="[-Dscala.version=2.12.8] [-Dsbt.version=1.2.8]"; converter/run mavenProject [outputDir]'`
	* Optional (if outputDir specified): copy <outputDir>/build.sbt to &lt;mavenProject&gt;/build.sbt
	* Optional (if outputDir specified): copy <outputDir>/plugins.sbt to &lt;mavenProject&gt;/project/plugins.sbt
1. Go to <mavenProject> and run `sbt compile` for test

*Note*
arguments added as `[..]` are optional.

Supported plugins:

Without parameters ( default configuration)

 * gmaven-plugin
 * maven-thrift-plugin
 * maven-war-plugin
 * cxf-codegen-plugin
 * gwt-maven-plugin
 * testng plugin (as dependency)
 

 Example converters are in package: pl.jozwik.mvn2sbt.pom2sbt



