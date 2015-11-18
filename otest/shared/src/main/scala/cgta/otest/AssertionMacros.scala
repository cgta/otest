package cgta.otest

import scala.reflect.macros.blackbox.Context

//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 3:06 PM
//////////////////////////////////////////////////////////////

import scala.language.experimental.macros

object AssertionMacros {
  def intercepts[T: c.WeakTypeTag](c: Context)(body: c.Expr[Unit]): c.Expr[T] = {
    import c.universe._
    val t = implicitly[c.WeakTypeTag[T]]
    val tname = t.tpe.toString()
    val res = q"""
      var _cAugHt : scala.Option[$t] = scala.None
      try {
        try{
          $body
        } catch {
          case e: $t=> _cAugHt = scala.Some(e)
        }
      } catch {
        case t: Throwable if cgta.otest.CatchableThrowable(t) =>
          throw cgta.otest.AssertionFailureException.intercept($tname, Some(t))
      }
      _cAugHt match {
        case scala.Some(e) => e
        case scala.None => throw cgta.otest.AssertionFailureException.intercept($tname, None)
      }
      """
    c.Expr[T](res)
  }

  def interceptsWithClues[T: c.WeakTypeTag](c: Context)(clues: c.Expr[Any]*)(body: c.Expr[Unit]): c.Expr[T] = {
    import c.universe._
    val t = implicitly[c.WeakTypeTag[T]]
    val tname = t.tpe.toString()
    val res = q"""
      var _cAugHt : scala.Option[$t] = scala.None
      try {
        try {
          $body
        } catch {
          case e: $t=> _cAugHt = scala.Some(e)
        }
      } catch {
        case t: Throwable if cgta.otest.CatchableThrowable(t) =>
          throw cgta.otest.AssertionFailureException.intercept($tname, Some(t), ..$clues)
      }
      _cAugHt match {
        case scala.Some(e) => e
        case scala.None => throw cgta.otest.AssertionFailureException.intercept($tname, None, ..$clues)
      }
      """
    c.Expr[T](res)
  }


}
