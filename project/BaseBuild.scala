import org.scalajs.sbtplugin.cross.CrossProject
import sbt.Keys._
import sbt._
import org.scalajs.sbtplugin.ScalaJSPlugin
//import org.scalajs.sbtplugin.ScalaJSPlugin.ScalaJSKeys


object BaseBuild extends Build {
  //  sys.props("scalac.patmat.analysisBudget") = "512"
  //org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger].setLevel(ch.qos.logback.classic.Level.INFO)


  lazy val basicSettings =scalacSettings ++     promptSettings


  lazy val promptSettings = Seq[Setting[_]](
    shellPrompt <<= (thisProjectRef, version) { (id, v) => _ => "ultimate:%s:%s> ".format(id.project, v) }
  )

  lazy val scalacSettings = Seq[Setting[_]](
    scalaVersion := "2.11.7",
    scalacOptions += "-deprecation",
    scalacOptions += "-unchecked",
    scalacOptions += "-feature",
    scalacOptions += "-language:implicitConversions",
    scalacOptions += "-language:higherKinds",
    scalacOptions += "-language:existentials",
    scalacOptions += "-language:postfixOps",
    scalacOptions += "-Xfatal-warnings"
  )


  def xjs(name: String, p: Project): Project = p
    .settings(basicSettings: _*)
    .enablePlugins(ScalaJSPlugin)

  def xjvm(name: String, p: Project): Project = p
    .settings(basicSettings: _*)

  def xp(name: String, p: CrossProject): CrossProject = p
    .settings(basicSettings: _*)
    .jsConfigure(xjs(name, _))
    .jvmConfigure(xjvm(name, _))

}
