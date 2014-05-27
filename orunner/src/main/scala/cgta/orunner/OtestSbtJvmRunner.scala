package cgta.orunner


import cgta.otest.{CatchableThrowable, AssertionFailure, FunSuite}
import sbt.testing.{Task, SubclassFingerprint, Logger, EventHandler, TaskDef}
import scala.collection.mutable.ArrayBuffer


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

  //  println("CREATEEDDDD")
  //  object Tracker {
  //    private var passed  = 0
  //    private var failed  = 0
  //    private var ignored = 0
  //
  //    private var tasks: List[OTestTask] = Nil
  //
  //    def addTasks(tasks: List[OTestTask]) {
  //      synchronized {
  //        this.tasks :::= tasks
  //      }
  //    }
  //    def markPassed() = { synchronized {passed += 1} }
  //    def markFailed() = { synchronized {failed += 1} }
  //    def markIgnored() = { synchronized {ignored += 1} }
  //  }


  override def done(): String = {
    "DONE!!"
  }

  override def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    taskDefs.map { t => new OTestTask(t): Task}
  }

  class OTestTask(val taskDef: TaskDef) extends sbt.testing.Task {
    def tags(): Array[String] = Array()

    def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
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
          } else {
            try {
              test.body()
              if (test.bad) {
                results += TestResults.FailedBad(test.name)
              } else {
                results += TestResults.Passed(test.name)
              }
            } catch {
              case e: AssertionFailure =>
                if (test.bad) {
                  results += TestResults.Passed(test.name)
                } else {
                  results += TestResults.FailedAssertion(test.name, e)
                }
              case e if CatchableThrowable(e) =>
                results += TestResults.FailedUnexpectedException(test.name, e)
              case e: Throwable =>
                results += TestResults.FailedFatalException(test.name, e)
                throw e
            }
          }
        }
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
          }
        }
      }
    }
  }
}


//
//  val ClassTag  = scala.reflect.ClassTag
//  val TestSuite = framework.TestSuite
//  type TestSuite = framework.TestSuite
//
//  def runSuite(suite: TestSuite,
//    path: Array[String],
//    args: Array[String],
//    addCount: String => Unit,
//    log: String => Unit,
//    addTotal: String => Unit) = {
//    val (indices, found) = tests.resolve(path)
//    addTotal(found.length.toString)
//
//    implicit val ec =
//      if (utest.util.ArgParse.find("--parallel", _.toBoolean, false, true)(args)) {
//        concurrent.ExecutionContext.global
//      } else {
//        ExecutionContext.RunNow
//      }
//
//    val formatter = DefaultFormatter(args)
//    val results = tests.run(
//      (path, s) => {
//        addCount(s.value.isSuccess.toString)
//        log(formatter.formatSingle(path, s))
//      },
//      testPath = path
//    )(ec)
//    formatter.format(results)
//  }