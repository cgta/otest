import sbt._
import sbt.Keys._

import sbtrelease.{ReleaseStateTransformations, ReleasePlugin, ReleaseStep}
import scala.scalajs.sbtplugin.env.nodejs.NodeJSEnv
import scala.scalajs.sbtplugin.ScalaJSPlugin
import scala.scalajs.sbtplugin.testing.JSClasspathLoader
import ScalaJSPlugin._

import cgta.sbtxsjs.SbtXSjsPlugin
import SbtXSjsPlugin.XSjsProjects

import org.sbtidea.SbtIdeaPlugin

object Common {
  sys.props("scalac.patmat.analysisBudget") = "512"

  object Versions {
    lazy val scala      = "2.10.2"
    lazy val crossScala = List("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1")

    //Also change in plugins.sbt file
    lazy val scalaJs = "0.5.0-RC1"
  }

  object SbtPlugins {
    lazy val scalaJs = addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % Versions.scalaJs)
  }

  object CompilerPlugins {
    lazy val macrosPlugin = addCompilerPlugin("org.scalamacros" %% "paradise" % "2.0.0" cross CrossVersion.full)
  }

  object Libs {
    lazy val macrosQuasi      = Seq("org.scalamacros" %% "quasiquotes" % "2.0.0")
    lazy val sbtTestInterface = Seq("org.scala-sbt" % "test-interface" % "1.0")
    lazy val scalaJsTools     = Seq("org.scala-lang.modules.scalajs" %% "scalajs-tools" % Versions.scalaJs)
    //    lazy val scalaJsPlugin     = Seq("org.scala-lang.modules.scalajs" %% "scalajs-plugin" % Versions.scalaJs)
    val scalaReflect = "org.scala-lang" % "scala-reflect"
  }

  object Compiler {
    lazy val settings = Seq[Setting[_]](
      scalacOptions += "-deprecation",
      scalacOptions += "-unchecked",
      scalacOptions += "-feature",
      // can't use this because of cross platform warnings
      //    scalacOptions += "-Xfatal-warnings",
      scalacOptions += "-language:implicitConversions",
      scalacOptions += "-language:higherKinds"
    )
  }

  object Prompt {
    lazy val settings = Seq[Setting[_]](shellPrompt <<= (thisProjectRef, version) {
      (id, v) => _ => "otest-build:%s:%s> ".format(id.project, v)
    })
  }

  object Bintray {
    lazy val settings = bintray.Plugin.bintrayPublishSettings :+
      ((bintray.Keys.bintrayOrganization in bintray.Keys.bintray) := Some("cgta"))

    def repo(name: String) = bintray.Keys.repository in bintray.Keys.bintray := name
  }

  object Release {
    lazy val settings = sbtrelease.ReleasePlugin.releaseSettings
  }

  object License {
    lazy val mit = licenses +=("MIT", url("http://opensource.org/licenses/MIT"))
  }


  lazy val basicSettings =
    Seq[Setting[_]](
      organization := "biz.cgta",
      scalaVersion := Versions.scala) ++
      Compiler.settings ++
      Prompt.settings ++
      Bintray.settings ++
      Release.settings :+
      License.mit


  def xprojects(name: String): XSjsProjects = {
    def getBasePackageName(projectName: String, suffix: String = null) = {
      val root = "cgta"
      val name = Option(suffix).fold(projectName)(projectName.split(_)(0))
      root + "." + name
    }
    SbtXSjsPlugin.xprojects(name)
      .settingsAll(basicSettings: _*)
      .settingsAll(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name)))
      .settingsJvm(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name, "-jvm")))
      .settingsSjs(ScalaJSPlugin.scalaJSSettings: _*)
      .settingsSjs(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name, "-sjs")))
  }
}


object OtestBuild extends Build {
  import Common._
  lazy val otestX = xprojects("otest")
    .settingsAll(libraryDependencies ++= (if (scalaVersion.value.startsWith("2.10.")) Libs.macrosQuasi else Nil))
    .settingsAll(CompilerPlugins.macrosPlugin)
    .settingsAll(libraryDependencies ++= Libs.sbtTestInterface)
    .settingsAll(libraryDependencies += Libs.scalaReflect % scalaVersion.value)
    .settingsAll(Bintray.repo("cgta-maven-releases"))

  lazy val otest    = otestX.base
  lazy val otestJvm = otestX.jvm
  lazy val otestSjs = otestX.sjs

  lazy val otestSjsPlugin = Project("otest-sjs-plugin", file("otest-sjs-plugin"))
    .settings(basicSettings: _*)
    .settings(libraryDependencies ++= Libs.sbtTestInterface)
    .settings(libraryDependencies += Libs.scalaReflect % scalaVersion.value)
    .settings(SbtPlugins.scalaJs)
    .settings(sbtPlugin := true)
    .settings(Bintray.repo("sbt-plugins"))
    .dependsOn(otestJvm)
  //    .settings(libraryDependencies ++= Libs.scalaJsTools)
  //    .settings(libraryDependencies ++= Libs.scalaJsPlugin)
  //    .settings(publishMavenStyle := false)

  object ReleaseProcess {
    import ReleaseStateTransformations._

    lazy val settings = Seq[Setting[_]](
      ReleasePlugin.ReleaseKeys.releaseProcess := {
        Seq[ReleaseStep](
          checkSnapshotDependencies, // : ReleaseStep
          inquireVersions, // : ReleaseStep
          runTest, // : ReleaseStep
          setReleaseVersion, // : ReleaseStep
          commitReleaseVersion, // : ReleaseStep, performs the initial git checks
          tagRelease, // : ReleaseStep
          publishArtifacts, // : ReleaseStep, checks whether `publishTo` is properly set up
          setNextVersion, // : ReleaseStep
          commitNextVersion, // : ReleaseStep
          pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
        )
      }
    )
  }


  lazy val root = Project("root", file("."))
    .aggregate(otestJvm, otestSjs)
    .settings(basicSettings: _*)
    .settings(crossScalaVersions := Versions.crossScala)
    .settings(ReleaseProcess.settings: _*)
    .settings(publish :=())
    .settings(publishLocal :=())
}
