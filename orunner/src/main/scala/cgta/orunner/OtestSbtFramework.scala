package cgta.orunner

import sbt.testing.SubclassFingerprint


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 3:55 PM
//////////////////////////////////////////////////////////////

object OtestSbtFramework {
  val funSuiteName = "cgta.otest.FunSuite"
}

class OtestSbtFramework extends sbt.testing.Framework {

  def name(): String = "otest"

  def fingerprints(): Array[sbt.testing.Fingerprint] = Array(
    new SubclassFingerprint {
      def superclassName = OtestSbtFramework.funSuiteName
      def isModule = true
      def requireNoArgConstructor = false
    },
    new SubclassFingerprint {
      def superclassName = OtestSbtFramework.funSuiteName
      def isModule = false
      def requireNoArgConstructor = true
    }
  )

  def runner(args: Array[String],
    remoteArgs: Array[String],
    testClassLoader: ClassLoader) = {
    new OtestSbtJvmRunner(args, remoteArgs, testClassLoader)
  }
}