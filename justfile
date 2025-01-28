
qdev:
    mvn quarkus:dev -P '!checkerframework'

bnative:
    mvn package -Pnative
