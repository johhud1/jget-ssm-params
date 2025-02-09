
qdev:
    mvn quarkus:dev -P '!checkerframework'

bnative:
    mvn package -Pnative

# TODO: figure out a nicer release workflow?
# don't forget to run mvn release:prepare release:perform before..
release-dist:
    mvn -Dnative,dist package

release-dryrun:
    ./mvnw -Prelease jreleaser:full-release -Djreleaser.select.current.platform -Djreleaser.dry.run=true

jrelease:
    ./mvnw -Prelease jreleaser:full-release -Djreleaser.select.current.platform 