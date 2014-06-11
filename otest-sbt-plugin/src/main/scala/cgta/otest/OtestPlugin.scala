package cgta.otest

import sbt.{Setting, TestFramework}
import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys
import scala.scalajs.sbtplugin.env.nodejs.NodeJSEnv
import scala.scalajs.sbtplugin.testing.JSClasspathLoader


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 6/10/14 5:24 PM
//////////////////////////////////////////////////////////////

object OtestPlugin {
  lazy val settingsJvm = Seq[Setting[_]](
    sbt.Keys.testFrameworks += new TestFramework("cgta.otest.runner.OtestSbtFrameworkJvm")
  )

  lazy val settingsSjs = Seq[Setting[_]](
      (sbt.Keys.loadedTestFrameworks in sbt.Test) := {
        import cgta.otest.runner.OtestSbtFrameworkSjs
        (sbt.Keys.loadedTestFrameworks in sbt.Test).value.updated(
          sbt.TestFramework(classOf[OtestSbtFrameworkSjs].getName),
          new OtestSbtFrameworkSjs(env = (ScalaJSKeys.jsEnv in sbt.Test).value)
        )
      },
      (ScalaJSKeys.jsEnv in sbt.Test) := new NodeJSEnv,
      sbt.Keys.testLoader := JSClasspathLoader((ScalaJSKeys.execClasspath in sbt.Compile).value),
    sbt.Keys.testFrameworks += new TestFramework("cgta.otest.runner.OtestSbtFrameworkSjs")
  )
}