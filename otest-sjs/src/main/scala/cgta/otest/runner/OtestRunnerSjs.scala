package cgta.otest.runner

import sbt.testing.{Task, TaskDef}
import scala.scalajs.tools.env.JSEnv

//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 4:42 PM
//////////////////////////////////////////////////////////////

class OtestRunnerSjs(
  override val args: Array[String],
  override val remoteArgs: Array[String],
  val testClassLoader: ClassLoader,
  env : JSEnv) extends sbt.testing.Runner {

  val tracker = new TestResultTracker

  override def done(): String = {
    tracker.doneString
  }

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    taskDefs.map { taskDef => new OtestTaskSjs(taskDef, tracker, testClassLoader, env): Task}
  }
}