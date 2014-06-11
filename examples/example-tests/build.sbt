//The dependency on otest-jvm is added here so that intellij will add dependencies into the base examples project,
//it doesnt end up being used, because all actions in sbt are undertaken in the example-tests-jvm or example-tests-sjs
//subprojects
libraryDependencies += "biz.cgta" %% "otest-jvm" % "0.1.5-SNAPSHOT" % "test"
