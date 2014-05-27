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
    fail("should not be run")
  }
  test("assertTrue passed on true input") {
    assertTrue(true)
  }
  bad("assertTrue fails on false input") {
    assertTrue(false)
  }
  test("assertFalse passed on false input") {
    assertFalse(false)
  }
  bad("assertFalse fails on true input") {
    assertFalse(true)
  }
  test("assertEquals 1 == 2") {
    assertEquals(1,1)
  }
  bad("assertEquals fails on 1 == 2") {
    assertEquals(1,2)
  }
  test("assertAnyEquals 1 == 2L") {
    assertAnyEquals(1,1L)
  }
  bad("assertAnyEquals fails on 1 == 2L") {
    assertAnyEquals(1,2L)
  }
  test("intercept exception") {
    intercept[SampleException] {throw new SampleException}
    intercept[Exception] {throw new SampleException}
    intercept[Exception] {throw new SampleException}
  }
  bad("intercept exception") {
    intercept[RuntimeException] {throw new SampleException}
  }
}