run `bin/test`

bump version numbers:

To next release version:
README.md

To next snapshot version:
examples/example-tests/build.sbt
examples/example-tests-jvm/build.sbt
examples/example-tests-sjs/build.sbt

run `git commit -a -m "Preparing for release"`
run `git push origin HEAD`

run `sbt release`

