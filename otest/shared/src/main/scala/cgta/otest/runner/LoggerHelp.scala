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
  lazy val isScalaJS = {
    try {
      1/0
      true
    } catch {
      case e : ArithmeticException => false
    }
  }

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
    var passedAsserts = false
    @tailrec
    def loop(itr: Iterator[Either[String, StackTraceElement]]) {
      if (itr.hasNext) {
        val cur = itr.next()
        cur match {
          case Left(msg) =>
            lb += (if (first) "Exception " else "Caused by: ") + msg
            first = false
            loop(itr)
          case Right(ste) =>
            val steStr = ste.toString
            if (isScalaJS) {
              lb += "  at " + steStr
              loop(itr)
            } else {
              val isCgtaOtestCode = steStr.startsWith("cgta.otest.")
              if (passedAsserts) {
                if (isCgtaOtestCode) {
                  //Back into the guts of the runner, therefore stop printing the trace
                } else {
                  lb += "  at " + steStr
                  loop(itr)
                }
              } else {
                if (isCgtaOtestCode) {
                  //Not yet out of the assertion exception areas
                  loop(itr)
                } else {
                  passedAsserts = true
                  lb += "  at " + steStr
                  loop(itr)
                }
              }
            }
        }
      }
    }
    loop(t.iterator)
    lb.toList
  }


  def logResults(name: String, loggers: Array[Logger], results: Seq[TestResult]) {
    loggers.map(ColorLogger).foreach { logger =>
      val trimmedName = if (name.endsWith("$")) name.dropRight(1) else name
      val hasIgnoreOnly = results.exists(_.isIgnoredOnly)
      val ignoreOnlyMsg = if (hasIgnoreOnly) "(Ignored some test b/c testOnly was used)" else ""
      val msg = s"$trimmedName:$ignoreOnlyMsg [${results.map(_.duration()).sum}ms]"
      if (results.exists(_.isFailed)) {
        logger.redError(msg)
      } else if(hasIgnoreOnly)  {
        logger.yellowInfo(msg)
      } else  {
        logger.logger.info(msg)
      }
      results.foreach {
        case r: TestResults.Passed => logger.green(s"[${r.duration}ms] - ${r.name}")
        case r: TestResults.Ignored if r.becauseOnly =>
          //No logging here
        case r: TestResults.Ignored =>
          logger.yellowInfo(s"- ${r.name} !!! IGNORED !!!")
        case r: TestResults.Failed =>
          logger.redError(s"- ${r.name} *** FAILED ***")

          def logException(e: Throwable) {
            logTraceStr(trace(e))
          }
          def logEitherTrace(t: Seq[Either[String, StackTraceElement]]) {
            logTraceStr(trace(t))
          }
          def logTraceStr(t: Seq[String]) {
            t.foreach { line =>
              logger.redError("  " + line)
            }
          }

          r match {
            case f: FailedBad =>
              //Failed bad tests are actually expected to fail
            case f: FailedAssertion =>
              f.e match {
                case e : SimpleAssertionException =>
                  val acStr = e.actual.toString
                  val exStr = e.expected.toString
                  logTraceStr(trace(e.getStackTrace.map(Right(_))))
                  logger.redError(s"Expected `A` ${e.op} `B`")
                  logger.redError(s"A: $exStr")
                  logger.redError(s"B: $acStr")
                  if (e.clues.nonEmpty) {
                    logger.redError("Clues:")
                    e.clues.foreach(c => logger.redError(c.toString))
                  }
                case e =>
                  logException(e)
              }
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
    if (color.nonEmpty) {
      var m = ""
      if (logger.ansiCodesSupported()) {
        m += color
      }
      m += msg
      if (logger.ansiCodesSupported()) {
        m += Console.RESET
      }
      m
    } else {
      msg
    }
  }
  def redError(msg: String) { logger.error(format(color = Console.RED, msg = msg)) }
  def green(msg: String) { logger.info(format(color = Console.GREEN, msg = msg)) }
  def yellowInfo(msg: String) { logger.info(format(color = Console.YELLOW, msg = msg)) }
}