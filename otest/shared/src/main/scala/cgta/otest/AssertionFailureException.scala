package cgta.otest


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/27/14 1:27 PM
//////////////////////////////////////////////////////////////


object AssertionFailureException {
  def basic(expected: Any, actual: Any, join: String, clues: Any*) = {
    new SimpleAssertionException(expected = expected, actual = actual, op = join, clues = clues)
  }

  def fail(msg: String) = {
    new FailAssertionException(msg)
  }

  def intercept(expectedTypeName: String, unexpected: Option[Throwable], clues: Any*): AssertionFailureException = {
    val cluesStr = clues.mkString(",")
    unexpected match {
      case None =>
        new InterceptAssertionException(
          s"Expected to intercept [$expectedTypeName] but nothing was thrown. Clues [$cluesStr]", null)
      case Some(unexpected) =>
        val unexpectedTypeName = unexpected.getClass.toString
        new InterceptAssertionException(
          s"Expected to intercept [$expectedTypeName] but caught [$unexpectedTypeName]. Clues [$cluesStr]", unexpected)
    }
  }

//  def apply(reason: String): AssertionFailure = new AssertionFailure(reason, null)
//  def apply(reason: String, cause: Throwable): AssertionFailure = new AssertionFailure(reason, cause)
}


trait AssertionFailureException extends RuntimeException

class SimpleAssertionException(
  val expected : Any,
  val actual: Any,
  val op : String,
  val clues : Seq[Any]) extends RuntimeException with AssertionFailureException {

//  val cluesStr = clues.mkString(",")
//  new AssertionFailure(s"Expected [$expected] $join [$actual] Clues [$cluesStr]", null)
}


class FailAssertionException(msg : String) extends RuntimeException with AssertionFailureException {
//    val msgStr = if (msg != null) s": $msg" else ""
//    new AssertionFailure(s"fail() called$msgStr", null)
}

class InterceptAssertionException(
  msg : String,
  cause: Throwable) extends RuntimeException(msg, cause) with AssertionFailureException {

}