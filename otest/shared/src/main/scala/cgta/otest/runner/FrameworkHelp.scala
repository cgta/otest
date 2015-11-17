package cgta.otest
package runner

import sbt.testing.SubclassFingerprint


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 3:55 PM
//////////////////////////////////////////////////////////////

object FrameworkHelp {
  val funSuiteName = "cgta.otest.FunSuite"
  def fingerprints(): Array[sbt.testing.Fingerprint] = Array(
    new SubclassFingerprint {
      def superclassName = funSuiteName
      def isModule = true
      def requireNoArgConstructor = false
    },
    new SubclassFingerprint {
      def superclassName = funSuiteName
      def isModule = false
      def requireNoArgConstructor = true
    }
  )
}


