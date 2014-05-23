package cgta.osampletests

import cgta.otest.FunSuite


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 3:50 PM
//////////////////////////////////////////////////////////////

object TestAssertions extends FunSuite {
  test("assertTrue passed on true input") {
    assertTrue(true)
  }
  bad("assertTrue fails on false input") {
    assertTrue(false)
  }
}