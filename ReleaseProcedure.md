


#Make sure on master
gru

#Commit and push up changes

#Run Tests
bin/test

#Bump version to next RELEASE version in:
  README.md

#Push version change upto repo & then release
git commit -a -m "Preparing for release"
git push origin HEAD

sbt release

#Be sure to enter the pgp key & sbt credentials for sonatype
#See here for more info: http://www.scala-sbt.org/0.13/docs/Using-Sonatype.html

#Bump version to next SNAPSHOT version:
  examples/project/plugins.sbt
  examples/project/ExampleBuild.scala

#Run Test
bin/test

#Push SNAPSHOT version up to master
git commit -a -m "Setting next SNAPSHOT"
git push origin HEAD


