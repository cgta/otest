import cgta.osbt.OsCgtaSbtPlugin
import sbt._
import sbt.Keys._

import sbtrelease.{ReleaseStateTransformations, ReleasePlugin, ReleaseStep}
import scala.annotation.tailrec


//Comment out when building for first time
object ExamplesBuild extends Build {
  import scala.scalajs.sbtplugin.ScalaJSPlugin
  import ScalaJSPlugin._
  import scala.scalajs.sbtplugin.env.nodejs.NodeJSEnv
  import scala.scalajs.sbtplugin.testing.JSClasspathLoader
  import cgta.osbt.OsCgtaSbtPlugin._

  val otestFrameworkJvm = new TestFramework("cgta.otest.runner.OtestSbtFrameworkJvm")
  val otestFrameworkSjs = new TestFramework("cgta.otest.runner.OtestSbtFrameworkSjs")

  lazy val exampleTestsX = xprojects("example-tests")
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


  lazy val exampleTests = exampleTestsX.base
  lazy val exampleTestsJvm = exampleTestsX.jvm
  lazy val exampleTestsSjs = exampleTestsX.sjs
}
