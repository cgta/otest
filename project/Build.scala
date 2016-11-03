import sbt._
import sbt.Keys._

import sbtrelease.{ReleaseStateTransformations, ReleasePlugin, ReleaseStep}
import scala.annotation.tailrec

import com.typesafe.sbt.pgp.PgpKeys
import com.typesafe.sbt.pgp.PgpKeys._

import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.sbtplugin.cross.CrossProject



object Build extends sbt.Build {

  import BaseBuild._

//  //  org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[(ch.qos.logback.classic.Logger)].setLevel(ch.qos.logback.classic.Level.INFO)
  object Versions {
    //Change in plugins too!!
    val scalaJSVersion = "0.6.13"
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
//
//  object CompilerPlugins {
//    lazy val macrosPlugin = addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0-M5" cross CrossVersion.full)
//  }

  object Libs {
    //    lazy val scalaJsPlugin     = Seq("org.scala-lang.modules.scalajs" %% "scalajs-plugin" % Versions.scalaJs)
    val scalaReflect = "org.scala-lang" % "scala-reflect"
  }


//  lazy val oscala = crossProject.in(file("oscala")).configure(xp("oscala", _))
//    .jsSettings(Libs.dom.settings: _*)
//    .jsConfigure(_.copy(id = "oscalaSJS"))
//    .settings(sbtide.Keys.ideBasePackages :=  List("cgta.oscala"))
//    .jvmSettings(Assembly.settings: _*)
//
//  lazy val oscalaJVM = oscala.jvm
//  lazy val oscalaSJS = oscala.js
//
//

//  lazy val (otestX, otest, otestJvm, otestSjs, otestJvmTest, otestSjsTest) = SbtXSjsPlugin.XSjsProjects("otest", file("otest"))
//    .settingsAll(organization := "biz.cgta")
//    .settingsAll(PublishSets.settings: _*)
//    .settingsAll(publishMavenStyle := true)
//    .settingsAll(SbtIdeaPlugin.ideaBasePackage := Some("cgta.otest"))
//    .settingsAll(OsCgtaSbtPlugin.basicSettings: _*)
//    .settingsAll(libraryDependencies ++= (if (scalaVersion.value.startsWith("2.10.")) Libs.macrosQuasi else Nil))
//    .settingsAll(CompilerPlugins.macrosPlugin)
//    .settingsAll(libraryDependencies += Libs.scalaReflect % scalaVersion.value)
//    .settingsJvm(
//      libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0",
//      libraryDependencies += "org.scala-js" %% "scalajs-stubs" % Versions.scalaJSVersion % "provided"
//    )
//    .mapSjs(_.enablePlugins(ScalaJSPlugin))
//    .settingsSjs(
//      libraryDependencies += "org.scala-js" %% "scalajs-test-interface" % Versions.scalaJSVersion,
//      testFrameworks := Seq(new TestFramework("otest.runner.Framework"))
//    )
//    .tupledWithTests
//

    lazy val otest = crossProject.in(file("otest")).configure(xp("otest", _))
      .jsConfigure(_.copy(id = "otestSJS"))
    .settings(organization := "biz.cgta")
//    .settings(CompilerPlugins.macrosPlugin)
    .settings(PublishSets.settings: _*)
    .settings(publishMavenStyle := true)
    .settings(libraryDependencies += Libs.scalaReflect % scalaVersion.value)
    .jvmSettings(
      libraryDependencies += "org.scala-sbt" % "test-interface" % "1.0",
      libraryDependencies += "org.scala-js" %% "scalajs-stubs" % Versions.scalaJSVersion % "provided"
    )
    .jsSettings(
      libraryDependencies += "org.scala-js" %% "scalajs-test-interface" % Versions.scalaJSVersion,
      testFrameworks := Seq(new TestFramework("otest.runner.Framework"))
    )

    lazy val otestJVM = otest.jvm
    lazy val otestSJS = otest.js


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

      lazy val otests = Seq(otestJVM, otestSJS)

      lazy val runTestOtest = ReleaseStep(
        action = runAllTasks(otests.map(test in Test in _): _*)(_),
        enableCrossBuild = true
      )

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

    }

    lazy val settings = Seq[Setting[_]](
      ReleasePlugin.ReleaseKeys.releaseProcess := {
        import ReleaseStateTransformations._
        Seq[ReleaseStep](
          checkSnapshotDependencies,
          inquireVersions,
          runClean,
          CgtaSteps.runTestOtest,
          setReleaseVersion,
          commitReleaseVersion,
          tagRelease,
          CgtaSteps.publishArtifactsOtest,
          setNextVersion,
          commitNextVersion,
          pushChanges
        )
      }
    )
  }


  lazy val root = Project("root", file("."))
    .aggregate(otestJVM, otestSJS)
    .settings(crossScalaVersions := Seq("2.11.8", "2.12.0"))
    .settings(sbtrelease.ReleasePlugin.releaseSettings: _*)
    .settings(ReleaseProcess.settings: _*)
    .settings(publish :=())
    .settings(publishLocal :=())
}



