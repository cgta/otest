resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.1")

addSbtPlugin("org.scala-lang.modules.scalajs" % "scalajs-sbt-plugin" % "0.5.0")

addSbtPlugin("biz.cgta" % "sbt-x-sjs-plugin" % "0.1.0")

//CHANGE VERSION IN ExamplesBuild as well!
addSbtPlugin("biz.cgta" % "otest-sbt-plugin" % "0.1.7-SNAPSHOT")