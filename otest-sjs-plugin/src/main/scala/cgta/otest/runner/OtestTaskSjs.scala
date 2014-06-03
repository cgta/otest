package cgta.otest
package runner

import sbt.testing.{SubclassFingerprint, TaskDef, Task, Logger, EventHandler}
import scala.scalajs.tools.env.JSEnv
import scala.scalajs.sbtplugin.testing.SbtTestLoggerAccWrapper
import scala.scalajs.tools.classpath.CompleteClasspath
import scala.scalajs.tools.io.MemVirtualJSFile

//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 12:19 PM
//////////////////////////////////////////////////////////////




class OtestTaskSjs(
  val taskDef: TaskDef,
  tracker: TestResultTracker,
  completeClasspath: CompleteClasspath,
  env: JSEnv) extends sbt.testing.Task {

  override def tags(): Array[String] = Array()

  override def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
    implicit val td = taskDef
    val testKey = taskDef.fullyQualifiedName

    tracker.begin()

    taskDef.fingerprint() match {
      case fingerprint: SubclassFingerprint if fingerprint.superclassName() == FrameworkHelp.funSuiteName =>
        if (fingerprint.isModule) {
          runSuite()
        } else {
          sys.error("FunSuite only works on objects, classes don't work.")
        }
      case _ =>
    }

    def runSuite() {
      val code = new MemVirtualJSFile("Generated Launcher for OtestSjs Suite Execution").
        withContent(s"""OtestTaskRunnerSjs().runSuite($testKey)""")
      val logger = new SbtTestLoggerAccWrapper(loggers)
      val testConsole = new OtestJsConsole(tracker, eventHandler, loggers, completeClasspath)
      env.runJS(completeClasspath, code, logger, testConsole)
    }

    Array()
  }
}