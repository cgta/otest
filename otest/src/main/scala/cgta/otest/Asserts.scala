package cgta.otest


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 2:28 PM
//////////////////////////////////////////////////////////////

import scala.language.experimental.macros

object Asserts extends AssertsMixin

trait CanAssertEqL1 {
  val singleton = CanAssertEq[Any, Any]()
  implicit def sameType[A, B](implicit ev: A =:= B): CanAssertEq[A, B] = singleton.asInstanceOf[CanAssertEq[A, B]]

}

object CanAssertEq extends CanAssertEqL1 {
  //Allowing options here both as an example of how to write special case exceptions
  //and because in my experience it is by far the most frequent case. For sequences, I suggest
  //just calling toList or toSeq on each side if possible, and if not just using .isAnyEquals
  implicit def optionEqs[A, B, AA, BB](implicit
    e1: AA <:< Option[A],
    e2: BB <:< Option[B],
    e3: CanAssertEq[A, B]): CanAssertEq[AA, BB] = singleton.asInstanceOf[CanAssertEq[AA, BB]]

}
case class CanAssertEq[A, B]()


trait AssertsMixin {
  def isTrue(actual: Boolean, clues: Any*) {
    if (!actual) throw AssertionFailure.basic("true", actual, "but got", clues: _*)
  }

  def isEquals[A, B](expected: A, actual: B, clues: Any*)(implicit ev: CanAssertEq[A, B]) {
    if (expected == actual) {
    } else {
      throw AssertionFailure.basic(expected, actual, "to be equal to", clues: _*)
    }
  }

  def isNotEquals[A, B](expected: A, actual: B, clues: Any*)(implicit ev: CanAssertEq[A, B]) {
    if (expected != actual) {
    } else {
      throw AssertionFailure.basic(expected, actual, "not to be equal to", clues: _*)
    }
  }

  def isAnyEquals(expected: Any, actual: Any, clues: Any*) {
    if (expected == actual) {
    } else {
      throw AssertionFailure.basic(expected, actual, "to be equal to", clues: _*)
    }
  }

  def isNotAnyEquals(expected: Any, actual: Any, clues: Any*) {
    if (expected != actual) {
    } else {
      throw AssertionFailure.basic(expected, actual, "not be equal to", clues: _*)
    }
  }

  def isIdentityEquals(expected: AnyRef, actual: AnyRef, clues: Any*) {
    if (expected eq actual) {
    } else {
      throw AssertionFailure.basic(expected, actual, "to be identity eq to", clues: _*)
    }
  }

  def isNotIdentityEquals(expected: AnyRef, actual: AnyRef, clues: Any*) {
    if (expected eq actual) {
      throw AssertionFailure.basic(expected, actual, "not to be identity eq to", clues: _*)
    } else {
    }
  }

  //Asserts a < b
  def isLt[A](a: A, b: A, clues: Any*)(implicit ordering: Ordering[A]) {
    if (ordering.lt(a, b)) {
    } else {
      throw AssertionFailure.basic(a, b, "to be <", clues: _*)
    }
  }

  //Asserts a <= b
  def isLte[A](a: A, b: A, clues: Any*)(implicit ordering: Ordering[A]) {
    if (ordering.lteq(a, b)) {
    } else {
      throw AssertionFailure.basic(a, b, "to be <=", clues: _*)
    }
  }

  //Asserts a > b
  def isGt[A](a: A, b: A, clues: Any*)(implicit ordering: Ordering[A]) {
    if (ordering.gt(a, b)) {
    } else {
      throw AssertionFailure.basic(a, b, "to be >", clues: _*)
    }
  }

  //Asserts a >= b
  def isGte[A](a: A, b: A, clues: Any*)(implicit ordering: Ordering[A]) {
    if (ordering.gteq(a, b)) {
    } else {
      throw AssertionFailure.basic(a, b, "to be >=", clues: _*)
    }
  }

  def fail(msg: String = null): Nothing = {
    throw AssertionFailure.fail(msg)
  }

  def intercepts[T](body: Unit): T = macro AssertionMacros.intercepts[T]

  def interceptsWithClues[T](clues: Any*)(body: Unit): T = macro AssertionMacros.interceptsWithClues[T]
}


