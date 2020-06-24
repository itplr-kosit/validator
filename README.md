# Validator

The validator is an XML validation-engine. It validates XML documents against XML Schema and Schematron Rules depending on self defined [scenarios](docs/configurations.md) which are used to fully configure the validation process.
The validator always outputs a [validation report in XML](docs/configurations.md#validators-report) including all validation errors and data about the validation.

See [architecture](docs/architecture.md) for informations about the actual validation process.

## Packages

The validator distribution contains the following artifacts:

1. **validationtool-`<version>`.jar**: Java library for embedded use within an application
1. **validationtool-`<version`>-standalone.jar**: Uber-JAR for standalone usage containing all dependencies in one jar file. This file comes with JAXB *embedded* and can be used with Java 8 and Java >= 11)
1. **validationtool-`<version`>-java8-standalone.jar**: Uber-JAR for standalone usage with Java JDK 8 containing all dependencies in one jar file. This file file *does not* contain JAXB and depends on the bundled version of the JDK.
1. **libs/***: directory containing all (incl. optional) dependencies of the validator


## Validation Configurations

The validator is just an engine and does not know anything about XML Documents and has no own validation rules.
Validation rules and details are defined in [validation scenarios](docs/configurations.md) which are used to fully configure the validation process.
All configurations are self-contained modules which are deployed and developed on their own.

### Third Party Validation Configurations

Currently, there are two public third party validation configurations available.

* Validation Configuration for [XRechnung](http://www.xoev.de/de/xrechnung):
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-xrechnung)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-xrechnung/releases) can also be downloaded
* Validation Configuration for [XGewerbeanzeige](https://xgewerbeanzeige.de/)
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-xgewerbeanzeige)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-xgewerbeanzeige/releases) can also be downloaded

## Usage

The validator is designed to be used in three different ways:

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

### Daemon-Mode

You can also start the validator as a HTTP-Server. Just start it in _Daemon-Mode_ with the `-D` option.

```shell
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> -D
```


The [daemon documentation](./docs/daemon.md) shows more usage details and further configuration options.

