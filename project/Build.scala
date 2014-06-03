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



object Build {
  import cgta.osbt.OsCgtaSbtPlugin._

  lazy val otestX = xprojects("otest")
    .settingsAll(libraryDependencies ++= (if (scalaVersion.value.startsWith("2.10.")) Libs.macrosQuasi else Nil))
    .settingsAll(CompilerPlugins.macrosPlugin)
    .settingsAll(libraryDependencies ++= Libs.sbtTestInterface)
    .settingsAll(libraryDependencies += Libs.scalaReflect % scalaVersion.value)
    .settingsAll(Bintray.repo("cgta-maven-releases"))
    .settingsAll(crossScalaVersions := Versions.crossScala)
    .settingsAll(ReleasePlugin.ReleaseKeys.releaseProcess := {
    import ReleaseStateTransformations._
    Seq[ReleaseStep](
      runClean,
      runTest,
      publishArtifacts
    )
  })

  lazy val otest    = otestX.base
  lazy val otestJvm = otestX.jvm
  lazy val otestSjs = otestX.sjs


//    .settings(addOsCgtaDep("otest-jvm"))

  //    .settings(libraryDependencies ++= Libs.scalaJsTools)
  //    .settings(libraryDependencies ++= Libs.scalaJsPlugin)
  //    .settings(publishMavenStyle := false)

  object ReleaseProcess {
    //    lazy val settings = Seq[Setting[_]](
    //      ReleasePlugin.ReleaseKeys.releaseProcess := {
    //        import ReleaseStateTransformations._
    //        Seq[ReleaseStep](
    //          checkSnapshotDependencies, // : ReleaseStep
    //          inquireVersions, // : ReleaseStep
    //          runClean, // : ReleaseStep
    //          runTest, // : ReleaseStep
    //          setReleaseVersion, // : ReleaseStep
    //          commitReleaseVersion, // : ReleaseStep, performs the initial git checks
    //          tagRelease, // : ReleaseStep
    //          publishArtifacts, // : ReleaseStep, checks whether `publishTo` is properly set up
    //          setNextVersion, // : ReleaseStep
    //          commitNextVersion, // : ReleaseStep
    //          pushChanges // : ReleaseStep, also checks that an upstream branch is properly configured
    //        )
    //      }
    //    )
    lazy val settings = Seq[Setting[_]](
      ReleasePlugin.ReleaseKeys.releaseProcess := {
        import ReleaseStateTransformations._
        Seq[ReleaseStep](
        runTests
        )
      }
    )
  }

  lazy val runTests = ReleaseStep(action = st => {
    // extract the build state
    val extracted: Extracted = Project.extract(st)
    val ref: ProjectRef = extracted.get(thisProjectRef)
    extracted.runAggregated(test in Test in ref, st)
  })


  lazy val root = Project("root", file("."))
    .aggregate(otestJvm, otestSjs)
    .settings(basicSettings: _*)
    .settings(ReleaseProcess.settings: _*)
    .settings(publish :=())
    .settings(publishLocal :=())
}
