import sbt._
import sbt.Keys._
import cgta.sbtxsjs.SbtXSjsPlugin

object ExamplesBuild extends Build {

  //ALSO CHANGE IN project/plugins.sbt
  lazy val otestVersion = "0.1.8-SNAPSHOT"


  import scala.scalajs.sbtplugin.ScalaJSPlugin._

  lazy val exampleTestsX = SbtXSjsPlugin.xSjsProjects("example-tests", file("example-tests"))
    .settingsBase(libraryDependencies += "biz.cgta" %% "otest-jvm" % otestVersion % "test")
    .settingsJvm(libraryDependencies += "biz.cgta" %% "otest-jvm" % otestVersion % "test")
    .settingsJvm(cgta.otest.OtestPlugin.settingsJvm: _*)
    .settingsSjs(libraryDependencies += "biz.cgta" %%% "otest-sjs" % otestVersion % "test")
    .settingsSjs(scala.scalajs.sbtplugin.ScalaJSPlugin.scalaJSSettings: _*)
    .settingsSjs(cgta.otest.OtestPlugin.settingsSjs: _*)

  lazy val exampleTests = exampleTestsX.base
  lazy val exampleTestsJvm = exampleTestsX.jvm
  lazy val exampleTestsSjs = exampleTestsX.sjs
}

