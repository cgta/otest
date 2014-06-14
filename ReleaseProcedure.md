run `bin/test`

bump version numbers:

To next RELEASE version:
README.md

git commit -a -m "Preparing for release"
git push origin HEAD

sbt release

To next SNAPSHOT version:
examples/project/plugins.sbt
examples/project/ExampleBuild.scala


