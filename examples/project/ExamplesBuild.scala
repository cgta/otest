import sbt._
import sbt.Keys._
import cgta.sbtxsjs.SbtXSjsPlugin

object ExamplesBuild extends Build {

  lazy val exampleTestsX = SbtXSjsPlugin.xprojects("example-tests")
    .settingsAll(bintray.Plugin.bintrayResolverSettings: _*)

  //  lazy val otestVersion = "0.1.5-SNAPSHOT"
  //ALSO CHANGE IN project/plugins.sbt
  lazy val otestVersion = "0.1.6-SNAPSHOT"

  //Be sure to look at the build.sbt files under each subprojects folder as well
  lazy val exampleTests = exampleTestsX.base
    .settings(libraryDependencies += "biz.cgta" %% "otest-jvm" % otestVersion % "test")

  lazy val exampleTestsJvm = exampleTestsX.jvm
    .settings(libraryDependencies += "biz.cgta" %% "otest-jvm" % otestVersion % "test")
    .settings(cgta.otest.OtestPlugin.settingsJvm: _*)


  import scala.scalajs.sbtplugin.ScalaJSPlugin._
  lazy val exampleTestsSjs = exampleTestsX.sjs
    .settings(libraryDependencies += "biz.cgta" %%% "otest-sjs" % otestVersion % "test")
    .settings(scala.scalajs.sbtplugin.ScalaJSPlugin.scalaJSSettings: _*)
    .settings(cgta.otest.OtestPlugin.settingsSjs: _*)
}

