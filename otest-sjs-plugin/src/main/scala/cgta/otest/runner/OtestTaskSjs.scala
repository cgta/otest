package cgta.otest
package runner

import sbt.testing.{TaskDef, SubclassFingerprint, Task, Logger, EventHandler}
import cgta.otest.runner.TestResults.{FailedFatalException, FailedUnexpectedException, FailedAssertion, Passed, FailedBad, Ignored}
import scala.scalajs.tools.env.{JSEnv, JSConsole, ConsoleJSConsole}
import scala.scalajs.sbtplugin.testing.{SbtTestLoggerAccWrapper, TestOutputConsole}
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

  //See /home/bjackman/dev/github/scala-js/sbt-plugin/src/main/scala/scala/scalajs/sbtplugin/testing/TestTask.scala
  override def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
    tracker.begin()


    //    val testConsole =
    //      new TestOutputConsole(
    //        ConsoleJSConsole,
    //        eventHandler,
    //        loggers,
    //        new Events(taskDef),
    //        completeClasspath,
    //        noSourceMap = false)
    val code = testRunnerFile()
    val logger = new SbtTestLoggerAccWrapper(loggers)
    val testConsole = new JSConsole {
      override def log(msg: Any): Unit = println(msg)
    }

//    val pw = new PrintWriter("/home/bjackman/tmp/sjs.txt")
//    pw.write(completeClasspath.allCode.head.content)
//    pw.close()

    env.runJS(completeClasspath, code, logger, testConsole)

    Array()
  }

  private def testRunnerFile() = {
    val testKey = taskDef.fullyQualifiedName

    // Note that taskDef does also have the selector, fingerprint and
    // explicitlySpecified value we could pass to the framework. However, we
    // believe that these are only moderately useful. Therefore, we'll silently
    // ignore them.

    new MemVirtualJSFile("Generated Launcher for OtestSjs Suite Execution").
      withContent(s"""
        console.log("!!!!Hello World!!!")
        console.log(scala)
        console.log("TestKey"+OtestTaskRunnerSjs().runSuite())
      """)
  }

  def executeb(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
    tracker.begin()
    val name = taskDef.fullyQualifiedName()
    taskDef.fingerprint() match {
      case fingerprint: SubclassFingerprint if fingerprint.superclassName() == FrameworkHelp.funSuiteName =>
        if (fingerprint.isModule) {
          val cls = Class.forName(name + "$")
          runSuite(eventHandler, cls.getField("MODULE$").get(cls).asInstanceOf[FunSuite], loggers)(taskDef)
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