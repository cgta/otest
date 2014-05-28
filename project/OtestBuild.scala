import sbt._
import sbt.Keys._
import org.sbtidea.SbtIdeaPlugin
import scala.scalajs.sbtplugin.env.nodejs.NodeJSEnv
import scala.scalajs.sbtplugin.ScalaJSPlugin
import ScalaJSPlugin._
import scala.scalajs.sbtplugin.testing.JSClasspathLoader
import cgta.sbtxsjs.SbtXSjsPlugin
import SbtXSjsPlugin.XSjsProjects




object OtestCommonBuild extends Build {
  sys.props("scalac.patmat.analysisBudget") = "512"

  object Versions {
    lazy val scala = "2.10.2"
    lazy val scalaJs = "0.5.0-M3"
  }

  lazy val macroSettings = Seq[Setting[_]](
    libraryDependencies ++= Libs.macrosQuasi,
    addCompilerPlugin(CompilerPlugins.macrosPlugin))

  object CompilerPlugins {
    val macrosPlugin = "org.scalamacros" %% "paradise" % "2.0.0" cross CrossVersion.full
  }

  object Libs {
    val macrosQuasi      = Seq("org.scalamacros" %% "quasiquotes" % "2.0.0")
    val sbtTestInterface = Seq("org.scala-sbt" % "test-interface" % "1.0")
//    val scalaJSSbtPlugin = Seq("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % Versions.scalaJs)
  }

  lazy val basicSettings =
    Seq[Setting[_]](
      organization := "biz.cgta",
      scalaVersion := Versions.scala,
      shellPrompt <<= (thisProjectRef, version) {
        (id, v) => _ => "otest-build:%s:%s> ".format(id.project, v)
      }
    ) ++ scalacSettings


  lazy val scalacSettings = Seq[Setting[_]](
    scalacOptions += "-deprecation",
    scalacOptions += "-unchecked",
    scalacOptions += "-feature",
//    scalacOptions += "-Xfatal-warnings",
    scalacOptions += "-language:implicitConversions",
    scalacOptions += "-language:higherKinds"
  )


  def xprojects(name: String): XSjsProjects = {
    def getBasePackageName(projectName: String, suffix: String = null) = {
      val root = "cgta"
      val name = Option(suffix).map(projectName.split(_)(0)).getOrElse(projectName)
      root + "." + name
    }
    SbtXSjsPlugin.xprojects(name)
      .settingsShared(basicSettings: _*)
      .settingsShared(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name)))
      .settingsJvm(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name, "-jvm")))
      .settingsSjs(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name, "-sjs")))
  }
}

object OtestBuild extends Build {
  import OtestCommonBuild._
  lazy val otestX = xprojects("otest")
    .settingsShared(macroSettings: _*)
    .settingsShared(libraryDependencies ++= Libs.sbtTestInterface)
    .settingsSjs(addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % "0.5.0-M3"))

  lazy val otest    = otestX.shared
  lazy val otestJvm = otestX.jvm
  lazy val otestSjs = otestX.sjs

  lazy val root = Project("root", file("."))
    .aggregate(otestJvm, otestSjs)
    .settings(basicSettings: _*)
    .settings(publish := {})
}

object OTestSampleBuild extends Build {
  import OtestCommonBuild._

  val otestFrameworkJvm = new TestFramework("cgta.otest.runner.OtestSbtFrameworkJvm")
  val otestFrameworkSjs = new TestFramework("cgta.otest.runner.OtestSbtFrameworkSjs")

  lazy val osampletestsX = xprojects("osampletests")
    .settingsShared(libraryDependencies += "biz.cgta" %% "otest-jvm" % (version in ThisBuild).value,
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


  lazy val osampletests    = osampletestsX.shared
  lazy val osampletestsJvm = osampletestsX.jvm
  lazy val osampletestsSjs = osampletestsX.sjs
}

