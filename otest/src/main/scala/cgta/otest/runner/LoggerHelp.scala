package cgta.otest
package runner

import sbt.testing.Logger
import cgta.otest.runner.TestResults.{FailedFatalException, FailedUnexpectedException, FailedAssertion, FailedBad}


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 10:49 AM
//////////////////////////////////////////////////////////////

object LoggerHelp {
  def logResults(name : String, loggers: Array[Logger], results: Seq[TestResult]) {
    loggers.map(ColorLogger).foreach { logger =>
      logger.green(name + ":")
      results.foreach {
        case r: TestResults.Passed => logger.green(s"- ${r.name}")
        case r: TestResults.Ignored =>
          logger.yellow(s"- ${r.name} !!! IGNORED !!!")
        case r: TestResults.Failed =>
          logger.red(s"- ${r.name} *** FAILED ***")

          def trace(e: Throwable, wasChained: Boolean) {
            val prefix = if (wasChained) "  Caused by: " else "  Exception "
            logger.red(prefix + e.getClass.toString + ": " + e.getMessage)
            e.getStackTrace.foreach { ste =>
              logger.red("    at " + ste.toString)
            }
            if (e.getCause != null) {
              trace(e, wasChained = true)
            }
          }

          r match {
            case FailedBad(_, _) =>
            case FailedAssertion(_, e, _) => trace(e, wasChained = false)
            case FailedUnexpectedException(_, e, _) => trace(e, wasChained = false)
            case FailedFatalException(_, e, _) => trace(e, wasChained = false)
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