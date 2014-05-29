import sbt._
import sbt.Keys._
import cgta.sbtxsjs.SbtXSjsPlugin
import SbtXSjsPlugin.XSjsProjects
import org.sbtidea.SbtIdeaPlugin
import scala.scalajs.sbtplugin.ScalaJSPlugin

object Common {
  sys.props("scalac.patmat.analysisBudget") = "512"

  object Versions {
    lazy val scala = "2.10.2"
    //Also change in plugins.sbt file
    lazy val scalaJs = "0.5.0-M3"
  }

  lazy val macroSettings = Seq[Setting[_]](
    libraryDependencies ++= Libs.macrosQuasi,
    addCompilerPlugin(CompilerPlugins.macrosPlugin))

  object CompilerPlugins {
    lazy val macrosPlugin = "org.scalamacros" %% "paradise" % "2.0.0" cross CrossVersion.full
  }

  object SbtPlugins {
    lazy val scalaJs = addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % Versions.scalaJs)
  }

  object Libs {
    lazy val macrosQuasi = Seq("org.scalamacros" %% "quasiquotes" % "2.0.0")
    lazy val sbtTestInterface = Seq("org.scala-sbt" % "test-interface" % "1.0")
  }

  lazy val basicSettings =
    sbtrelease.ReleasePlugin.releaseSettings ++
    bintray.Plugin.bintrayPublishSettings ++
    Seq[Setting[_]](
      licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
      (bintray.Keys.bintrayOrganization in bintray.Keys.bintray) := Some("cgta"),
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
      .settingsAll(basicSettings: _*)
      .settingsAll(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name)))
      .settingsJvm(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name, "-jvm")))
      .settingsSjs(ScalaJSPlugin.scalaJSSettings: _*)
      .settingsSjs(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name, "-sjs")))
  }
}