package cgta.otest
package runner

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js
import sbt.testing.{TaskDef, Logger, EventHandler}
import cgta.otest.TestWrapper


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 6:51 PM
//////////////////////////////////////////////////////////////

object OtestSjsConsoleProtocol {
  def initString = "xXxNeckJamBanxXx/"

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

}

import OtestSjsConsoleProtocol.Types

@JSExport
object OtestTaskRunnerSjs {
  val * = js.Dynamic.literal
  def echo(ss: js.Dynamic) {
    println(OtestSjsConsoleProtocol.initString + js.Dynamic.global.JSON.stringify(ss))
  }
  def getTrace(e : Throwable) : js.Array[String] = {
    val xs = js.Array[String]()
    xs.push(LoggerHelp.trace(e, wasChained = false) : _*)
    xs
  }


  @JSExport
  def runSuite(fn: js.Function0[FunSuite]) {
    val suite: FunSuite = fn()
    try {
      for (test <- suite.SuiteImpl.tests) {
        runTest(test)
      }
      echo(*(t = Types.trackerSuiteCompleted))
    } finally {
      echo(*(t = Types.stLogResults, n = suite.SuiteImpl.simpleName))
    }
  }


  def runTest(test: TestWrapper) = {
    val startUtcMs = System.currentTimeMillis()
    def durMs = System.currentTimeMillis() - startUtcMs
    if (test.ignored) {
      echo(*(t = Types.stIgnored, n = test.name))
    } else {
      try {
        test.body()
        if (test.bad) {
          echo(*(t = Types.stFailedBad, n = test.name, d = durMs))
        } else {
          echo(*(t = Types.stPassed, n = test.name, d = durMs))
        }
      } catch {
        case e: AssertionFailure =>
          if (test.bad) {
            echo(*(t = Types.stPassed, n = test.name, d = durMs))
          } else {
            echo(*(t = Types.stFailedAssert, n = test.name, d = durMs, e = getTrace(e)))
          }
        case e if CatchableThrowable(e) =>
          echo(*(t = Types.stFailedException, n = test.name, d = durMs, e = getTrace(e)))
        case e: Throwable =>
          echo(*(t = Types.stFailedFatal, n = test.name, d = durMs, e = getTrace(e)))
          echo(*(t = Types.trackerSuiteAborted))
          throw e
      }
    }
  }

}