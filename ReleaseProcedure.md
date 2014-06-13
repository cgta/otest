run `bin/test`

bump version numbers:

To next release version:
README.md

To next snapshot version:
examples/project/plugins.sbt
examples/project/ExampleBuild.scala

git commit -a -m "Preparing for release"
git push origin HEAD

sbt release

