package cgta.otest
package runner

import cgta.otest.{CatchableThrowable, AssertionFailure, FunSuite}
import sbt.testing.{Status, Selector, Fingerprint, OptionalThrowable, TaskDef, Task, SubclassFingerprint, Logger, EventHandler}
import scala.collection.mutable.ArrayBuffer
import cgta.otest.runner.TestResults.{Ignored, FailedBad, Passed, FailedAssertion, FailedUnexpectedException, FailedFatalException}


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 4:01 PM
//////////////////////////////////////////////////////////////


sealed trait TestResult extends sbt.testing.Event {
  val taskDef: TaskDef
  override def selector(): Selector = taskDef.selectors().head
  override def fingerprint(): Fingerprint = taskDef.fingerprint()
  override def fullyQualifiedName(): String = taskDef.fullyQualifiedName()
  def name: String
  def isPassed: Boolean = false
  def isFailed: Boolean = false
  def isAborted: Boolean = false
  def isIgnored: Boolean = false
}
object TestResults {
  case class Passed(name: String, duration: Long)(implicit val taskDef: TaskDef) extends TestResult {
    override val isPassed = true
    override def throwable(): OptionalThrowable = new OptionalThrowable()
    override def status(): Status = Status.Success
  }
  case class Ignored(name: String)(implicit val taskDef: TaskDef) extends TestResult {
    val duration = 0L
    override val isIgnored = true
    override def throwable(): OptionalThrowable = new OptionalThrowable()
    override def status(): Status = Status.Ignored
  }
  sealed trait Failed extends TestResult
  case class FailedBad(name: String, duration: Long)(implicit val taskDef: TaskDef) extends Failed {
    override val isFailed = true
    override def throwable(): OptionalThrowable = new OptionalThrowable()
    override def status(): Status = Status.Failure
  }
  case class FailedAssertion(
    name: String, e: AssertionFailure, duration: Long)(implicit val taskDef: TaskDef) extends Failed {
    override val isFailed = true
    override def throwable(): OptionalThrowable = new OptionalThrowable(e)
    override def status(): Status = Status.Failure
  }
  case class FailedUnexpectedException(
    name: String, e: Throwable, duration: Long)(implicit val taskDef: TaskDef) extends Failed {
    override val isFailed = true
    override def throwable(): OptionalThrowable = new OptionalThrowable(e)
    override def status(): Status = Status.Failure

  }
  case class FailedFatalException(
    name: String, e: Throwable, duration: Long)(implicit val taskDef: TaskDef) extends Failed {
    override val isAborted = true
    override def throwable(): OptionalThrowable = new OptionalThrowable(e)
    override def status(): Status = Status.Error
  }
}


class OtestSbtJvmRunner(
  override val args: Array[String],
  override val remoteArgs: Array[String],
  val testClassLoader: ClassLoader) extends sbt.testing.Runner {

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
    val res = s"Run completed in ${Tracker.durMs()} milliseconds.\n" +
      s"Total number of tests run: $total\n" +
      s"Suites: completed ${Suites.completed}, aborted ${Suites.aborted}\n" +
      s"Tests: succeeded $passed, failed $failed, aborted $aborted, ignored $ignored, pending $pending\n"

    res
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
        case fingerprint: SubclassFingerprint if fingerprint.superclassName() == OtestSbtFramework.funSuiteName =>
          if (fingerprint.isModule) {
            val cls = Class.forName(name + "$")
            runSuite(eventHandler, cls.getField("MODULE$").get(cls).asInstanceOf[FunSuite], loggers)
          } else {
            sys.error("FunSuite only works on objects, classes don't work.")
          }
        case _ =>
      }

      Array()
    }

    def runSuite(eventHandler: EventHandler, s: FunSuite, loggers: Array[Logger]) {
      val results = ArrayBuffer[TestResult]()
      def addResult(r: TestResult) {
        results += r
        eventHandler.handle(r)
        if (r.isFailed) Tracker.Tests.failed += 1
        else if (r.isPassed) Tracker.Tests.passed += 1
        else if (r.isIgnored) Tracker.Tests.ignored += 1
        else if (r.isAborted) Tracker.Tests.aborted += 1
      }
      implicit val td = taskDef
      try {
        for (test <- s.SuiteImpl.tests) {
          val startUtcMs = System.currentTimeMillis()
          def durMs = System.currentTimeMillis() - startUtcMs
          if (test.ignored) {
            addResult(Ignored(test.name))
          } else {
            try {
              test.body()
              addResult(if (test.bad) FailedBad(test.name, durMs) else Passed(test.name, durMs))
            } catch {
              case e: AssertionFailure =>
                addResult(if (test.bad) Passed(test.name, durMs) else FailedAssertion(test.name, e, durMs))
              case e if CatchableThrowable(e) =>
                addResult(FailedUnexpectedException(test.name, e, durMs))
              case e: Throwable =>
                addResult(FailedFatalException(test.name, e, durMs))
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
                case FailedBad(_, _) =>
                case FailedAssertion(_, e, _) => trace(e, wasChained = false)
                case FailedUnexpectedException(_, e, _) => trace(e, wasChained = false)
                case FailedFatalException(_, e, _) => trace(e, wasChained = false)
              }

          }
        }
      }
    }
  }
}


