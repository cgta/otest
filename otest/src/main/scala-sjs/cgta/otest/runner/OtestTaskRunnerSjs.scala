package cgta.otest
package runner

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js
import cgta.otest.TestWrapper
import scala.annotation.tailrec


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 6:51 PM
//////////////////////////////////////////////////////////////

import OtestConsoleProtocol.Types

@JSExport
object OtestTaskRunnerSjs {
  val * = js.Dynamic.literal
  def echo(ss: js.Dynamic) {
    println(OtestConsoleProtocol.initString + js.Dynamic.global.JSON.stringify(ss))
  }

  def getTrace(e: Throwable): js.Array[Any] = {
    val xs = js.Array[Any]()
    def stackTraceElementToJsObj(ste: StackTraceElement): js.Object = {
      import scala.scalajs.runtime.StackTrace.ColumnStackTraceElement
      *(cn = ste.getClassName,
        mn = ste.getMethodName,
        fn = ste.getFileName,
        l = ste.getLineNumber,
        c = ste.getColumnNumber
      ).asInstanceOf[js.Object]
    }

    @tailrec
    def loop(e: Throwable) {
      xs.push(e.getClass + ": " + e.getMessage)
      xs.push(e.getStackTrace.map(stackTraceElementToJsObj): _*)
      val cause = e.getCause
      if (cause != null) loop(cause)
    }
    loop(e)
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