package cgta.orunner

import sbt.testing.{TaskDef, Task}


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 4:01 PM
//////////////////////////////////////////////////////////////

class OtestSbtJvmRunner(val args: Array[String], val remoteArgs: Array[String]) extends sbt.testing.Runner {
  def done(): String = {
    "WE ARE ALL DONE WITH THE DAMNED OTESTS"
  }
  def tasks(taskDefs: Array[TaskDef]): Array[Task] = {
    ???
  }
}

//class JvmRunner(val args: Array[String],
//                val remoteArgs: Array[String])
//  extends GenericRunner {
//
//  def doStuff(s: Seq[String], loggers: Seq[Logger], name: String) = {
//    val cls = Class.forName(name + "$")
//    val suite = cls.getField("MODULE$").get(cls).asInstanceOf[TestSuite]
//    val res = utest.runSuite(
//      suite,
//      s.toArray,
//      args,
//      s => if (s.toBoolean) success.incrementAndGet() else failure.incrementAndGet(),
//      msg => loggers.foreach(_.info(progressString + name + "" + msg)),
//      s => total.addAndGet(s.toInt)
//    )
//
//    addResult(res)
//  }
//}

