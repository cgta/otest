package cgta.otest

import sbt._
import sbt.{Setting, TestFramework}
import scala.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys
import cgta.otest.runner.OtestSbtFrameworkSjs

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


  private lazy val otestTestFrameworkSettings = Seq(
    sbt.Keys.loadedTestFrameworks +=
      sbt.TestFramework("cgta.otest.runner.OtestSbtFrameworkSjs") ->
        new OtestSbtFrameworkSjs(env = ScalaJSKeys.jsEnv.value)
  )

  private lazy val otestTestSettings = otestTestFrameworkSettings ++
    inTask(ScalaJSKeys.packageStage)(otestTestFrameworkSettings) ++
    inTask(ScalaJSKeys.fastOptStage)(otestTestFrameworkSettings) ++
    inTask(ScalaJSKeys.fullOptStage)(otestTestFrameworkSettings)


  lazy val settingsSjs = inConfig(sbt.Test)(otestTestSettings)
}
