package cgta.otest

import scala.reflect.macros.Context

//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 3:06 PM
//////////////////////////////////////////////////////////////

import scala.language.experimental.macros

object AssertionMacros {
  def intercept[T: c.WeakTypeTag](c: Context)(body: c.Expr[Unit]): c.Expr[Unit] = {
    import c.universe._
    val t = implicitly[c.WeakTypeTag[T]]
    val tname = t.toString()
    val res = q"""
      var _cAugHt =false
      try{
        $body
      } catch {
        case e: $t=>_cAugHt =true
        case t: Throwable if cgta.otest.CatchableThrowable(t) =>
          throw cgta.otest.AssertionFailure.intercept($tname, Some(t))
      }
      if(!_cAugHt){
        throw cgta.otest.AssertionFailure.intercept($tname, None)
      }
      """
    c.Expr[Unit](res)
  }

  def interceptWithClue[T: c.WeakTypeTag](c: Context)(clue: c.Expr[Any])(body: c.Expr[Unit]): c.Expr[Unit] = {
    import c.universe._
    val t = implicitly[c.WeakTypeTag[T]]
    val tname = t.toString()
    val res = q"""
      var _cAugHt =false
      try{
        $body
      } catch {
        case e: $t=>_cAugHt =true
        case t: Throwable if cgta.otest.CatchableThrowable(t) =>
          throw cgta.otest.AssertionFailure.intercept($tname, Some(t), $clue)
      }
      if(!_cAugHt){
        throw cgta.otest.AssertionFailure.intercept($tname, None, $clue)
      }
      """
    c.Expr[Unit](res)
  }


}
