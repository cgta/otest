package cgta.otest


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 1:27 PM
//////////////////////////////////////////////////////////////


trait FunSuite extends AssertionsMixin {
  private def registerTest(t: RegisteredTest)

  /** Runs only once, before any of the test in suite have run
    */
  def beforeSuite(body: => Unit)

  /** Runs after all the tests in the suite have run.
    */
  def afterSuite(body: => Unit)

  /** Runs before each test in this suite
    */
  def before(body: => Unit)

  /** Runs after each test in this suite
    */
  def after(body: => Unit)

  /** Change test to ignore to prevent it from running
    */
  def ignore(name: String)(body: => Unit) {
    registerTest(RegisteredTest(name, () => body, ignored = true))
  }

  def test(name: String)(body: => Unit) {
    registerTest(RegisteredTest(name, () => body))
  }
}

case class RegisteredTest(name: String, body: () => Unit, ignored: Boolean = false)

trait AssertionsMixin {
  def assertTrue(p: Boolean, clue: String = null) {
    if (!p) throw AssertionFailure.basic("true", "" + p, clue)
  }

  def assertFalse(p: Boolean, clue: String) {
    if (p) throw AssertionFailure.basic("false", "" + p, clue)
  }

  def assertEquals[A, B](expected: A, actual: B, clue: Any = null)(implicit ev: A =:= B) {
    if (expected != actual) {
      throw AssertionFailure.basic("" + expected, "" + actual, if (clue == null) null else "" + clue)
    }
  }

  def assertAnyEquals(expected: Any, actual: Any, clue: Any = null) {
    if (expected != actual) {
      throw AssertionFailure.basic("" + expected, "" + actual, if (clue == null) null else "" + clue)
    }
  }

  def fail(msg: String = null) {
    throw new AssertionFailure(s"fail() called${if (msg != null) s": $msg" else ""}")
  }

  def intercept[T <: Throwable](body: => Any) = macro AssertionMacros.intercept[T]
//  def intercept[T <: Throwable](clue : Any)(body: => Any) = macro AssertionMacros.intercept[T]
}

object AssertionFailure {
  def basic(expected: String, actual: String, clue: String): AssertionFailure = {
    new AssertionFailure(s"Expected [$expected] but got [$actual]${if (clue != null) s" clue: $clue" else ""}")
  }
}

class AssertionFailure(reason: String) extends RuntimeException(reason)