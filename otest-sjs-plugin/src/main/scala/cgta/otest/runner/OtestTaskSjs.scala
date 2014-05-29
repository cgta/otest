package cgta.otest
package runner

import sbt.testing.{TaskDef, SubclassFingerprint, Task, Logger, EventHandler}
import cgta.otest.runner.TestResults.{FailedStringTrace, FailedFatalException, FailedUnexpectedException, FailedAssertion, Passed, FailedBad, Ignored}
import scala.scalajs.tools.env.{JSEnv, JSConsole, ConsoleJSConsole}
import scala.scalajs.sbtplugin.testing.{SbtTestLoggerAccWrapper, TestOutputConsole}
import scala.scalajs.tools.classpath.CompleteClasspath
import scala.scalajs.tools.io.MemVirtualJSFile
import scala.util.parsing.json.JSON

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

  val initString = "xXxNeckJamBanxXx/"

  object Types {
    val trackerSuiteCompleted = "tracker-suite-completed"
    val trackerSuiteAborted   = "tracker-suite-aborted"
    val stLogResults          = "st-log-results"
    val stIgnored             = "st-ignored"
    val stPassed              = "st-passed"
    val stFailedBad           = "st-failed-bad"
    val stFailedAssert        = "st-failed-assert"
    val stFailedException     = "st-failed-exception"
    val stFailedFatal         = "st-failed-fatal"
    val stError               = "st-error"
  }

  override def tags(): Array[String] = Array()

  //See /home/bjackman/dev/github/scala-js/sbt-plugin/src/main/scala/scala/scalajs/sbtplugin/testing/TestTask.scala
  override def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
    tracker.begin()

    implicit val td = taskDef

    val testKey = taskDef.fullyQualifiedName

    val code = new MemVirtualJSFile("Generated Launcher for OtestSjs Suite Execution").
      withContent(s"""OtestTaskRunnerSjs().runSuite($testKey)""")
    val logger = new SbtTestLoggerAccWrapper(loggers)

    val st = tracker.newSuiteTracker(taskDef, eventHandler)
    val testConsole = new JSConsole {
      override def log(msg: Any): Unit = {
        val msgStr = "" + msg
        if (msgStr.startsWith(initString)) {
          //This is a command and control message from the underlying implementation
          val json = msgStr.drop(initString.length)
          JSON.parseFull(json) match {
            case Some(obj) =>
              val map = obj.asInstanceOf[Map[String, Any]]
              def name = map("n").asInstanceOf[String]
              def durMs = map("d").asInstanceOf[Double].toLong
              def trace = {
                println("TRACE CALLED", map)
                map("e").asInstanceOf[List[String]]
              }
              map("t") match {
                case Types.trackerSuiteCompleted => tracker.Suites.completed += 1
                case Types.trackerSuiteAborted => tracker.Suites.aborted += 1
                case Types.stLogResults => st.logResults(name, loggers)
                case Types.stIgnored => st.addResult(Ignored(name))
                case Types.stPassed => st.addResult(Passed(name, durMs))
                case Types.stFailedBad => st.addResult(FailedBad(name, durMs))
                case Types.stFailedAssert | Types.stFailedException =>
                  st.addResult(FailedStringTrace(name, trace, durMs, failed = true))
                case Types.stFailedFatal =>
                  st.addResult(FailedStringTrace(name, trace, durMs, aborted = true))
                case Types.stError => st.addResult(FailedBad(name, durMs))
              }
            case None =>
          }
        } else {
          println(msgStr)
        }
      }
    }

    env.runJS(completeClasspath, code, logger, testConsole)

    Array()
  }


  //  def executeb(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
  //    tracker.begin()
  //    val name = taskDef.fullyQualifiedName()
  //    taskDef.fingerprint() match {
  //      case fingerprint: SubclassFingerprint if fingerprint.superclassName() == FrameworkHelp.funSuiteName =>
  //        if (fingerprint.isModule) {
  //          val cls = Class.forName(name + "$")
  //          runSuite(eventHandler, cls.getField("MODULE$").get(cls).asInstanceOf[FunSuite], loggers)(taskDef)
  //        } else {
  //          sys.error("FunSuite only works on objects, classes don't work.")
  //        }
  //      case _ =>
  //    }
  //    Array()
  //  }
  //
  //  def runSuite(eventHandler: EventHandler, s: FunSuite, loggers: Array[Logger])(implicit taskDef: TaskDef) {
  //    val st = tracker.newSuiteTracker(taskDef, eventHandler)
  //    try {
  //      for (test <- s.SuiteImpl.tests) {
  //        runTest(test, st)
  //      }
  //      tracker.Suites.completed += 1
  //    } finally {
  //      st.logResults(s.SuiteImpl.simpleName, loggers)
  //    }
  //  }
  //
  //  def runTest(test: TestWrapper, st: TestResultTracker#SuiteTracker)(implicit taskDef: TaskDef) = {
  //    val startUtcMs = System.currentTimeMillis()
  //    def durMs = System.currentTimeMillis() - startUtcMs
  //    if (test.ignored) {
  //      st.addResult(Ignored(test.name))
  //    } else {
  //      try {
  //        test.body()
  //        st.addResult(if (test.bad) FailedBad(test.name, durMs) else Passed(test.name, durMs))
  //      } catch {
  //        case e: AssertionFailure =>
  //          st.addResult(if (test.bad) Passed(test.name, durMs) else FailedAssertion(test.name, e, durMs))
  //        case e if CatchableThrowable(e) =>
  //          st.addResult(FailedUnexpectedException(test.name, e, durMs))
  //        case e: Throwable =>
  //          st.addResult(FailedFatalException(test.name, e, durMs))
  //          tracker.Suites.aborted += 1
  //          throw e
  //      }
  //    }
  //  }

}