import sbt._
import sbt.Keys._

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