package cgta.otest
package runner

import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/28/14 6:51 PM
//////////////////////////////////////////////////////////////


@JSExport
object OtestTaskRunnerSjs {
  @JSExport
  def runSuite(suite: js.Function0[FunSuite]) {
    println("SUITESSS"+ suite)
    println("XXX"+ js.Dynamic.global.cgta )
    println("XXX"+ js.Dynamic.global.TestAssertions)
    println("XXX"+ js.Dynamic.global.TestAssertions$)
    println("XXX"+ js.Dynamic.global.TestAssertions$)
  }
}