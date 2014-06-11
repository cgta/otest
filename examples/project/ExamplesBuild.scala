import sbt._
import cgta.sbtxsjs.SbtXSjsPlugin

object ExamplesBuild extends Build {

  lazy val exampleTestsX = SbtXSjsPlugin.xprojects("example-tests")


  //Be sure to look at the build.sbt files under each subprojects folder as well
  lazy val exampleTests = exampleTestsX.base
  lazy val exampleTestsJvm = exampleTestsX.jvm
  lazy val exampleTestsSjs = exampleTestsX.sjs
}
