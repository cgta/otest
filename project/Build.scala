import cgta.osbt.OsCgtaSbtPlugin
import cgta.sbtxsjs.SbtXSjsPlugin
import org.sbtidea.SbtIdeaPlugin
import sbt._
import sbt.Keys._

import sbtrelease.{ReleaseStateTransformations, ReleasePlugin, ReleaseStep}
import scala.annotation.tailrec
import scala.scalajs.sbtplugin.ScalaJSPlugin
import com.typesafe.sbt.SbtPgp.PgpKeys


object Build extends sbt.Build {

  //  org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[(ch.qos.logback.classic.Logger)].setLevel(ch.qos.logback.classic.Level.INFO)


  object Versions {

    //Change in project/scalaJs.sbt as well
    lazy val scalaJs = "0.5.0"
  }

  object PublishSets {
    val settings = Seq[Setting[_]](
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases" at nexus + "service/local/staging/deploy/maven2")
      },
      (publishArtifact in Test) := false,
      pomIncludeRepository := { _ => false},
      pomExtra :=
        <url>https://github.com/cgta/otest</url>
          <licenses>
            <license>
              <name>MIT license</name>
              <url>http://www.opensource.org/licenses/mit-license.php</url>
            </license>
          </licenses>
          <scm>
            <url>git://github.com/cgta/otest.git</url>
            <connection>scm:git://github.com/cgta/otest.git</connection>
          </scm>
          <developers>
            <developer>
              <id>benjaminjackman</id>
              <name>Benjamin Jackman</name>
              <url>https://github.com/benjaminjackman</url>
            </developer>
          </developers>

    )
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
    .settingsAll(organization := "biz.cgta")
    .settingsAll(publishMavenStyle := true)
    .settingsAll(SbtIdeaPlugin.ideaBasePackage := Some("cgta.otest"))
    .settingsAll(OsCgtaSbtPlugin.basicSettings: _*)
    .settingsAll(libraryDependencies ++= (if (scalaVersion.value.startsWith("2.10.")) Libs.macrosQuasi else Nil))
    .settingsAll(CompilerPlugins.macrosPlugin)
    .settingsAll(libraryDependencies ++= Libs.sbtTestInterface)
    .settingsAll(libraryDependencies += Libs.scalaReflect % scalaVersion.value)
    .settingsSjs(ScalaJSPlugin.scalaJSSettings: _*)

  lazy val otest    = otestX.base
  lazy val otestJvm = otestX.jvm
  lazy val otestSjs = otestX.sjs

  lazy val otestSbtPlugin = Project("otest-sbt-plugin", file("./otest-sbt-plugin"))
    .settings(organization := "biz.cgta")
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
        action = runAllTasks(otests.map(PgpKeys.publishSigned in Global in _): _*)(_),
        check = st => {
          // getPublishTo fails if no publish repository is set up.
          val ex = Project.extract(st)
          otests.foreach(p => Classpaths.getPublishTo(ex.get(publishTo in Global in p)))
          st
        },
        enableCrossBuild = true
      )

      lazy val publishArtifactsPlugin = ReleaseStep(
        action = runAllTasks(PgpKeys.publishSigned in Global in otestSbtPlugin)(_),
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



