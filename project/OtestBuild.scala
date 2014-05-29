import sbt._
import sbt.Keys._

import scala.scalajs.sbtplugin.env.nodejs.NodeJSEnv
import scala.scalajs.sbtplugin.ScalaJSPlugin
import scala.scalajs.sbtplugin.testing.JSClasspathLoader
import ScalaJSPlugin._




object OtestBuild extends Build {
  import Common._
  lazy val otestX = xprojects("otest")
    .settingsAll(macroSettings: _*)
    .settingsAll(libraryDependencies ++= Libs.sbtTestInterface)
    .settingsAll(SbtPlugins.scalaJs)
    .settingsAll(bintray.Keys.repository in bintray.Keys.bintray := "cgta-maven-releases")

  lazy val otest    = otestX.base
  lazy val otestJvm = otestX.jvm
  lazy val otestSjs = otestX.sjs

  lazy val otestSjsPlugin = Project("otest-sjs-plugin", file("otest-sjs-plugin"))
    .settings(basicSettings: _*)
    .settings(libraryDependencies ++= Libs.sbtTestInterface)
    .settings(SbtPlugins.scalaJs)
    .settings(sbtPlugin := true)
    .settings(publishMavenStyle := false)
    .settings(bintray.Keys.repository in bintray.Keys.bintray := "sbt-plugins")
    .dependsOn(otestJvm)


  lazy val root = Project("root", file("."))
    .aggregate(otestJvm, otestSjs, otestSjsPlugin)
    .settings(basicSettings: _*)
    .settings(publish := {})
}

object OtestSamplesBuild extends Build {
  import Common._

  val otestFrameworkJvm = new TestFramework("cgta.otest.runner.OtestSbtFrameworkJvm")
  val otestFrameworkSjs = new TestFramework("cgta.otest.runner.OtestSbtFrameworkSjs")

  lazy val osampletestsX = xprojects("osampletests")
    .settingsBase(libraryDependencies += "biz.cgta" %% "otest-jvm" % (version in ThisBuild).value,
      testFrameworks += otestFrameworkJvm)
    .settingsJvm(libraryDependencies += "biz.cgta" %% "otest-jvm" % (version in ThisBuild).value,
      testFrameworks += otestFrameworkJvm)
    .settingsSjs(
      libraryDependencies += "biz.cgta" %%% "otest-sjs" % (version in ThisBuild).value,
      (loadedTestFrameworks in Test) := {
        import cgta.otest.runner.OtestSbtFrameworkSjs
        (loadedTestFrameworks in Test).value.updated(
          sbt.TestFramework(classOf[OtestSbtFrameworkSjs].getName),
          new OtestSbtFrameworkSjs(env = (ScalaJSKeys.jsEnv in Test).value)
        )
      },
      (ScalaJSKeys.jsEnv in Test) := new NodeJSEnv,
      testLoader := JSClasspathLoader((ScalaJSKeys.execClasspath in Compile).value),
      testFrameworks += otestFrameworkSjs
    )



  lazy val osampletests    = osampletestsX.base
  lazy val osampletestsJvm = osampletestsX.jvm
  lazy val osampletestsSjs = osampletestsX.sjs
}

