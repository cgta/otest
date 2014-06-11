import sbt._
import cgta.sbtxsjs.SbtXSjsPlugin

object ExamplesBuild extends Build {

  lazy val exampleTestsX = SbtXSjsPlugin.xprojects("example-tests")

  lazy val exampleTests = exampleTestsX.base
  lazy val exampleTestsJvm = exampleTestsX.jvm
  lazy val exampleTestsSjs = exampleTestsX.sjs
}
