package cgta.otest
package runner

import sbt.testing.{Fingerprint, Selector, TaskDef, Status, OptionalThrowable}


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 10:46 AM
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