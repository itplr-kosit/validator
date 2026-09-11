# KoSIT Validator

[![Maven Central](https://img.shields.io/maven-central/v/org.kosit/validator)](https://central.sonatype.com/artifact/org.kosit/validator)
[![Apache 2.0 license](https://img.shields.io/badge/license-Apache%202-blue)](https://www.apache.org/licenses/LICENSE-2.0)


## Introduction

The KoSIT Validator is an XML validation engine to validate XML files of various formats. It basically does the following in order:

1. identifies actual XML format and matches it with a scenario 
1. validates the XML file with the validation artifacts (XML Schema and Schematron rules) defined in the matched scenario 
1. computes an decision recommendation (according the supplied schema and rules)
1. generates a Conformance Validation Report (CVR)

The Validator depends on self defined [scenarios](docs/configurations.md) in order to fully configure the whole process in detail.


See [architecture](docs/architecture.md) for more information on the whole validation process.


## Validation configurations

The Validator is just an engine and does not know anything about XML documents and has no own validation rules.
Validation rules and details are defined in [validation scenarios](docs/configurations.md) which are used to fully configure the validation process.
All configurations are self-contained modules which are deployed and developed on their own.

### Example validation configurations

> [!warning]
> These are links to 1.6 validator configurations and do not yet work for 2.0.

Here are some public validation configurations:

* Validation Configuration for [XRechnung](https://xeinkauf.de/xrechnung/):
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-xrechnung)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-xrechnung/releases) can also be downloaded
* Validation Configuration for [Peppol BIS Billing](https://docs.peppol.eu/poacc/billing/3.0/):
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-bis)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-bis/releases) can also be downloaded
* Validation Configuration for [XGewerbeanzeige](https://xgewerbeanzeige.de/)
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-xgewerbeanzeige)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-xgewerbeanzeige/releases) can also be downloaded

## Usage

The Validator can be used in three different ways:

* as standalone application running from the CLI
* as library embedded within a custom application

### Standalone Command Line Interface (CLI)

**Important hint**: since v2.0.0 the filename has been changed from `validator-*` to `validator-cli-*`

This is due to the separation of validator into distinct cli and server modules.

The general way using the CLI is:

```shell
java -jar validator-cli-<version>-standalone.jar -s <scenario-config-file> [-r <repository-path>]
[OPTIONS] [FILE] [FILE] [FILE] ...
```

The help option displays further CLI options to customize the process:

```shell
java -jar validator-cli-<version>-standalone.jar --help
```

A concrete example with a specific Validator configuration can be found on 
[validator-configuration-bis](https://github.com/itplr-kosit/validator-configuration-bis)

The [CLI documentation](./docs/cli.md) shows further configuration options.

### Application User Interface (API / embedded usage)

The Validator can also be used in own Java Applications via the API. An example use of the API as follows:

```java
Path scenarios = Paths.get("scenarios.xml");
ScenarioSet config = ScenarioSet.load(scenarios.toUri()).build(ProcessorProvider.getProcessor());

// the engine runs the canonical pipeline along the configured scenarios; engineInformation names your application
ValidationEngine<ConformanceValidationResult> validator = new ConformanceValidation(engineInformation, ProcessorProvider.getProcessor(), config);

CTReadResource document = ReadResource.of(Resource.of(testDocument), resourceHelper);
ConformanceValidationResult result = validator.validate(document);

// examine the result here: result.getDecision(), result.getRationale(), result.writeCvr(out)
```

The  [API documentation](./docs/api.md) shows further configuration options.


> [!note]
> With Java 11+, you need to include a dependency to `org.glassfish.jaxb:jaxb-runtime` in your project explicitly,
as that dependency is marked `optional` in this project and will thus not be resolved transitively.

## Packages

The Validator `distribution` contains the following artifacts:

1. `validator-cli-`<version>`.jar`: Thin-JAR for local execution
1. `validator-server-`<version>`-runner.jar`: Standalone Validation Server
1. `libs/`: directory containing all other validator modules and dependencies (incl. optional)

### Installation

Download from the following sources is possible:

* GitHub releases: https://github.com/itplr-kosit/validator/releases
    * This release contains a ZIP file with all the different JAR variants

### Maven 

* Maven Central with the below coordinates (replace `x.y.z` with the actual version to use)

```xml
<dependency>
    <groupId>org.kosit</groupId>
    <artifactId>validator-core</artifactId>
    <version>x.y.z</version>
</dependency>
```

To use the standalone CLI version with Maven coordinates:

```xml
<dependency>
    <groupId>org.kosit</groupId>
    <artifactId>validator-cli</artifactId>
    <version>x.y.z</version>
    <classifier>standalone</classifier>
</dependency>
```

## 2.x Roadmap

This section describes the next steps planned in the Validator development for 2.x.

* Finish version 2.0.0 which will include major API incompatibilities - Winter 2026 
  * Fix API based on Feedback on the technical previews
  * Finalize  of Validator Specification (v1) incl. Conformance Validation Report

* The release of version 2.0.0 implies a feature-freeze for version 1.6

## Authors & Acknowledgements

We are thankful to numerous third-party [contributors](https://github.com/itplr-kosit/validator/graphs/contributors).

## License

The Validator is licensed under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).
