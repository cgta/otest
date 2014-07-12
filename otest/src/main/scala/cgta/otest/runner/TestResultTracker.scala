package cgta.otest
package runner

import scala.collection.mutable.ArrayBuffer
import sbt.testing.{TaskDef, EventHandler, Logger}


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 12:06 PM
//////////////////////////////////////////////////////////////

class TestResultTracker {
  object Tests {
    var passed  = 0
    var failed  = 0
    var errors  = 0
    var ignored = 0
    var pending = 0
    def total = passed + failed + errors + ignored + pending
  }
  object Suites {
    var completed = 0
    var aborted   = 0
  }
  var startUtcMs = 0L
  def begin() { startUtcMs = System.currentTimeMillis() }
  def durMs() = System.currentTimeMillis() - startUtcMs

  class SuiteTracker(taskDef: TaskDef, eventHandler: EventHandler) {
    def logResults(name: String, loggers: Array[Logger]) {
      LoggerHelp.logResults(name, loggers, results)
    }

    val results = new ArrayBuffer[TestResult]()
    def addResult(r: TestResult) {
      implicit val td = taskDef
      results += r
      eventHandler.handle(r)
      if (r.isFailed) {Tests.failed += 1}
      else if (r.isPassed) {Tests.passed += 1}
      else if (r.isIgnored) {Tests.ignored += 1}
      else if (r.isAborted) {Tests.errors += 1}
    }
  }

  def newSuiteTracker(taskDef: TaskDef, eventHandler: EventHandler) = new SuiteTracker(taskDef, eventHandler)

  def doneString: String = {
    import Tests._
    import Suites._
    //    s"Run completed in ${durMs()} milliseconds.\n" +
    //      s"Total number of tests run: $total\n" +
    //      s"Suites: completed ${Suites.completed}, aborted ${Suites.aborted}\n" +
    //      s"Tests: succeeded $passed, failed $failed, errors $errors, ignored $ignored, pending $pending\n"

    s"Suites: complete:$completed abort:$aborted Tests: ok:$passed, fail:$failed, error:$errors, ignore:$ignored\n"
  }

}