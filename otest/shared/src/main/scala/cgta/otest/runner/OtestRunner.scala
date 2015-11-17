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

class OtestRunner(
  override val args: Array[String],
  override val remoteArgs: Array[String],
  val testClassLoader: ClassLoader) extends sbt.testing.Runner {

  val tracker = new TestResultTracker

  override def done(): String = {
    tracker.doneString
  }

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    taskDefs.map { taskDef => new OtestTask(taskDef, tracker, testClassLoader): Task}
  }

  def receiveMessage(msg: String): Option[String] = {
    None
  }
  def serializeTask(task: sbt.testing.Task, serializer: sbt.testing.TaskDef => String): String = {
    serializer(task.taskDef())
  }
  def deserializeTask(task: String, deserializer: String => sbt.testing.TaskDef): sbt.testing.Task = {
    new OtestTask(deserializer(task), tracker, testClassLoader)
  }


}

