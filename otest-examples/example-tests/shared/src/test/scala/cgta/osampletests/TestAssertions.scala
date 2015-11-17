package cgta.osampletests

import cgta.otest.Asserts
import cgta.otest.{AssertionFailure, FunSuite}


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 3:50 PM
//////////////////////////////////////////////////////////////

class SampleException extends Exception


object TestAssertions extends FunSuite {
  case class Foo()

//  testOnly("Local Assert") {
//    Assert.fail("should not be run")
//  }
//
//  testOnly("Global Asserts") {
//    Asserts.fail("should not be run")
//  }
//
//
  ignore("This test is ignored on purpose") {Assert.fail("should not be run")}

  test("assert true passes on true input") {Assert.isTrue(true)}
  test("assert false passes on false input") {Assert.isFalse(false)}
  bad("assert true fails on false input") {Assert.isTrue(false)}
  bad("assert false fails on true input") {Assert.isFalse(true)}

  test("assertEquals 1 == 1") {Assert.isEquals(1, 1)}
  test("assertEquals Some(1) == Option(1)") {Assert.isEquals(Some(1), Option(1))}
  bad("assertEquals fails on 1 == 2") {Assert.isEquals(1, 2)}
  bad("assertNotEquals fails on 1 == 1") {Assert.isNotEquals(1, 1)}
  test("assertNotEquals 1 != 2") {Assert.isNotEquals(1, 2)}

  test("assertAnyEquals 1 == 2L") {Assert.isAnyEquals(1, 1L)}
  bad("assertAnyEquals fails on 1 == 2L") {Assert.isAnyEquals(1, 2L)}
  test("assertNotAnyEquals 1 != 2L") {Assert.isNotAnyEquals(1, 2L)}
  bad("assertNotAnyEquals fails on 2 != 2L") {Assert.isNotAnyEquals(2, 2L)}

  test("assertIdentityEquals") {
    val x = Foo()
    Assert.isIdentityEquals(x, x)
  }
  bad("assertIdentityEquals fails") {
    val x = Foo()
    val y = Foo()
    Assert.isIdentityEquals(x, y)
  }
  test("assertNotIdentityEquals") {
    val x = Foo()
    val y = Foo()
    Assert.isNotIdentityEquals(x, y)
  }
  bad("assertNotIdentityEquals fails") {
    val x = Foo()
    Assert.isNotIdentityEquals(x, x)
  }

  test("ok 1 < 2") {Assert.isLt(1, 2)}
  test("ok 1 <= 2") {Assert.isLte(1, 2)}
  test("ok 2 <= 2") {Assert.isLte(1, 1)}
  test("ok 2 > 1") {Assert.isGt(2, 1)}
  test("ok 2 >= 1") {Assert.isGte(2, 1)}
  test("ok 2 >= 2") {Assert.isGte(2, 2)}

  bad("bad 2 < 1") {Assert.isLt(2, 1)}
  bad("bad 2 <= 1") {Assert.isLte(2, 1)}
  bad("bad 1 > 2") {Assert.isGt(1, 2)}
  bad("bad 1 >= 2") {Assert.isGte(1, 2)}

  test("intercept exception") {
    Assert.intercepts[SampleException] {throw new SampleException}
    Assert.intercepts[Exception] {throw new SampleException}
    Assert.intercepts[Throwable] {throw new SampleException}
  }
  bad("intercept exception wrong kind of exception") {
    Assert.intercepts[RuntimeException] {throw new SampleException}
  }
  test("intercept exception with clues") {
    Assert.interceptsWithClues[SampleException](1) {throw new SampleException}
    Assert.interceptsWithClues[Exception](1, 2) {throw new SampleException}
    Assert.interceptsWithClues[Throwable](1, 2, "foo") {throw new SampleException}
  }
  bad("intercept exception with clues wrong kind of exception") {
    Assert.interceptsWithClues[RuntimeException](1, 2, "foo") {throw new SampleException}
  }
  test("intercept returns what was thrown") {
    val x = new SampleException
    val y = Assert.intercepts[SampleException]{throw x}
    Assert.isEquals(x,y)
  }
  test("intercept failure reasons") {
    def exTest(msg: String)(f: => Any) {
      try {
        f
        Assert.fail("Expected an AssertionFailure")
      } catch {
        case e: AssertionFailure =>
          Assert.isEquals(
            msg,
            e.getMessage)
        case e: Throwable =>
          Assert.fail("Expected an AssertionFailure")
      }
    }
    exTest(
      "Expected to intercept [RuntimeException] but nothing was thrown. Clues []") {
      Assert.intercepts[RuntimeException]{}
    }
    exTest(
      "Expected to intercept [RuntimeException] but caught [class cgta.osampletests.SampleException]. Clues []") {
      Assert.intercepts[RuntimeException](throw new SampleException)
    }
    exTest(
      "Expected to intercept [RuntimeException] but nothing was thrown. Clues [1,2]") {
      Assert.interceptsWithClues[RuntimeException](1,2){}
    }
    exTest(
      "Expected to intercept [RuntimeException] but caught [class cgta.osampletests.SampleException]. Clues [1,2]") {
      Assert.interceptsWithClues[RuntimeException](1,2)(throw new SampleException)
    }

  }
}