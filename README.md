# otest

A very simple unit testing framework that works for Scala code compiled to
java bytecode as well as javascript.

It's suites are very similar to FunSuites from ScalaTest and its assertions
are are simple functions that throw AssertExceptions on failure.

## Motivation

CGTA LLC has several hundred unit tests written as FunSuites for ScalaTest,
we are in the process of porting our codebase over to cross compile in ScalaJs
and we needed something that would make porting over the unit tests as easy as
possible.

There is nothing amibitious about `otest`, it simply tries to be what we need
for our unit testing and nothing more.

##A Sample Test




##Usage

otest runs in sbt. 

It is is released in two versions, an -sjs version a -jvm version.

in project/plugins.sbt add the following:

    addSbtPlugin("biz.cgta" % "otest-sbt-plugin" % "VERSION_TO_USE")

where `VERSION_TO_USE` is the version you want to use.

#### In a ScalaJvm project:

add the following to the build.sbt:

    cgta.otest.OtestPlugin.settingsJvm

#### In a ScalaJs project:

add the following to the build.sbt:

    cgta.otest.OtestPlugin.settingsSjs

If you want to cross-build your source like we do for jvm+sjs projects compatibility
look at the https://github.com/cgta/sbt-x-sjs-plugin project

