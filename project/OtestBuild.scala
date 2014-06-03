import cgta.osbt.OsCgtaSbtPlugin
import sbt._
import sbt.Keys._

import sbtrelease.{ReleaseStateTransformations, ReleasePlugin, ReleaseStep}


object OtestBuild extends Build {
  import cgta.osbt.OsCgtaSbtPlugin._

  lazy val otestX = xprojects("otest")
    .settingsAll(libraryDependencies ++= (if (scalaVersion.value.startsWith("2.10.")) Libs.macrosQuasi else Nil))
    .settingsAll(CompilerPlugins.macrosPlugin)
    .settingsAll(libraryDependencies ++= Libs.sbtTestInterface)
    .settingsAll(libraryDependencies += Libs.scalaReflect % scalaVersion.value)
    .settingsAll(Bintray.repo("cgta-maven-releases"))
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

  lazy val otestPlugin = Project("otest-sjs-plugin", file("./otest-sjs-plugin"))
    .settings(basicSettings: _*)
    .settings(libraryDependencies ++= Libs.sbtTestInterface)
    .settings(libraryDependencies += Libs.scalaReflect % scalaVersion.value)
    .settings(SbtPlugins.scalaJs)
    .settings(sbtPlugin := true)
    .settings(Bintray.repo("sbt-plugins"))
    .dependsOn(otestJvm)


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
          checkSnapshotDependencies,
          inquireVersions,
          runClean,
          CgtaSteps.runTestOtest,
          CgtaSteps.runTestPlugin, //Make sure to force a load of dependencies!
          setReleaseVersion,
          commitReleaseVersion, // performs the initial git checks
          tagRelease,
          CgtaSteps.publishArtifactsOtest, // checks whether `publishTo` is properly set up
          CgtaSteps.publishArtifactsPlugin, // checks whether `publishTo` is properly set up
          setNextVersion,
          commitNextVersion,
          pushChanges // also checks that an upstream branch is properly configured
        )
      }
    )
  }

  object CgtaSteps {
    lazy val runTestOtest           = ReleaseStep(action = st0 => {
      (for {
        (st1, _) <- Project.runTask(test in Test in otestJvm, st0)
        (st2, _) <- Project.runTask(test in Test in otestSjs, st1)
      } yield {
        st2
      }).get
    },
      enableCrossBuild = true
    )
    lazy val runTestPlugin          = ReleaseStep(action = st0 => {
      (for {
        (st1, _) <- Project.runTask(test in Test in otestPlugin, st0)
      } yield {
        st1
      }).get
    }
    )
    lazy val publishArtifactsOtest  = ReleaseStep(
      action = st0 => {
        (for {
          (st1, _) <- Project.runTask(publish in Global in otestJvm, st0)
          (st2, _) <- Project.runTask(publish in Global in otestSjs, st1)
        } yield {
          st2
        }).get
      },
      check = st => {
        // getPublishTo fails if no publish repository is set up.
        val ex = Project.extract(st)
        Classpaths.getPublishTo(ex.get(publishTo in Global in otestJvm))
        Classpaths.getPublishTo(ex.get(publishTo in Global in otestSjs))
        st
      },
      enableCrossBuild = true
    )
    lazy val publishArtifactsPlugin = ReleaseStep(
      action = st0 => {
        (for {
          (st1, _) <- Project.runTask(publish in Global in otestPlugin, st0)
        } yield {
          st1
        }).get
      },
      check = st => {
        val ex = Project.extract(st)
        Classpaths.getPublishTo(ex.get(publishTo in Global in otestPlugin))
        st
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
