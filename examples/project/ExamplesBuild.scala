import sbt._
import sbt.Keys._
import cgta.sbtxsjs.SbtXSjsPlugin

object ExamplesBuild extends Build {

  lazy val otestVersion = "0.2.0-M3-SNAPSHOT"

  import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

  lazy val exampleTestsX = SbtXSjsPlugin.XSjsProjects("example-tests", file("example-tests"))
    .settingsBase(
      libraryDependencies += "biz.cgta" %% "otest-jvm" % otestVersion % "test")
    .settingsJvm(
      libraryDependencies += "biz.cgta" %% "otest-jvm" % otestVersion % "test",
      testFrameworks := Seq(new TestFramework("cgta.otest.runner.OtestSbtFramework")))
    .settingsSjs(
      libraryDependencies += "biz.cgta" %%%! "otest-sjs" % otestVersion % "test",
      testFrameworks := Seq(new TestFramework("cgta.otest.runner.OtestSbtFramework")),
      scalaJSStage in Test := FastOptStage
    )
    .mapSjs(_.enablePlugins(org.scalajs.sbtplugin.ScalaJSPlugin))


  lazy val exampleTests = exampleTestsX.base
  lazy val exampleTestsJvm = exampleTestsX.jvm
  lazy val exampleTestsSjs = exampleTestsX.sjs
}

