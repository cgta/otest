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
  override def name(): String = "otest-sjs"
  override def fingerprints(): Array[Fingerprint] = FrameworkHelp.fingerprints()

  override def runner(args: Array[String],
    remoteArgs: Array[String],
    testClassLoader: ClassLoader): Runner = {
    val jsClasspath = extractClasspath(testClassLoader)
    new OtestRunnerSjs(args, remoteArgs, jsClasspath, env)
  }

  /** extract classpath from ClassLoader (which must be a JSClasspathLoader) */
  private def extractClasspath(cl: ClassLoader) = cl match {
    case cl: JSClasspathLoader => cl.cp
    case _ =>
      sys.error("The Scala.js framework only works with a class loader of " +
          s"type JSClasspathLoader (${cl.getClass} given)")
  }


}