package cgta.otest


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 1:43 PM
//////////////////////////////////////////////////////////////


import scala.language.experimental.macros
import scala.reflect.macros.Context

object AssertionMacros {
  def intercept[T <: Throwable : c.WeakTypeTag](c: Context)(body: => Unit): c.Expr[Any] = {

  }
}
