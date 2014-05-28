package cgta.otest.runner

import sbt.testing.{Fingerprint, Runner}

import scala.scalajs.tools.env.JSEnv
import scala.scalajs.sbtplugin.testing.JSClasspathLoader

//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 4:35 PM
//////////////////////////////////////////////////////////////

class OtestSbtFrameworkSjs(env: JSEnv) extends sbt.testing.Framework {
  override def name(): String = "otest"
  override def fingerprints(): Array[Fingerprint] = FrameworkHelp.fingerprints()

  override def runner(args: Array[String],
    remoteArgs: Array[String],
    testClassLoader: ClassLoader): Runner = {
    println("OTEST RUNNNER SJSJSJSJSJSJ")
    new OtestRunnerSjs(args, remoteArgs, testClassLoader, env)
  }

}