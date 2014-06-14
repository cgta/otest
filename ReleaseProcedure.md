
#Run Tests
bin/test

#Bump version to next RELEASE version in:
  README.md

#Push version change upto repo & then release
git commit -a -m "Preparing for release"
git push origin HEAD

sbt release

#Bump version to next SNAPSHOT version:
  examples/project/plugins.sbt
  examples/project/ExampleBuild.scala
 
#Run Test
bin/test

