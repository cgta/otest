
import org.sbtidea.SbtIdeaPlugin
import sbt._
import sbt.Keys._
import scala.scalajs.sbtplugin.ScalaJSPlugin


object OtestBuild extends Build {
  sys.props("scalac.patmat.analysisBudget") = "512"


  def getBasePackageName(projectName: String, suffix: String = null) = {
    val root = "cgta"
    val name = Option(suffix).map(projectName.split(_)(0)).getOrElse(projectName)
    root + "." + name
  }

  object Versions {
    lazy val scala = "2.10.2"
  }

  lazy val basicSettings =
    Seq[Setting[_]](
      organization := "biz.cgta",
      scalaVersion := Versions.scala,
      shellPrompt <<= (thisProjectRef, version) {
        (id, v) => _ => "orange:%s:%s> ".format(id.project, v)
      }
    ) ++ scalacSettings


  lazy val scalacSettings = Seq[Setting[_]](
    scalacOptions += "-deprecation",
    scalacOptions += "-unchecked",
    scalacOptions += "-feature",
    scalacOptions += "-language:implicitConversions",
    scalacOptions += "-language:higherKinds",
    scalacOptions += "-Xfatal-warnings")

  lazy val macroSettings = Seq[Setting[_]](
    libraryDependencies ++= Libs.macrosQuasi,
    addCompilerPlugin(Libs.macrosPlugin))

  object Libs {
    val macrosQuasi = List("org.scalamacros" %% "quasiquotes" % "2.0.0")
    val macrosPlugin = "org.scalamacros" %% "paradise" % "2.0.0" cross CrossVersion.full
  }

  def sharedProject(name: String) = Project(name, file(name))
    .settings(basicSettings: _*)
    .settings(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name)))

  def jvmProject(name: String) = Project(name, file(name))
    .settings(basicSettings: _*)
    .settings(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name, "-jvm")))

  def sjsProject(name: String) = Project(name, file(name))
    .settings(basicSettings: _*)
    .settings(SbtIdeaPlugin.ideaBasePackage := Some(getBasePackageName(name, "-sjs")))
    .settings(ScalaJSPlugin.scalaJSSettings: _*)


  //Sets up projects that have code for both the jvm and js environments
  class SjsCrossBuild(val name: String,
                      val deps: Seq[SjsCrossBuild] = Seq.empty,
                      val sharedSettings: Seq[Def.Setting[_]] = Seq.empty,
                      val jvmLibs: Seq[ModuleID] = Seq.empty,
                      val sjsLibs: Seq[ModuleID] = Seq.empty) {

    val sharedSourceSettings = Seq(
      unmanagedSourceDirectories in Compile += baseDirectory(_ / ".." / name / "src" / "main" / "scala").value,
      unmanagedSourceDirectories in Test += baseDirectory(_ / ".." / name / "src" / "test" / "scala").value)

    //This project is provided so that the gen-idea script will make a project for intellij
    //As such the jvm libraries are used
    lazy val shared: Project = sharedProject(name)
      .settings(sharedSettings: _*)

    lazy val sjs: Project = sjsProject(name + "-sjs")
      .settings(sharedSettings ++ sharedSourceSettings: _*)
      .settings(libraryDependencies ++= sjsLibs)
      .dependsOn(deps.map(x => x.sjs: sbt.ClasspathDep[sbt.ProjectReference]): _*)

    lazy val jvm: Project = jvmProject(name + "-jvm")
      .settings(sharedSettings ++ sharedSourceSettings: _*)
      .settings(libraryDependencies ++= jvmLibs)
      .dependsOn(deps.map(x => x.jvm: sbt.ClasspathDep[sbt.ProjectReference]): _*)
  }

  lazy val otestCross = new SjsCrossBuild("otest",
    sharedSettings = macroSettings)
  lazy val otest = otestCross.shared
  lazy val otestJvm = otestCross.jvm
  lazy val otestSjs = otestCross.sjs

  lazy val orunnerCross = new SjsCrossBuild("orunner")
  lazy val orunner = orunnerCross.shared
  lazy val orunnerJvm = orunnerCross.jvm
  lazy val orunnerSjs = orunnerCross.sjs

  lazy val osampletestsCross = new SjsCrossBuild("osampletests")
  lazy val osampletests = osampletestsCross.shared
  lazy val osampletestsJvm = osampletestsCross.jvm
  lazy val osampletestsSjs = osampletestsCross.sjs


  lazy val root = Project("root", file("."))
    .aggregate(
      otestJvm, otestSjs,
      orunnerJvm, orunnerSjs
    )

}
