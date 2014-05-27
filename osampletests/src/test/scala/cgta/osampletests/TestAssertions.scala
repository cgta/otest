package cgta.osampletests

import cgta.otest.FunSuite


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 3:50 PM
//////////////////////////////////////////////////////////////

class SampleException extends Exception

object TestAssertions extends FunSuite {
  println("Running a test!")
  ignore("This test is ignored on purpose") {
    Assert.fail("should not be run")
  }
  test("assert passes on true input") {
    Assert.isTrue(true)
  }
  bad("assert fails on false input") {
    Assert.isTrue(false)
  }
  test("assertEquals 1 == 1") {
    Assert.isEquals(1, 1)
  }
  bad("assertEquals fails on 1 == 2") {
    Assert.isEquals(1, 2)
  }
  bad("assertNotEquals fails on 1 == 1") {
    Assert.isNotEquals(1, 1)
  }
  test("assertNotEquals 1 != 2") {
    Assert.isNotEquals(1, 2)
  }
  test("assertAnyEquals 1 == 2L") {
    Assert.isAnyEquals(1, 1L)
  }
  bad("assertAnyEquals fails on 1 == 2L") {
    Assert.isAnyEquals(1, 2L)
  }
  test("assertIdentityEquals") {
    val x = "Hello"
    Assert.isIdentityEquals(x, x)
  }
  bad("assertIdentityEquals fails") {
    val x = "Hello"
    val y = new String("Hello")
    Assert.isIdentityEquals(x, y)
  }
  test("assertNotIdentityEquals") {
    val x = "Hello"
    val y = new String("Hello")
    Assert.isNotIdentityEquals(x, y)
  }
  bad("assertNotIdentityEquals fails") {
    val x = "Hello"
    Assert.isNotIdentityEquals(x, x)
  }

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

}