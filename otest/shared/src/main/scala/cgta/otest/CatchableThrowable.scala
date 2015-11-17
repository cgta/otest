package cgta.otest

import scala.util.control.ControlThrowable


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/26/14 10:01 AM
//////////////////////////////////////////////////////////////


object CatchableThrowable {
  /**
   * Returns true if the provided `Throwable` is to be considered non-fatal, or false if it is to be considered fatal
   */
  def apply(t: Throwable): Boolean = t match {
    case _: StackOverflowError => true // StackOverflowError ok even though it is a VirtualMachineError
    // VirtualMachineError includes OutOfMemoryError and other fatal errors
    case _: VirtualMachineError | _: ThreadDeath | _: InterruptedException | _: LinkageError | _: ControlThrowable => false
    case _ => true
  }
  //Taken from org.scalatest.Suite
  //  def anExceptionThatShouldCauseAnAbort(throwable: Throwable): Boolean =
  //    throwable match {
  //      case _: AnnotationFormatError |
  ///*
  //           _: org.scalatest.TestRegistrationClosedException |
  //           _: org.scalatest.DuplicateTestNameException |
  //           _: org.scalatest.NotAllowedException |
  //*/
  //           _: CoderMalfunctionError |
  //           _: FactoryConfigurationError |
  //           _: LinkageError |
  //           _: ThreadDeath |
  //           _: TransformerFactoryConfigurationError |
  //           _: VirtualMachineError => true
  //      // Don't use AWTError directly because it doesn't exist on Android, and a user
  //      // got ScalaTest to compile under Android.
  //      case e if e.getClass.getName == "java.awt.AWTError" => true
  //      case _ => false
  //    }
}