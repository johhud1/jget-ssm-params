# jget-ssm-params

A java port of https://github.com/justmiles/go-get-ssm-params

Installation: download the binary from the releases page and execute. Currently only building for linux-x86_64

`./jget-ssm-params --help`
```
Usage: get-ssm-params [-hV] [-o=<output>] [-p=<path>] [-t=<templatePath>]
gets parameters from AWS SSM parameter store and outputs them to local environment.
Region and other SSM client configuration can be set via environment variables.
See https://docs.quarkiverse.io/quarkus-amazon-services/dev/amazon-ssm.html#quarkus-amazon-ssm_section_quarkus-ssm for detailed SSM client configuration options
  -h, --help              Show this help message and exit.
  -o, --output=<output>   Format of the output that the SSM parameters will be written in
  -p, --path=<path>
  -t, --template=<templatePath>
                          Path to a template file that will be used to render the output
  -V, --version           Print version information and exit.
  ```


Mainly a test bed for exploring Quarkus and various java tools.

This project uses Quarkus, to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

The project is following Convetional Commits: read more at <https://www.conventionalcommits.org/en/v1.0.0/>.

The project is using JReleaser for releases: read more at <https://jreleaser.org/>.


## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/jget-ssm-params-0.1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- Amazon SSM ([guide](https://docs.quarkiverse.io/quarkus-amazon-services/dev/amazon-ssm.html)): Connect to Amazon SSM
- Picocli ([guide](https://quarkus.io/guides/picocli)): Develop command line applications with Picocli
