package cgta.orunner


import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import cgta.otest.{AssertionFailure, FunSuite}
import sbt.testing.{Task, SubclassFingerprint, Logger, EventHandler, TaskDef}
import scala.util.control.NonFatal


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 4:01 PM
//////////////////////////////////////////////////////////////


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
      println(s"Lets pretend I am running a test [$taskDef] [$s] [$loggers] [$name]")

      taskDef.fingerprint() match {
        case fingerprint: SubclassFingerprint =>
          if (fingerprint.isModule) {
            val cls = Class.forName(name + "$")
            runSuite(cls.getField("MODULE$").get(cls).asInstanceOf[FunSuite])
          } else {
            sys.error("Using FunSuite only works on objects not on classes")
          }
        case _ =>
      }

      Array()
    }

    def runSuite(s: FunSuite) {
      for (test <- s.SuiteImpl.tests) {
        def testPassed() {

        }

        def assertionFailed(e: AssertionFailure) {
        }

        def unexpectedException(e: Throwable) {

        }

        def testIgnored() {

        }

        if (test.ignored) {
          testIgnored()
        } else {
          try {
            test.body
            testPassed()
          } catch {
            case e: AssertionFailure => assertionFailed(e)
            case e if NonFatal(e) => unexpectedException(e)
          }
        }
        LOG THE STUFF
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