package cgta.otest
package runner

import sbt.testing.{TaskDef, Task}


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 4:37 PM
//////////////////////////////////////////////////////////////

class OtestRunnerJvm(
  override val args: Array[String],
  override val remoteArgs: Array[String],
  val testClassLoader: ClassLoader) extends sbt.testing.Runner {

  val tracker = new TestResultTracker

  override def done(): String = {
    tracker.doneString
  }

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    taskDefs.map { taskDef => new OtestTaskJvm(taskDef, tracker, testClassLoader): Task}
  }
}

