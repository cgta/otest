package cgta.otest
package runner

import sbt.testing.Logger
import cgta.otest.runner.TestResults.{FailedStringTrace, FailedFatalException, FailedUnexpectedException, FailedAssertion, FailedBad}


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 10:49 AM
//////////////////////////////////////////////////////////////

object LoggerHelp {
  def trace(e: Throwable, wasChained: Boolean): List[String] = {
    val prefix = if (wasChained) "Caused by: " else "Exception "
    val top = prefix + e.getClass.toString + ": " + e.getMessage
    val rest = e.getStackTrace.toList.map { ste =>
      "  at " + ste.toString
    }
    top :: rest ::: (if (e.getCause != null) trace(e, wasChained = true) else Nil)
  }


  def logResults(name: String, loggers: Array[Logger], results: Seq[TestResult]) {
    loggers.map(ColorLogger).foreach { logger =>
      logger.green(name + ":")
      results.foreach {
        case r: TestResults.Passed => logger.green(s"- ${r.name}")
        case r: TestResults.Ignored =>
          logger.yellow(s"- ${r.name} !!! IGNORED !!!")
        case r: TestResults.Failed =>
          logger.red(s"- ${r.name} *** FAILED ***")

          def logException(e: Throwable) {
            logTrace(trace(e, wasChained = false))
          }
          def logTrace(t: Seq[String]) {
            t.foreach { line =>
              logger.red("  " + line)
            }
          }

          r match {
            case f: FailedBad =>
            case f: FailedAssertion => logException(f.e)
            case f: FailedUnexpectedException => logException(f.e)
            case f: FailedFatalException => logException(f.e)
            case f: FailedStringTrace => logTrace(f.trace)
          }

      }
    }
  }

}

case class ColorLogger(logger: Logger) {
  def format(color: String, msg: String): String = {
    var m = ""
    if (logger.ansiCodesSupported()) {
      m += color
    }
    m += msg
    if (logger.ansiCodesSupported()) {
      m += Console.RESET
    }
    m
  }
  def red(msg: String) { logger.info(format(color = Console.RED, msg = msg)) }
  def green(msg: String) { logger.info(format(color = Console.GREEN, msg = msg)) }
  def yellow(msg: String) { logger.info(format(color = Console.YELLOW, msg = msg)) }
}