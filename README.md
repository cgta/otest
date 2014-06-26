# otest 0.1.9 - Unit Testing for Scala and ScalaJs

Its suites are very similar to FunSuites from ScalaTest and its assertions simply throw `cgta.otest.AssertionFailure` when they fail.

It was inspired by [utest](https://github.com/lihaoyi/utest), but designed to fit into our legacy codebase, with minimal refactoring.

It currently is built targeting `Scala 2.10` & `Scala 2.11` as well as  `ScalaJs 0.5.0`

Motivation
==========

CGTA  has several hundred unit tests written as FunSuites for ScalaTest,
we are in the process of porting our codebase over to cross compile in ScalaJs
and we needed something that would make porting over the unit tests as easy as
possible.


A Sample Test
=============
Here is an example of a very simple test suite
----------------------------------------------

TestBinaryHelp.scala:
```scala
import cgta.otest.FunSuite

object TestBinaryHelp extends FunSuite {
  test("popCnt32") {
    Assert.isEquals(0, BinaryHelp.popCnt32(0))
    Assert.isEquals(1, BinaryHelp.popCnt32(1))
    Assert.isEquals(1, BinaryHelp.popCnt32(2))
    Assert.isEquals(8, BinaryHelp.popCnt32(0xFF))
    Assert.isEquals(32, BinaryHelp.popCnt32(-1))
    Assert.isEquals(1, BinaryHelp.popCnt32(Int.MinValue))
    Assert.isEquals(31, BinaryHelp.popCnt32(Int.MaxValue))
  }
  test("ZigZag64") {
    Assert.isEquals(0L, BinaryHelp.decodeZigZag64(BinaryHelp.encodeZigZag64(0)))
    Assert.isEquals(-1L, BinaryHelp.decodeZigZag64(BinaryHelp.encodeZigZag64(-1)))
    Assert.isEquals(1L, BinaryHelp.decodeZigZag64(BinaryHelp.encodeZigZag64(1)))
    Assert.isEquals(Long.MaxValue, BinaryHelp.decodeZigZag64(BinaryHelp.encodeZigZag64(Long.MaxValue)))
    Assert.isEquals(Long.MinValue, BinaryHelp.decodeZigZag64(BinaryHelp.encodeZigZag64(Long.MinValue)))
  }  
  ...
}

```

Here is a summary of the avaible assertions:
--------------------------------------------
```scala
isTrue(actual: Boolean, clues: Any*)
isEquals[A, B](expected: A, actual: B, clues: Any*)(implicit ev: CanAssertEq[A, B])
isNotEquals[A, B](expected: A, actual: B, clues: Any*)(implicit ev: CanAssertEq[A, B])
isAnyEquals(expected: Any, actual: Any, clues: Any*)
isNotAnyEquals(expected: Any, actual: Any, clues: Any*)
isIdentityEquals(expected: AnyRef, actual: AnyRef, clues: Any*)
isNotIdentityEquals(expected: AnyRef, actual: AnyRef, clues: Any*)
isLt[A](a: A, b: A, clues: Any*)(implicit ordering: Ordering[A])
isLte[A](a: A, b: A, clues: Any*)(implicit ordering: Ordering[A])
isGt[A](a: A, b: A, clues: Any*)(implicit ordering: Ordering[A])
isGte[A](a: A, b: A, clues: Any*)(implicit ordering: Ordering[A])
fail(msg: String = null)
intercepts[T](body: Unit) 
intercepts[T](clues: Any*)(body: Unit) 
```
*Note that the `intercepts[T]` are implemented with macros*

A full list of available assertions can be seen [here](/otest/src/main/scala/cgta/otest/Asserts.scala).

You can see them in action in their [unit test](/examples/example-tests/src/test/scala/cgta/osampletests/TestAssertions.scala)


Assert failures are just exceptions
-----------------------------------

An assertion works by throwing an exception of type `AssertionFailure` the test suite will catch that exception, terminate the current test and display a message describing the failure, a stack trace, and any clues provided.

Type-safety and `isEquals` vs `isAnyEquals`:
--------------------------------------------

Typically when comparing two things in an assert block those two things should be of the same type. By default otest enforces this with the `isEquals` / `isNotEquals` asserts. If you want to use standard java-style equality just use `isAnyEquals`. The typesafe way is most often the right way so it's the default. One special case kept popping up when porting our tests over, combinations of `Some[_]`, `Option[_]` and `None`. With the magic of implicits isEquals is able to recognize that `isEquals(Some(4), Option(4))` is a reasonable enough assertion while `isEquals(Some(4), Some("4"))` isn't. When comparing different types of sequences, we coerce them to a common type of sequence, typically just calling `.toList`. 

Using with SBT
==============

otest runs in sbt, it doesn't have support for maven or any other build systems as we only use sbt in-house.

It is is released in two versions, a -jvm version for projects targeting the Jvm and an -sjs version for ScalaJs projects targeting javascript.

Add the sbt plugin for otest:
-----------------------------

otest has to register it's test runner with sbt. to do that you will have to add an sbt plugin to your project.
 
Following sbt best practices, each plugin should be put in their own file.

Create a file called `project/otest.sbt` and add the following:
```scala
addSbtPlugin("biz.cgta" % "otest-sbt-plugin" % "0.1.9")
```


In a ScalaJvm project:
----------------------

add the following to the `build.sbt`:

```scala
cgta.otest.OtestPlugin.settingsJvm

libraryDependencies += "biz.cgta" %% "otest-jvm" % "0.1.9" % "test"
```

In a ScalaJs project:
---------------------

add the following to the `build.sbt`:

```scala
cgta.otest.OtestPlugin.settingsSjs

libraryDependencies += "biz.cgta" %%% "otest-jvm" % "0.1.9" % "test"
```

*NOTE: The triple '%%%' in the version string here, this is added to sbt by the scalaJs plugin. Whereas %% handles binary incompitabilites between versions of Scalac, %%% goes one step further and ensures compatibility between ScalaJs versions by adding a tag like `_sjs0.5` to the artifact id as well.*

Building otest
==============
clone the repo locally, cd into it, and run the shell script

`bin/test`

It will publishLocal SNAPSHOT versions of otest for jvm and sjs, and it will run
the example tests on each of them.


Additional Notes
================

Licensing
---------
MIT see the [LICENSE file](/LICENSE).

Cross building
--------------
If you want to cross-build your source like we do for jvm+sjs projects compatibility
look at the [CGTA sbt-x-sjs-plugin project](https://github.com/cgta/sbt-x-sjs-plugin) as well as the [example
project](/examples) and this project's [build](/project) If you decide to go down this route I'd suggest giving [Better Living Through sbt](https://www.youtube.com/watch?v=y-_h_m4GjVo) a watch on youtube, to see how to setup a [firmwide plugin project](https://github.com/Banno/banno-sbt-plugin) for sbt.

Thanks
------
The ScalaJs team [Sebastien Doeraene](https://github.com/sjrd) & [Tobias Schlatter](https://github.com/gzm0) for making all of this possible.

Thanks to [Li Haoyi](https://github.com/lihaoyi), for making [utest](https://github.com/lihaoyi/utest) as well as [several](https://github.com/lihaoyi/upickle), [other](https://github.com/lihaoyi/scala.rx), [excellent](https://github.com/lihaoyi/scalatags) ScalaJs compatible libraries.


