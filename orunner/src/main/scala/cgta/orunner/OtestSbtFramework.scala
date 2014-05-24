package cgta.orunner

import sbt.testing.SubclassFingerprint


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 3:55 PM
//////////////////////////////////////////////////////////////

class OtestSbtFramework extends sbt.testing.Framework {

  println("CALLED THE FRAMEWORK")

  def name(): String = "otest"

  def fingerprints(): Array[sbt.testing.Fingerprint] = Array(
    new SubclassFingerprint {
      def superclassName = "cgta.otest.FunSuite"

      def isModule = true

      def requireNoArgConstructor = false
    },
    new SubclassFingerprint {
      def superclassName = "cgta.otest.FunSuite"

      def isModule = false

      def requireNoArgConstructor = true
    }

    //    ,
    //    new SubclassFingerprint {
    //      def superclassName = "otest.FunSuite"
    //      def isModule = false
    //      def requireNoArgConstructor = true
    //    }
  )

  def runner(args: Array[String],
             remoteArgs: Array[String],
             testClassLoader: ClassLoader) = {
    new OtestSbtJvmRunner(args, remoteArgs)
  }
}