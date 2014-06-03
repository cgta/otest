import sbt._
import sbt.Keys._

import scala.scalajs.sbtplugin.ScalaJSPlugin
import ScalaJSPlugin._
import scala.scalajs.sbtplugin.env.nodejs.NodeJSEnv
import scala.scalajs.sbtplugin.testing.JSClasspathLoader


object OtestSamplesBuild extends Build {
  import cgta.osbt.OsCgtaSbtPlugin._

   val otestFrameworkJvm = new TestFramework("cgta.otest.runner.OtestSbtFrameworkJvm")
   val otestFrameworkSjs = new TestFramework("cgta.otest.runner.OtestSbtFrameworkSjs")

   lazy val testVersion = "0.1.1"


   lazy val osampletestsX = xprojects("osampletests")
     .settingsBase(libraryDependencies += "biz.cgta" %% "otest-jvm" % testVersion,
       testFrameworks += otestFrameworkJvm)
     .settingsJvm(libraryDependencies += "biz.cgta" %% "otest-jvm" % testVersion,
       testFrameworks += otestFrameworkJvm)
     .settingsSjs(
       libraryDependencies += "biz.cgta" %%% "otest-sjs" % testVersion,
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
