package cgta.otest
package runner


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/29/14 10:51 AM
//////////////////////////////////////////////////////////////


object OtestConsoleProtocol {
  def initString = "xXxNeckJamBanxXx/"

  object Types {
    val trackerSuiteCompleted = "tracker-suite-completed"
    val trackerSuiteAborted   = "tracker-suite-aborted"
    val stLogResults          = "st-log-results"
    val stIgnored             = "st-ignored"
    val stPassed              = "st-passed"
    val stFailedBad           = "st-failed-bad"
    val stFailedAssert        = "st-failed-assert"
    val stFailedException     = "st-failed-exception"
    val stFailedFatal         = "st-failed-fatal"
    val stError               = "st-error"
  }


}