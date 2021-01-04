# KoSIT Validator
- [Introduction](#introduction)
- [Validation Configurations](#validation-configurations)
    * [Third Party Validation Configurations](#third-party-validation-configurations)
- [Usage](#usage)
    * [Standalone Command-Line Interface](#standalone-command-line-interface)
    * [Application User Interface (API / embedded usage)](#application-user-interface--api---embedded-usage-)
    * [Daemon-Mode](#daemon-mode)
- [Packages](#packages)

## Introduction
The validator is an XML validation engine to validate and process  XML files in various formats. It basically does the following in order:

1. identify actual xml format 
1. validate the xml file (using schema  and schematron rules)
1. generate a custom report / extract custom data from the xml file
1. compute an acceptance status (according the supplied schema and rules)

The validator depends on self defined [scenarios](docs/configurations.md) which are used to fully configure the process.
It always creates a [validation report in XML](docs/configurations.md#validators-report). The actual content of this is controlled by the scenario.

See [architecture](docs/architecture.md) for information about the actual validation process.


## Validation configurations

The validator is just an engine and does not know anything about XML documents and has no own validation rules.
Validation rules and details are defined in [validation scenarios](docs/configurations.md) which are used to fully configure the validation process.
All configurations are self-contained modules which are deployed and developed on their own.

### Third party validation configurations

Currently, there are two public third party validation configurations available.

* Validation Configuration for [XRechnung](http://www.xoev.de/de/xrechnung):
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-xrechnung)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-xrechnung/releases) can also be downloaded
* Validation Configuration for [XGewerbeanzeige](https://xgewerbeanzeige.de/)
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-xgewerbeanzeige)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-xgewerbeanzeige/releases) can also be downloaded

## Usage

The validator can be used in three different ways:

* as standalone application running from the cli
* as library embedded within a custom application
* as a daemon providing a http interface

### Standalone Command-Line Interface

The general way using the CLI is:

```shell
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> [OPTIONS] [FILE] [FILE] [FILE] ...
```

The help option displays further CLI options to customize the process:

```shell
java -jar  validationtool-<version>-standalone.jar --help
```

A concrete example with a specific validator configuration can be found on 
[GitHub](https://github.com/itplr-kosit/validator-configuration-xrechnung)

The  [CLI documentation](./docs/cli.md) shows further configuration options.

### Application User Interface (API / embedded usage)

The validator can also be used in own Java Applications via the API. An example use of the API as follows:

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
as that dependency is marked `optional` in this project and 
will thus not be resolved transitively.

### Daemon-Mode

You can also start the validator as a HTTP-Server. Just start it in _Daemon-Mode_ with the `-D` option.

```shell
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> -D
```


The [daemon documentation](./docs/daemon.md) shows more usage details and further configuration options.

## Packages

The validator distribution contains the following artifacts:

1. **validationtool-`<version>`.jar**: Java library for embedded use within an application
1. **validationtool-`<version`>-standalone.jar**: Uber-JAR for standalone usage containing all dependencies in one jar file. This file comes with JAXB *embedded* and can be used with Java 8 and Java >= 11)
1. **validationtool-`<version`>-java8-standalone.jar**: Uber-JAR for standalone usage with Java JDK 8 containing all dependencies in one jar file. This file file *does not* contain JAXB and depends on the bundled version of the JDK.
1. **libs/***: directory containing all (incl. optional) dependencies of the validator
