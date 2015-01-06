package cgta.otest
package runner

import sbt.testing.{Task, SubclassFingerprint, TaskDef, Logger, EventHandler}
import cgta.otest.runner.TestResults.{FailedFatalException, Ignored, FailedBad, Passed, FailedAssertion, FailedUnexpectedException}
import org.scalajs.testinterface.TestUtils


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 12:00 PM
//////////////////////////////////////////////////////////////

class OtestTask(
  val taskDef: TaskDef,
  tracker: TestResultTracker,
  testClassLoader: ClassLoader) extends sbt.testing.Task {

  override def tags(): Array[String] = Array()

  def execute(eventHandler: EventHandler, loggers: Array[Logger], continuation: (Array[Task]) => Unit): Unit = {
    continuation(execute(eventHandler, loggers))
  }

  override def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
    tracker.begin()
    val name = taskDef.fullyQualifiedName()
    taskDef.fingerprint() match {
      case fingerprint: SubclassFingerprint if fingerprint.superclassName() == FrameworkHelp.funSuiteName =>
        if (fingerprint.isModule) {
          TestUtils.loadModule(name, testClassLoader) match {
            case m : FunSuite =>
              runSuite(eventHandler, m, loggers)(taskDef)
            case x =>
              sys.error(s"Cannot test $taskDef of type: $x")
          }
        } else {
          sys.error("FunSuite only works on objects, classes don't work.")
        }
      case _ =>
    }
    Array()
  }

  def runSuite(eventHandler: EventHandler, s: FunSuite, loggers: Array[Logger])(implicit taskDef: TaskDef) {
    val st = tracker.newSuiteTracker(taskDef, eventHandler)
    try {
      for (test <- s.SuiteImpl.tests) {
        runTest(test, st)
      }
      tracker.Suites.completed += 1
    } finally {
      st.logResults(s.SuiteImpl.simpleName, loggers)
    }
  }

  def runTest(test: TestWrapper, st: TestResultTracker#SuiteTracker)(implicit taskDef: TaskDef) = {
    val startUtcMs = System.currentTimeMillis()
    def durMs = System.currentTimeMillis() - startUtcMs
    if (test.ignored) {
      st.addResult(Ignored(test.name))
    } else {
      try {
        test.body()
        st.addResult(if (test.bad) FailedBad(test.name, durMs) else Passed(test.name, durMs))
      } catch {
        case e: AssertionFailure =>
          st.addResult(if (test.bad) Passed(test.name, durMs) else FailedAssertion(test.name, e, durMs))
        case e if CatchableThrowable(e) =>
          st.addResult(FailedUnexpectedException(test.name, e, durMs))
        case e: Throwable =>
          st.addResult(FailedFatalException(test.name, e, durMs))
          tracker.Suites.aborted += 1
          throw e
      }
    }
  }

}