# Validator

The validator is an XML validation-engine. It validates XML documents against XML Schema and Schematrons depending on self defined [scenarios](docs/configurations) which are used to fully configure the validation process.
The validator always outputs a [validation report in XML](docs/configurations.md#validators-report) including all validation errors and data about the validation.

## Releases

Two kind of releases are available:

Das Prüftool steht in zwei Varianten zur Verfügung:

- als standalone/cli, die von der Kommandozeile aus aufgerufen werden kann
- als Bibliothek/api, die in eigene Anwendungen integriert werden kann

* die *Standalone-Distribution*  enthält das Uber-Jar mit allen Klassen zur Verarbeitung von Eingaben aus der Kommandozeile,
sowie für Ausgabeoptionen für Ergebnisse. Sämtliche Abhängigkeiten sind im Jar gebundlet  und das Jar-File ist 'ausführbar'.
* die *Full-Distribution* enthält darüber sämtlichen weiteren Varianten des `validationtools` sowie die benötigten Abhängigkeiten.

## Build

### Requirements

* Maven > 3.0.0
* Java > 8 update 111

### Procedure

 `mvn install` generates two different packages in the `dist` directory:

## Validation Configurations

The validator is just an engine and does not know anything about XML Documents and has no own validation rules.

Validation rules and details are defined in [validation scenarios](docs/configurations) which are used to fully configure the validation process.

All configurations are self-contained modules and deployed on their own.

### Third Party Validation Configurations

Currently, there are two public third party validation configurations available.

* Validation Configuration for [XRechnung](http://www.xoev.de/de/xrechnung) is available on
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-xrechnung)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-xrechnung/releases) can also be downloaded
* Validation Configuration for XGewerbeanzeige
  * Source code is available on [GitHub](https://github.com/itplr-kosit/validator-configuration-xgewerbeanzeige)
  * [Releases](https://github.com/itplr-kosit/validator-configuration-xgewerbeanzeige/releases) can also be downloaded

## Usage

### Standalone Command-Line Interface

The general way using the CLI is:

```shell
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> [OPTIONS] [FILE] [FILE] [FILE] ...
```

You can more CLI options by

```shell
java -jar  validationtool-<version>-standalone.jar --help
```

A concrete exmaple with a specific validator configuration can be found on [GitHub](https://github.com/itplr-kosit/validator-configuration-xrechnung

### Daemon-Mode

You can also start the validator as an HTTP-Server. Just start it in _Daemon-Mode_ with the `-D` option.

```shell
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> -D
```

Per default the HTTP-Server listens on _localhost_ at Port 8080.

You can configure it with `-H` for IP Adress and `-P` for port number:

```shell
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> -D -H 192.168.1.x -P 8081
```

You can HTTP-POST to  `/` and the response will return the report document as defined in your validator configuration.

Additionally there is the GET `/health` endpoint which can be used by monitoring systems.

### Application User Interface

The validator can also be used in own Java Applications via the API. Details can be [found here](./docs/api.md).
