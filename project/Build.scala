import cgta.osbt.OsCgtaSbtPlugin
import cgta.sbtxsjs.SbtXSjsPlugin
import org.sbtidea.SbtIdeaPlugin
import sbt._
import sbt.Keys._

import sbtrelease.{ReleaseStateTransformations, ReleasePlugin, ReleaseStep}
import scala.annotation.tailrec



object Build extends sbt.Build {

//  org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[(ch.qos.logback.classic.Logger)].setLevel(ch.qos.logback.classic.Level.INFO)


  object Versions {

    //Change in project/scalaJs.sbt as well
    lazy val scalaJs = "0.5.0"
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

  lazy val otestX = SbtXSjsPlugin.xSjsProjects("otest", file("otest"))
    .settingsAll(SbtIdeaPlugin.ideaBasePackage := Some("cgta.otest"))
    .settingsAll(OsCgtaSbtPlugin.basicSettings: _*)
    .settingsAll(libraryDependencies ++= (if (scalaVersion.value.startsWith("2.10.")) Libs.macrosQuasi else Nil))
    .settingsAll(CompilerPlugins.macrosPlugin)
    .settingsAll(libraryDependencies ++= Libs.sbtTestInterface)
    .settingsAll(libraryDependencies += Libs.scalaReflect % scalaVersion.value)

  lazy val otest    = otestX.base
  lazy val otestJvm = otestX.jvm
  lazy val otestSjs = otestX.sjs

  lazy val otestSbtPlugin = Project("otest-sbt-plugin", file("./otest-sbt-plugin"))
    .settingsAll(SbtIdeaPlugin.ideaBasePackage := Some("cgta.otest"))
    .settings(libraryDependencies ++= Libs.sbtTestInterface)
    .settings(libraryDependencies += Libs.scalaReflect % scalaVersion.value)
    .settings(addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % Versions.scalaJs % "provided"))
    .settings(sbtPlugin := true)
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
        action = runAllTasks(test in Test in otestSbtPlugin)(_))

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
        action = runAllTasks(publish in Global in otestSbtPlugin)(_),
        check = st => {
          val ex = Project.extract(st)
          Classpaths.getPublishTo(ex.get(publishTo in Global in otestSbtPlugin))
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
    .aggregate(otestJvm, otestSjs, otestSbtPlugin)
    .settings(crossScalaVersions := Seq("2.10.2", "2.11.1"))
    .settings(OsCgtaSbtPlugin.basicSettings: _*)
    .settings(ReleaseProcess.settings: _*)
    .settings(publish :=())
    .settings(publishLocal :=())
}



