# KoSIT Validator

[![Maven Central](https://img.shields.io/maven-central/v/org.kosit/validator)](https://central.sonatype.com/artifact/org.kosit/validator)
[![Apache 2.0 license](https://img.shields.io/badge/license-Apache%202-blue)](https://www.apache.org/licenses/LICENSE-2.0)


## Introduction

The Validator is an XML validation engine to validate and process XML files in various formats. It basically does the following in order:

1. identify actual XML format 
1. validate the XML file (using schema  and schematron rules)
1. generate a custom report / extract custom data from the XML file
1. compute an acceptance status (according the supplied schema and rules)

The Validator depends on self defined [scenarios](docs/configurations.md) in order to fully configure the whole process.
It always creates a [validation report in XML](docs/configurations.md#validators-report). The actual content of the report can also be controlled by the scenario.

See [architecture](docs/architecture.md) for information about the whole validation process.


## Validation configurations

The Validator is just an engine and does not know anything about XML documents and has no own validation rules.
Validation rules and details are defined in [validation scenarios](docs/configurations.md) which are used to fully configure the validation process.
All configurations are self-contained modules which are deployed and developed on their own.

### Example validation configurations

Here are some public validation configurations:

* Validation Configuration for [XRechnung](https://xeinkauf.de/xrechnung/):
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-xrechnung)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-xrechnung/releases) can also be downloaded
* Validation Configuration for [Peppol BIS Billing](docs.peppol.eu/poacc/billing/3.0/):
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-bis)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-bis/releases) can also be downloaded
* Validation Configuration for [XGewerbeanzeige](https://xgewerbeanzeige.de/)
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-xgewerbeanzeige)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-xgewerbeanzeige/releases) can also be downloaded

## Usage

The Validator can be used in three different ways:

* as standalone application running from the CLI
* as library embedded within a custom application
* as a daemon providing an http interface

### Standalone Command Line Interface (CLI)

**Important hint**: since v1.5.1 the filename has been changed from `validationtool-*` to `validator-*`

The general way using the CLI is:

```shell
java -jar validator-<version>-standalone.jar -s <scenario-config-file> [-r <repository-path>]
[OPTIONS] [FILE] [FILE] [FILE] ...
```

The help option displays further CLI options to customize the process:

```shell
java -jar validator-<version>-standalone.jar --help
```

A concrete example with a specific Validator configuration can be found on 
[validator-configuration-bis](https://github.com/itplr-kosit/validator-configuration-bis)

The [CLI documentation](./docs/cli.md) shows further configuration options.

### Application User Interface (API / embedded usage)

The Validator can also be used in own Java Applications via the API. An example use of the API as follows:

```java
Path scenarios = Paths.get("scenarios.xml");
Configuration config = Configuration.load(scenarios.toUri());
Input document = InputFactory.read(testDocument);

Check validator = new DefaultCheck(config);
Result validationResult = validator.checkInput(document);

// examine the result here
```

The  [API documentation](./docs/api.md) shows further configuration options.

**Note:** With Java 11+, you need to include a dependency to `org.glassfish.jaxb:jaxb-runtime` in your project explicitly,
as that dependency is marked `optional` in this project and will thus not be resolved transitively.

### Daemon-Mode

You can also start the validator as a HTTP-Server. Just start it in _Daemon-Mode_ with the `-D` option.

```shell
java -jar  validator-<version>-standalone.jar  -s <scenario-config-file> -D
```


The [daemon documentation](./docs/daemon.md) shows more usage details and further configuration options.

## Packages

The Validator distribution contains the following artifacts:

1. **validator-`<version>`.jar**: Java library for embedded use within an application
1. **validator-`<version>`-standalone.jar**: Uber-JAR for standalone usage containing all dependencies in one jar file. This file comes with JAXB *embedded* and can be used with Java >= 11)
1. **libs/**: directory containing all (incl. optional) dependencies of the validator

## Installation

Download from the following sources is possible:

* GitHub releases: https://github.com/itplr-kosit/validator/releases
    * This release contains a ZIP file with all the different JAR variants
* Maven Central with the below coordinates (replace `x.y.z` with the actual version to use)

```xml
<dependency>
    <groupId>org.kosit</groupId>
    <artifactId>validator</artifactId>
    <version>x.y.z</version>
</dependency>
```

To use the standalone version with Maven coordinates, add the respective classifier:

```xml
<dependency>
    <groupId>org.kosit</groupId>
    <artifactId>validator</artifactId>
    <version>x.y.z</version>
    <classifier>standalone</classifier>
</dependency>
```

## Roadmap

This section describes the next steps planned in the Validator development.

* Release version 1.6.0 based on Java 11 and using Jakarta 4.x. - Autumn 2025
    * Drop support of version 1.5.x when version 1.6 is released
* Develop version 2.0.0 which will include major API incompatibilities - Winter 2025 
    * Rework scenarios.xml
    * Rework report output engine
    * Change the output type to [XVRL](https://github.com/xproc/xvrl)-based document types &rarr; this implies that existing XSL templates need to be updated
    * Consider multi Schematron engine support
    * Extract the daemon mode into its own submodule
    * Consider extracting the CLI into its own submodule
* The release of version 2.0.0 implies a feature-freeze for version 1.6

## Authors & Acknowledgements

We are thankful to numerous third-party [contributors](https://github.com/itplr-kosit/validator/graphs/contributors).

## License

The Validator is licensed under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).
