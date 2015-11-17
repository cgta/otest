package cgta.otest


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/27/14 1:27 PM
//////////////////////////////////////////////////////////////


object AssertionFailure {
  def basic(expected: Any, actual: Any, join: String, clues: Any*): AssertionFailure = {
    val cluesStr = clues.mkString(",")
    new AssertionFailure(s"Expected [$expected] $join [$actual] Clues [$cluesStr]", null)
  }

  def fail(msg: String): AssertionFailure = {
    val msgStr = if (msg != null) s": $msg" else ""
    new AssertionFailure(s"fail() called$msgStr", null)
  }

  def intercept(expectedTypeName: String, unexpected: Option[Throwable], clues: Any*): AssertionFailure = {
    val cluesStr = clues.mkString(",")
    unexpected match {
      case None =>
        new AssertionFailure(
          s"Expected to intercept [$expectedTypeName] but nothing was thrown. Clues [$cluesStr]", null)
      case Some(unexpected) =>
        val unexpectedTypeName = unexpected.getClass.toString
        new AssertionFailure(
          s"Expected to intercept [$expectedTypeName] but caught [$unexpectedTypeName]. Clues [$cluesStr]", unexpected)
    }
  }

  def apply(reason: String): AssertionFailure = new AssertionFailure(reason, null)
  def apply(reason: String, cause: Throwable): AssertionFailure = new AssertionFailure(reason, cause)
}

class AssertionFailure(reason: String, cause: Throwable) extends RuntimeException(reason, cause)