# otest - Unit Testing for Scala and ScalaJs

It's suites are very similar to FunSuites from ScalaTest and its assertions simply throw `cgta.otest.AssertionFailure` when they fail.

It was inspired by [utest](https://github.com/lihaoyi/utest), but designed to fit into our legacy codebase, with minimal refactoring.

## Motivation

CGTA LLC has several hundred unit tests written as FunSuites for ScalaTest,
we are in the process of porting our codebase over to cross compile in ScalaJs
and we needed something that would make porting over the unit tests as easy as
possible.

There is nothing amibitious about `otest`, it simply tries to be what we at CGTA need
for our unit testing and nothing more.

##A Sample Test

Here is an example of a very simple test suite

TestFoo.scala:
```scala
import cgta.otest.FunSuite

object TestBinaryHelp extends FunSuite {
  test("popCnt32") {
    Assert.isEquals(0, BinaryHelp.popCnt32(0))
    Assert.isEquals(1, BinaryHelp.popCnt32(1))
    Assert.isEquals(1, BinaryHelp.popCnt32(2))
    Assert.isEquals(8, BinaryHelp.popCnt32(0xFF))
    Assert.isEquals(1, BinaryHelp.popCnt32(Int.MinValue))
    Assert.isEquals(32, BinaryHelp.popCnt32(-1))
    Assert.isEquals(31, BinaryHelp.popCnt32(Int.MaxValue))
  }
}

```


A full list of available assertions can be seen [here](/otest/src/main/scala/cgta/otest/Asserts.scala).
You can see them in action in their [unit test](/examples/example-tests/src/test/scala/cgta/osampletests/TestAssertions.scala)

This is a summary:
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

*Note that the intercepts[T] are implemented with macros.*

#### Assert failures are just exceptions

An assertion works by throwing an exception of type `AssertionFailure` the test suite will catch that exception, terminate the current test and display a message describing the failure, a stack trace, and any clues provided.

#### Type-safety and `isEquals` vs `isAnyEquals`: 

Typically when comparing two things in an assert block those two things should be of the same type. By default otest enforces this with the `isEquals` / `isNotEquals` asserts. If you want to use standard java-style equality just use `isAnyEquals`. The typesafe way is most often the right way so it's the default. One special case kept popping up when porting our tests over, combinations of `Some[_]`, `Option[_]` and `None`. With the magic of implicits isEquals is able to recognize that `isEquals(Some(4), Option(4))` is a reasonable enough assertion while `isEquals(Some(4), Some("4"))` isn't. When comparing different types of sequences, we coerce them to a common type of sequence, typically just calling `.toList`. 

##Using with SBT

otest runs in sbt, it doesnt' have support for maven or any other build systems as we only use sbt in-house.

It is is released in two versions, a -jvm version for projects targeting the Jvm and an -sjs version for ScalaJs projects targeting javascript.

#### Add the sbt plugin for otest:

Sbt has to register it's test runner with sbt. to do that you will have to add an sbt plugin to your project. 

So in project/plugins.sbt add the following:

    addSbtPlugin("biz.cgta" % "otest-sbt-plugin" % "VERSION_TO_USE")

where `VERSION_TO_USE` is the version you want to use. 

*NOTE: Be sure to keep this version in sync with the versions below!*

if you are compiling for scala js you will also need to include the ScalaJs Plugin
in your plugins.sbt file, making it look something like this:

    addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % "0.5.0-RC2")
    
    addSbtPlugin("biz.cgta" % "otest-sbt-plugin" % "VERSION_TO_USE")

#### In a ScalaJvm project:

add the following to the build.sbt:

    cgta.otest.OtestPlugin.settingsJvm
    
    libraryDependencies += "biz.cgta" %% "otest-jvm" % "VERSION_TO_USE" % "test"

#### In a ScalaJs project:

add the following to the build.sbt:

    cgta.otest.OtestPlugin.settingsSjs
    
    libraryDependencies += "biz.cgta" %%% "otest-jvm" % "VERSION_TO_USE" % "test"

*NOTE: The triple '%%%' in the version string here, this is added to sbt by the scalaJs plugin. Whereas %% handles binary incompitabilites between versions of Scalac, %%% goes one step further and ensures compatibility between ScalaJs versions by adding a _ to the artifact id as well.*

*ALSO NOTE: Just as above for you the plugin you will need to include the settings for ScalaJs, making your build.sbt file look something like this:*

    scala.scalajs.sbtplugin.ScalaJSPlugin.scalaJSSettings
    
    cgta.otest.OtestPlugin.settingsSjs
    
    libraryDependencies += "biz.cgta" %%% "otest-sjs" % "VERSION_TO_USE" % "test"

### Building otest itself




### Additional Notes

#### Licensing
otest has an MIT see the [LICENSE file](/LICENSE).

#### Cross building

If you want to cross-build your source like we do for jvm+sjs projects compatibility
look at the [CGTA sbt-x-sjs-plugin](https://github.com/cgta/sbt-x-sjs-plugin project) as well as the [example
project](/examples) and this project's [build](/project)

#### Better living through SBT

I strongly suggestion making a firmwide or personal plugin to sbt as suggested in this excellent talk
[Better Living Through sbt](https://www.youtube.com/watch?v=y-_h_m4GjVo). 

#### Thanks

The ScalaJs team particularly: 
[Sébastien Doeraene](https://github.com/sjrd) & [Tobias Schlatter](https://github.com/gzm0)

Thanks to Li Haoyi, for making [utest](https://github.com/lihaoyi/utest) as well as several [other](https://github.com/lihaoyi/scala.rx) [excellent](https://github.com/lihaoyi/scalatags) ScalaJs compatible libraries


