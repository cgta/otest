import sbt._
import cgta.sbtxsjs.SbtXSjsPlugin

object ExamplesBuild extends Build {

  lazy val exampleTestsX = SbtXSjsPlugin.xprojects("example-tests")

  //  lazy val otestVersion = "0.1.5-SNAPSHOT"
  //ALSO CHANGE IN project/plugins.sbt
  lazy val otestVersion = "0.1.5-SNAPSHOT"

  //Be sure to look at the build.sbt files under each subprojects folder as well
  lazy val exampleTests    = exampleTestsX.base.settings(
    Keys.libraryDependencies += "biz.cgta" %% "otest-jvm" % otestVersion % "test")
  lazy val exampleTestsJvm = exampleTestsX.jvm.settings(
    Keys.libraryDependencies += "biz.cgta" %% "otest-jvm" % otestVersion % "test")
  lazy val exampleTestsSjs = exampleTestsX.sjs.settings(
    Keys.libraryDependencies += "biz.cgta" %% "otest-sjs" % otestVersion % "test")
}

