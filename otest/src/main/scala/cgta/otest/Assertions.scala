package cgta.otest


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 2:28 PM
//////////////////////////////////////////////////////////////

import scala.language.experimental.macros

object Assertions extends AssertionsMixin

trait AssertionsMixin {
  def assertTrue(actual: Boolean, clue: String = null) {
    if (!actual) throw AssertionFailure.basic("true", actual, "but got", clue)
  }

  def assertFalse(actual: Boolean, clue: String = null) {
    if (actual) throw AssertionFailure.basic("false", actual, "but got", clue)
  }

  def assertEquals[A, B](expected: A, actual: B, clue: Any = null)(implicit ev: A =:= B) {
    if (expected == actual) {
    } else {
      throw AssertionFailure.basic(expected, actual, "to be equal to", clue)
    }
  }

  def assertNotEquals[A, B](expected: A, actual: B, clue: Any = null)(implicit ev: A =:= B) {
    if (expected != actual) {
    } else {
      throw AssertionFailure.basic(expected, actual, "not to be equal to", clue)
    }
  }

  def assertAnyEquals(expected: Any, actual: Any, clue: Any = null) {
    if (expected == actual) {
    } else {
      throw AssertionFailure.basic(expected, actual, "to be equal to", clue)
    }
  }
  def assertNotAnyEquals(expected: Any, actual: Any, clue: Any = null) {
    if (expected != actual) {
    } else {
      throw AssertionFailure.basic(expected, actual, "not be equal to", clue)
    }
  }

  def assertIdentityEquals(expected: AnyRef, actual: AnyRef, clue: Any = null) {
    if (expected eq actual) {
    } else {
      throw AssertionFailure.basic(expected, actual, "to be identity eq to", clue)
    }
  }
  def assertNotIdentityEquals(expected: AnyRef, actual: AnyRef, clue: Any = null) {
    if (expected eq actual) {
      throw AssertionFailure.basic(expected, actual, "not to be identity eq to", clue)
    } else {
    }
  }

  def fail(msg: String = null) {
    throw AssertionFailure.fail(msg)
  }

  def intercept[T](body: Unit): Unit = macro AssertionMacros.intercept[T]
  def interceptWithClue[T](clue: Any)(body: Unit) = macro AssertionMacros.interceptWithClue[T]
}


object AssertionFailure {
  def basic(expected: Any, actual: Any, join: String, clue: Any): AssertionFailure = {
    new AssertionFailure(s"Expected [$expected] ${join} [$actual]${if (clue != null) s" clue: $clue" else ""}", null)
  }

  def fail(msg: String): AssertionFailure = {
    new AssertionFailure(s"fail() called${if (msg != null) s": $msg" else ""}", null)
  }

  def intercept(expectedTypeName: String, unexpected: Option[Throwable], clue: String = null): AssertionFailure = {
    val clueStr = if (clue != null) s" clue: $clue" else ""
    unexpected match {
      case None =>
        new AssertionFailure(s"Expected to intercept [$expectedTypeName] but nothing was thrown. $clueStr", null)
      case Some(unexpected) =>
        val unexpectedTypeName = unexpected.getClass.toString
        new AssertionFailure(
          s"Expected to intercept [$expectedTypeName] but caught [$unexpectedTypeName]. $clueStr", unexpected)
    }
  }
}

class AssertionFailure(reason: String, cause: Throwable) extends RuntimeException(reason, cause)