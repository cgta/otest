package cgta.otest


//////////////////////////////////////////////////////////////
// Copyright (c) 2014 Ben Jackman, Jeff Gomberg
// All Rights Reserved
// please contact ben@jackman.biz or jeff@cgtanalytics.com
// for licensing inquiries
// Created by bjackman @ 5/23/14 1:27 PM
//////////////////////////////////////////////////////////////

import scala.language.experimental.macros
import scala.collection.mutable.ArrayBuffer

case class TestWrapper(name: String, body: () => Unit, ignored: Boolean = false, bad: Boolean = false)

trait FunSuite extends FunSuitePlatformImpl {
  object SuiteImpl {

    def simpleName = FunSuite.this.getClass.toString.split("\\.").last

    private val registered = new ArrayBuffer[TestWrapper]()

    def tests: List[TestWrapper] = registered.toList

    private[FunSuite] def registerTest(t: TestWrapper) = {
      registered += t
    }
  }

  object Assert extends AssertionsMixin


  //  /** Runs only once, before any of the test in suite have run
  //    */
  //  def beforeSuite(body: => Unit)
  //
  //  /** Runs after all the tests in the suite have run.
  //    */
  //  def afterSuite(body: => Unit)
  //
  //  /** Runs before each test in this suite
  //    */
  //  def before(body: => Unit)
  //
  //  /** Runs after each test in this suite
  //    */
  //  def after(body: => Unit)

  //  def cleanup(body: => Unit)

  def test(name: String)(body: => Unit) {
    SuiteImpl.registerTest(TestWrapper(name, () => body))
  }

  /** This test is expected to fail, it's a failure if this test
    * doesn't fail
    */
  def bad(name: String)(body: => Unit) {
    SuiteImpl.registerTest(TestWrapper(name, () => body, bad = true))
  }

  /** Change test to ignoretest to prevent it from running
    */
  def ignoretest(name: String)(body: => Unit) {
    SuiteImpl.registerTest(TestWrapper(name, () => body, ignored = true))
  }

  /** Change test to ignore to prevent it from running
    */
  def ignore(name: String)(body: => Unit) {
    ignoretest(name)(body)
  }

  /** Change test to ignorebad to prevent it from running
    */
  def ignorebad(name: String)(body: => Unit) {
    SuiteImpl.registerTest(TestWrapper(name, () => body, ignored = true, bad = true))
  }

}



