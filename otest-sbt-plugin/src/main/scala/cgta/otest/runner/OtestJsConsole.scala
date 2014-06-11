package cgta.otest
package runner

import sbt.testing.{TaskDef, Logger, EventHandler}
import cgta.otest.runner.TestResults.{FailedWithEitherTrace, Passed, FailedBad, Ignored}
import scala.scalajs.tools.env.JSConsole
import scala.scalajs.tools.classpath.CompleteClasspath
import scala.scalajs.tools.sourcemap.SourceMapper
import scala.util.parsing.json.JSON
import scala.collection.mutable.ArrayBuffer


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/29/14 1:00 PM
//////////////////////////////////////////////////////////////


class OtestJsConsole(
  tracker: TestResultTracker,
  eventHandler: EventHandler,
  loggers: Array[Logger],
  completeClasspath: CompleteClasspath)(
  implicit taskDef: TaskDef) extends JSConsole {

  private lazy val sourceMapper: Option[SourceMapper] = Some(new SourceMapper(completeClasspath))
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