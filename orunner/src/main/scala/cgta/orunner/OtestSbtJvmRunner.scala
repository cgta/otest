package cgta.orunner


import cgta.otest.{CatchableThrowable, AssertionFailure, FunSuite}
import sbt.testing.{Task, SubclassFingerprint, Logger, EventHandler, TaskDef}
import scala.collection.mutable.ArrayBuffer
import cgta.orunner.TestResults.{FailedBad, FailedFatalException, FailedUnexpectedException, FailedAssertion}


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 4:01 PM
//////////////////////////////////////////////////////////////


sealed trait TestResult {
  def name: String
  def isPassed: Boolean = false
  def isFailed: Boolean = false
  def isIgnored: Boolean = false
}
object TestResults {
  case class Passed(name: String) extends TestResult {
    override val isPassed = true
  }
  case class Ignored(name: String) extends TestResult {
    override val isIgnored = true
  }
  sealed trait Failed extends TestResult
  case class FailedBad(name: String) extends Failed {
    override val isFailed = true
  }
  case class FailedAssertion(name: String, e: AssertionFailure) extends Failed {
    override val isFailed = true
  }
  case class FailedUnexpectedException(name: String, e: Throwable) extends Failed {
    override val isFailed = true
  }
  case class FailedFatalException(name: String, e: Throwable) extends Failed {
    override val isFailed = true
  }
}


class OtestSbtJvmRunner(
  override val args: Array[String],
  override val remoteArgs: Array[String]) extends sbt.testing.Runner {

  object Tracker {
    object Tests {
      var passed  = 0
      var failed  = 0
      var aborted = 0
      var ignored = 0
      var pending = 0
      def total = passed + failed + aborted + ignored + pending
    }
    object Suites {
      var completed = 0
      var aborted   = 0
    }
    var startUtcMs = 0L
    def durMs() = System.currentTimeMillis() - startUtcMs
  }


  override def done(): String = {
    import Tracker.Tests._
    import Tracker.Suites
    s"Run completed in ${Tracker.durMs()} milliseconds.\n" +
      s"Total number of tests run: $total\n" +
      s"Suites: completed ${Suites.completed}, aborted ${Suites.aborted}\n" +
      s"Tests: succeeded $passed, failed $failed, aborted $aborted, ignored $ignored, pending $pending\n"

  }

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    taskDefs.map { t => new OTestTask(t): Task}
  }

  class OTestTask(val taskDef: TaskDef) extends sbt.testing.Task {
    def tags(): Array[String] = Array()

    def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
      Tracker.startUtcMs = System.currentTimeMillis()
      val name = taskDef.fullyQualifiedName()
      taskDef.fingerprint() match {
        case fingerprint: SubclassFingerprint =>
          if (fingerprint.isModule) {
            val cls = Class.forName(name + "$")
            runSuite(cls.getField("MODULE$").get(cls).asInstanceOf[FunSuite], loggers)
          } else {
            sys.error("Using FunSuite only works on objects not on classes")
          }
        case _ =>
      }
      Array()
    }

    def runSuite(s: FunSuite, loggers: Array[Logger]) {
      val results = ArrayBuffer[TestResult]()
      try {
        for (test <- s.SuiteImpl.tests) {
          if (test.ignored) {
            results += TestResults.Ignored(test.name)
            Tracker.Tests.ignored += 1
          } else {
            try {
              test.body()
              if (test.bad) {
                results += TestResults.FailedBad(test.name)
                Tracker.Tests.failed += 1
              } else {
                results += TestResults.Passed(test.name)
                Tracker.Tests.passed += 1
              }
            } catch {
              case e: AssertionFailure =>
                if (test.bad) {
                  results += TestResults.Passed(test.name)
                  Tracker.Tests.passed += 1
                } else {
                  results += TestResults.FailedAssertion(test.name, e)
                  Tracker.Tests.failed += 1
                }
              case e if CatchableThrowable(e) =>
                results += TestResults.FailedUnexpectedException(test.name, e)
                Tracker.Tests.failed += 1
              case e: Throwable =>
                results += TestResults.FailedFatalException(test.name, e)
                Tracker.Tests.aborted += 1
                Tracker.Suites.aborted += 1
                throw e
            }
          }
        }
        Tracker.Suites.completed += 1
      } finally {
        loggers.foreach { logger =>
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
          logger.info(format(color = Console.GREEN, msg = s.SuiteImpl.simpleName + ":"))
          results.foreach {
            case r: TestResults.Passed => logger.info(format(color = Console.GREEN, msg = s"- ${r.name}"))
            case r: TestResults.Ignored =>
              logger.info(format(color = Console.YELLOW, msg = s"- ${r.name} !!! IGNORED !!!"))
            case r: TestResults.Failed =>
              logger.info(format(color = Console.RED, msg = s"- ${r.name} *** FAILED ***"))

              def trace(e: Throwable, wasChained: Boolean) {
                val prefix = if (wasChained) "  Caused by: " else "  Exception "
                logger.info(format(color = Console.RED, msg = prefix + e.getClass.toString + ": " + e.getMessage))
                e.getStackTrace.foreach { ste =>
                  logger.info(format(color = Console.RED, msg = "    at " + ste.toString))
                }
                if (e.getCause != null) {
                  trace(e, wasChained = true)
                }
              }

              r match {
                case FailedBad(_) =>
                case FailedAssertion(_, e) => trace(e, wasChained = false)
                case FailedUnexpectedException(_, e) => trace(e, wasChained = false)
                case FailedFatalException(_, e) => trace(e, wasChained = false)
              }

          }
        }
      }
    }
  }
}


