package cgta.otest
package runner

import sbt.testing.{SubclassFingerprint, TaskDef, Task, Logger, EventHandler}
import cgta.otest.runner.TestResults.{FailedWithEitherTrace, Passed, FailedBad, Ignored}
import scala.scalajs.tools.env.{JSEnv, JSConsole}
import scala.scalajs.sbtplugin.testing.SbtTestLoggerAccWrapper
import scala.scalajs.tools.classpath.CompleteClasspath
import scala.scalajs.tools.io.MemVirtualJSFile
import scala.util.parsing.json.JSON
import scala.collection.mutable.ArrayBuffer
import scala.scalajs.tools.sourcemap.SourceMapper

//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 12:19 PM
//////////////////////////////////////////////////////////////


class OtestJsConsole(
  tracker: TestResultTracker,
  eventHandler: EventHandler,
  loggers: Array[Logger],
  completeClasspath: CompleteClasspath)(
  implicit taskDef: TaskDef) extends JSConsole {

  private lazy val sourceMapper: Option[SourceMapper] = None
  private val st = tracker.newSuiteTracker(taskDef, eventHandler)

  override def log(msg: Any): Unit = {
    import OtestConsoleProtocol.Types
    import OtestConsoleProtocol.initString

    val msgStr = "" + msg
    if (msgStr.startsWith(initString)) {
      //This is a command and control message from the underlying implementation
      val json = msgStr.drop(initString.length)
      JSON.parseFull(json) match {
        case Some(obj) =>
          val map = obj.asInstanceOf[Map[String, Any]]
          def name = map("n").asInstanceOf[String]
          def durMs = map("d").asInstanceOf[Double].toLong
          def trace = {
            val buf = new ArrayBuffer[Either[String, StackTraceElement]]()
            def e = map("e").asInstanceOf[List[Any]]
            e.foreach {
              case msg: String =>
                buf += Left(msg)
              case ste0: Map[_, _] =>
                val map = ste0.asInstanceOf[Map[String, Any]]
                def className = map("cn").asInstanceOf[String]
                def methodName = map("mn").asInstanceOf[String]
                def fileName = map("fn").asInstanceOf[String]
                def lineNum = map("l").asInstanceOf[Double].toInt
                def colNum = map("c").asInstanceOf[Double].toInt
                val ste = new StackTraceElement(className, methodName, fileName, lineNum)
                buf += Right(sourceMapper.map(_.map(ste, colNum)).getOrElse(ste))
            }
            buf.toList
          }
          map("t") match {
            case Types.trackerSuiteCompleted => tracker.Suites.completed += 1
            case Types.trackerSuiteAborted => tracker.Suites.aborted += 1
            case Types.stLogResults => st.logResults(name, loggers)
            case Types.stIgnored => st.addResult(Ignored(name))
            case Types.stPassed => st.addResult(Passed(name, durMs))
            case Types.stFailedBad => st.addResult(FailedBad(name, durMs))
            case Types.stFailedAssert | Types.stFailedException =>
              st.addResult(FailedWithEitherTrace(name, trace, durMs, failed = true))
            case Types.stFailedFatal =>
              st.addResult(FailedWithEitherTrace(name, trace, durMs, aborted = true))
            case Types.stError => st.addResult(FailedBad(name, durMs))
          }
        case None =>
      }
    } else {
      println(msgStr)
    }
  }
}

class OtestTaskSjs(
  val taskDef: TaskDef,
  tracker: TestResultTracker,
  completeClasspath: CompleteClasspath,
  env: JSEnv) extends sbt.testing.Task {

  override def tags(): Array[String] = Array()

  override def execute(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
    implicit val td = taskDef
    val testKey = taskDef.fullyQualifiedName

    tracker.begin()

    taskDef.fingerprint() match {
      case fingerprint: SubclassFingerprint if fingerprint.superclassName() == FrameworkHelp.funSuiteName =>
        if (fingerprint.isModule) {
          runSuite()
        } else {
          sys.error("FunSuite only works on objects, classes don't work.")
        }
      case _ =>
    }

    def runSuite() {
      val code = new MemVirtualJSFile("Generated Launcher for OtestSjs Suite Execution").
        withContent(s"""OtestTaskRunnerSjs().runSuite($testKey)""")
      val logger = new SbtTestLoggerAccWrapper(loggers)
      val testConsole = new OtestJsConsole(tracker, eventHandler, loggers, completeClasspath)
      env.runJS(completeClasspath, code, logger, testConsole)
    }

    Array()
  }


  //  def executeb(eventHandler: EventHandler, loggers: Array[Logger]): Array[Task] = {
  //    tracker.begin()
  //    val name = taskDef.fullyQualifiedName()
  //    taskDef.fingerprint() match {
  //      case fingerprint: SubclassFingerprint if fingerprint.superclassName() == FrameworkHelp.funSuiteName =>
  //        if (fingerprint.isModule) {
  //          val cls = Class.forName(name + "$")
  //          runSuite(eventHandler, cls.getField("MODULE$").get(cls).asInstanceOf[FunSuite], loggers)(taskDef)
  //        } else {
  //          sys.error("FunSuite only works on objects, classes don't work.")
  //        }
  //      case _ =>
  //    }
  //    Array()
  //  }
  //
  //  def runSuite(eventHandler: EventHandler, s: FunSuite, loggers: Array[Logger])(implicit taskDef: TaskDef) {
  //    val st = tracker.newSuiteTracker(taskDef, eventHandler)
  //    try {
  //      for (test <- s.SuiteImpl.tests) {
  //        runTest(test, st)
  //      }
  //      tracker.Suites.completed += 1
  //    } finally {
  //      st.logResults(s.SuiteImpl.simpleName, loggers)
  //    }
  //  }
  //
  //  def runTest(test: TestWrapper, st: TestResultTracker#SuiteTracker)(implicit taskDef: TaskDef) = {
  //    val startUtcMs = System.currentTimeMillis()
  //    def durMs = System.currentTimeMillis() - startUtcMs
  //    if (test.ignored) {
  //      st.addResult(Ignored(test.name))
  //    } else {
  //      try {
  //        test.body()
  //        st.addResult(if (test.bad) FailedBad(test.name, durMs) else Passed(test.name, durMs))
  //      } catch {
  //        case e: AssertionFailure =>
  //          st.addResult(if (test.bad) Passed(test.name, durMs) else FailedAssertion(test.name, e, durMs))
  //        case e if CatchableThrowable(e) =>
  //          st.addResult(FailedUnexpectedException(test.name, e, durMs))
  //        case e: Throwable =>
  //          st.addResult(FailedFatalException(test.name, e, durMs))
  //          tracker.Suites.aborted += 1
  //          throw e
  //      }
  //    }
  //  }

}