import cgta.osbt.OsCgtaSbtPlugin
import sbt._
import sbt.Keys._

import sbtrelease.{ReleaseStateTransformations, ReleasePlugin, ReleaseStep}
import scala.annotation.tailrec



object OtestBuild extends Build {
  import cgta.osbt.OsCgtaSbtPlugin._

  lazy val otestX = xprojects("otest")
    .settingsAll(libraryDependencies ++= (if (scalaVersion.value.startsWith("2.10.")) Libs.macrosQuasi else Nil))
    .settingsAll(CompilerPlugins.macrosPlugin)
    .settingsAll(libraryDependencies ++= Libs.sbtTestInterface)
    .settingsAll(libraryDependencies += Libs.scalaReflect % scalaVersion.value)
    .settingsAll(Bintray.repo("cgta-maven-releases"))

  lazy val otest    = otestX.base
  lazy val otestJvm = otestX.jvm
  lazy val otestSjs = otestX.sjs

  lazy val otestPlugin = Project("otest-sjs-plugin", file("./otest-sjs-plugin"))
    .settings(basicSettings: _*)
    .settings(libraryDependencies ++= Libs.sbtTestInterface)
    .settings(libraryDependencies += Libs.scalaReflect % scalaVersion.value)
    .settings(SbtPlugins.scalaJs)
    .settings(sbtPlugin := true)
    .settings(Bintray.repo("sbt-plugins"))
    .dependsOn(otestJvm)

  object ReleaseProcess {
    object CgtaSteps {
      def runAllTasks[A](tasks: TaskKey[A]*)(st0: State): State = {
        @tailrec
        def loop(tasks: List[TaskKey[A]], st: State): State = {
          tasks match {
            case Nil => st
            case task :: tasks => loop(tasks, Project.runTask(task, st).get._1)
          }
        }
        loop(tasks.toList, st0)
      }

      lazy val otests = Seq(otestJvm, otestSjs)

      lazy val runTestOtest = ReleaseStep(
        action = runAllTasks(otests.map(test in Test in _): _*)(_),
        enableCrossBuild = true
      )

      lazy val runTestPlugin = ReleaseStep(
        action = runAllTasks(test in Test in otestPlugin)(_))

      lazy val publishArtifactsOtest = ReleaseStep(
        action = runAllTasks(otests.map(publish in Global in _): _*)(_),
        check = st => {
          // getPublishTo fails if no publish repository is set up.
          val ex = Project.extract(st)
          otests.foreach(p => Classpaths.getPublishTo(ex.get(publishTo in Global in p)))
          st
        },
        enableCrossBuild = true
      )

      lazy val publishArtifactsPlugin = ReleaseStep(
        action = runAllTasks(publish in Global in otestPlugin)(_),
        check = st => {
          val ex = Project.extract(st)
          Classpaths.getPublishTo(ex.get(publishTo in Global in otestPlugin))
          st
        }
      )
    }

    lazy val settings = Seq[Setting[_]](
      ReleasePlugin.ReleaseKeys.releaseProcess := {
        import ReleaseStateTransformations._
        Seq[ReleaseStep](
          checkSnapshotDependencies,
          inquireVersions,
          runClean,
          CgtaSteps.runTestOtest,
          CgtaSteps.runTestPlugin,
          setReleaseVersion,
          commitReleaseVersion,
          tagRelease,
          CgtaSteps.publishArtifactsOtest,
          CgtaSteps.publishArtifactsPlugin,
          setNextVersion,
          commitNextVersion,
          pushChanges
        )
      }
    )
  }


  lazy val root = Project("root", file("."))
    .aggregate(otestJvm, otestSjs, otestPlugin)
    .settings(crossScalaVersions := Seq("2.10.2", "2.11.1"))
    .settings(basicSettings: _*)
    .settings(ReleaseProcess.settings: _*)
    .settings(publish :=())
    .settings(publishLocal :=())
}


