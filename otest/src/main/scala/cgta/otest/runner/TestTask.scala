package cgta.otest
package runner

import sbt.testing.TaskDef


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 12:06 PM
//////////////////////////////////////////////////////////////

trait ITestTask extends sbt.testing.Task {
  val taskDef: TaskDef
  val tracker: TestResultTracker
  val testClassLoader: ClassLoader
  def tags(): Array[String] = Array()
}

class TestTask(
  val taskDef: TaskDef,
  val tracker: TestResultTracker,
  val testClassLoader: ClassLoader) extends ITestTask with TestTaskImpl