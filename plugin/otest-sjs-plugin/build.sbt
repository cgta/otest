import cgta.osbt.OsCgtaSbtPlugin
import OsCgtaSbtPlugin._

OsCgtaSbtPlugin.basicSettings

libraryDependencies ++= Libs.sbtTestInterface

libraryDependencies += Libs.scalaReflect % scalaVersion.value

SbtPlugins.scalaJs

sbtPlugin := true

Bintray.repo("sbt-plugins")