import sbt._
import sbt.Keys._

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object ExamplesBuild extends Build {
  import BaseBuild._
  //  scalaVersion in ThisBuild := "2.11.7"
  //
  //  lazy val otestVersion = "0.2.1-SNAPSHOT"
  //
  //  import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
  //
  //  lazy val exampleTestsX = SbtXSjsPlugin.XSjsProjects("example-tests", file("example-tests"))
  //    .settingsBase(
  //      scalaVersion := "2.11.7",
  //      libraryDependencies += "biz.cgta" %% "otest" % otestVersion % "test")
  //    .settingsJvm(
  //      libraryDependencies += "biz.cgta" %% "otest" % otestVersion % "test",
  //      testFrameworks := Seq(new TestFramework("cgta.otest.runner.OtestSbtFramework")))
  //    .settingsSjs(
  //      libraryDependencies += "biz.cgta" %%% "otest" % otestVersion % "test",
  //      testFrameworks := Seq(new TestFramework("cgta.otest.runner.OtestSbtFramework")),
  //      scalaJSStage in Test := FastOptStage
  //    )
  //    .mapSjs(_.enablePlugins(org.scalajs.sbtplugin.ScalaJSPlugin))
  //
  //
  //  lazy val exampleTests = exampleTestsX.base
  //  lazy val exampleTestsJvm = exampleTestsX.jvm
  //  lazy val exampleTestsSjs = exampleTestsX.sjs

  lazy val otestVersion = "0.2.2-SNAPSHOT"

  lazy val otestExamples = crossProject.in(file("example-tests")).configure(xp("example-tests", _))
    .jsConfigure(_.copy(id = "otestExamplesSJS"))
    .settings(publishMavenStyle := true)
    .settings(
      scalaVersion := "2.11.7"
    )
    .jvmSettings(
      libraryDependencies += "biz.cgta" %% "otest" % otestVersion % "test",
      testFrameworks := Seq(new TestFramework("cgta.otest.runner.OtestSbtFramework")))
    .jsSettings(
      libraryDependencies += "biz.cgta" %%% "otest" % otestVersion % "test",
      testFrameworks := Seq(new TestFramework("cgta.otest.runner.OtestSbtFramework")),
      scalaJSStage in Test := FastOptStage
    )

  lazy val otestExamplesJVM = otestExamples.jvm
  lazy val otestExamplesSJS = otestExamples.js


}

