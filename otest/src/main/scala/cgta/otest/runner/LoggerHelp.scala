package cgta.otest
package runner

import sbt.testing.Logger
import cgta.otest.runner.TestResults.{FailedWithEitherTrace, FailedFatalException, FailedUnexpectedException, FailedAssertion, FailedBad}
import scala.collection.mutable.ListBuffer
import scala.annotation.tailrec


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 10:49 AM
//////////////////////////////////////////////////////////////

object LoggerHelp {

  def trace(e: Throwable): List[String] = {
    val lb = new ListBuffer[Either[String, StackTraceElement]]
    @tailrec
    def loop(e: Throwable) {
      lb += Left(e.getClass + ": " + e.getMessage)
      lb ++= e.getStackTrace.map(Right(_))
      val cause = e.getCause
      if (cause != null) loop(cause)
    }
    loop(e)
    trace(lb.toList)
  }

  def trace(t: Seq[Either[String, StackTraceElement]]): List[String] = {
    val lb = new ListBuffer[String]
    var first = true
    t.foreach {
      case Left(msg) =>
        lb += (if (first) "Exception " else "Caused by: ") + msg
        first = false
      case Right(ste) =>
        lb += "  at " + ste.toString
    }
    lb.toList
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
            logTraceStr(trace(e))
          }
          def logEitherTrace(t : Seq[Either[String, StackTraceElement]]) {
            logTraceStr(trace(t))
          }
          def logTraceStr(t: Seq[String]) {
            t.foreach { line =>
              logger.red("  " + line)
            }
          }

          r match {
            case f: FailedBad =>
            case f: FailedAssertion => logException(f.e)
            case f: FailedUnexpectedException => logException(f.e)
            case f: FailedFatalException => logException(f.e)
            case f: FailedWithEitherTrace => logEitherTrace(f.trace)
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