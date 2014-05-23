package cgta.otest


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 3:06 PM
//////////////////////////////////////////////////////////////
import scala.reflect.macros.Context
import scala.language.experimental.macros

object AssertionMacros {
  def intercept[T : c.WeakTypeTag](c: Context)(body: c.Expr[Unit]): c.Expr[Unit] = {
    import c.universe._
    val t = implicitly[c.WeakTypeTag[T]]
    val tname = t.toString
    val res = q"""
      var _cAught =false
      try{
        body
      } catch {
        case e: $t=>_cAught =true
        case t: Throwable => throw cgta.otest.AssertionFailure.intercept($tname, Some(t))
      }
      if(!_cAught_){
        throw cgta.otest.AssertionFailure.intercept($tname)
      }
      """
    c.Expr[Unit](null)
  }
}