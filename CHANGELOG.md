# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

# 1.6.0 (to be released)

### Changed

- (CORE) update to java 11 and to jakarta namespace

# 1.5.0

### Fixed

- (CLI) [#93](https://projekte.kosit.org/kosit/validator/-/issues/93) Remove usage information, when validation failed
- (CLI) [#95](https://projekte.kosit.org/kosit/validator/-/issues/95) NPE when using empty repository definition (-r "")
- (CORE) [#101](https://github.com/itplr-kosit/validator/issues/101) Role is null in FailedAssert

### Added

- (CLI)  Support for multiple configurations and multiple repositories. See [cli documentation](docs/cli.md) for details
- (API) Possibility to use preconfigured Saxon `Processor` instance for validation

### Changed
- (CORE) [#100](https://github.com/itplr-kosit/validator/issues/100) Make createReport optional
- (DAEMON) UI rewrite based on [Docusaurs](https://docusaurus.io)
- (
  API)  [ResolvingConfigurationStrategy.java#getProcessor()](de/kosit/validationtool/api/ResolvingConfigurationStrategy)
  is removed.
- (CORE) Bump [Saxon HE](https://www.saxonica.com/documentation11/documentation.xml) to 11.4
- (CORE) Bump [jaxb-ri](https://github.com/eclipse-ee4j/jaxb-ri) to 2.3.7
- (CORE) Various other dependency updates. See pom.xml

- (CORE) CLI parsing based on pico-cli, commons-cli is removed

## 1.4.2

### Fixed

- (CLI)  [#74](https://projekte.kosit.org/kosit/validator/-/issues/74) fix ansi output of the cli version
- [#80](https://github.com/itplr-kosit/validator/issues/80) using classloader to initialize jaxb context (to support
  usage in OSGi
  environments)
- [#75] (https://github.com/itplr-kosit/validator/issues/75) Improve logging on invalid documents

## 1.4.1

### Fixed

- Allow more than 3 customLevel elements in scenarios (see xrechnung
  configuration [ issue 49](https://github.com/itplr-kosit/validator-configuration-xrechnung/issues/49))
- Remove saxon signature from java8 uber-jar (see [67](https://github.com/itplr-kosit/validator/issues/67))

## 1.4.0

### Fixed

- date conversion when
  using [ConfigurationBuilder#date(Date)](https://github.com/itplr-kosit/validator/blob/d7beb1040418ae5cbeb9427532fd87482f55756c/src/main/java/de/kosit/validationtool/config/ConfigurationBuilder.java#L109)
- (CLI)  [#51](https://github.com/itplr-kosit/validator/issues/51) Suffix of report xml is missing
- [#53](https://github.com/itplr-kosit/validator/issues/53) Fix copyright and licensing information
- [#56](https://github.com/itplr-kosit/validator/issues/56) `namespace` element content needs trimming
- [DAEMON] [#57](https://github.com/itplr-kosit/validator/issues/57) Reading large inputs correctly

### Added

- read saxon XdmNode with InputFactory
- (CLI)  custom output without the various log messages
- (CLI)  options to set the log level (`-X` = full debug output, `-l <level>` set a specific level)
- (CLI)  return code is not 0 on rejected results
- (CLI)  read (single) test target from stdin
- [DAEMON] name inputs via request URI

### Changed

- InputFactory has methods to read any java.xml.transform.Source as Input not only StreamSources
- InputFactory uses a generated UUID as name for SourceInput, if no "real" name can be derived
- saxon dependency update (minor, 9.9.1-7)
- [DAEMON] proper status codes when returning results (see [daemon documentation](./docs/daemon.md#status-codes))

## 1.3.1

### Fixed

- `getFailedAsserts()` and `isSchematronValid()`
  in [DefaultResult.java](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/impl/DefaultResult.java)
  do not reflect actual schematron validation result
- processing aborts on schematron execution errors (e.g. errors within schematron logic). The validator now generates a
  report in such cases.
- exception while resolving when using XSLT's `unparsed-text()` function within report generation

### Added

- (CLI)  summary report

### Changed

- engine info contains version number of the validator (configurations can output this in the report for maintainance
  puposes)
- options to customize serialized report file names (cmdline only) via `--report-prefix` and `--report-postfix`
- remove unused dependency Apache Commons HTTP

## 1.3.0

### Added

- Added a builder style configuration API to configure scenarios
- Added an option to configure xml security e.g. to load from http sources or not from a specific repository
  (so loading is configurable less restrictive, default strategy is to only load from a local repository)
- Support java.xml.transform.Source as Input

### Changed

- Inputs are NOT read into memory (e.g. Byte-Array) prior processing within the validator. This reduces memory
  consumption.
- Overall processing of xml files is based on Saxon s9api. No JAXP or SAX classes are used by
  the validator (this further improves performance and memory consumption)

### Deprecations

- CheckConfiguration is deprecated now. Use Configuration.load(...) or Configuration.build(...)

## 1.2.1

### Fixed

- Validator is creating invalid createReportInput xml in case of no scenario match

## 1.2.0

### Added

- Provide access to schematron result
  through [Result.java](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/api/Result.java)
  - *Result#getFailedAsserts()* returns a list of failed asserts found by schematron
  - *Result#isSchematronValid()* convinience access to evaluate whether schematron was processed without any *
    FailedAsserts*

### Changed

- *Result#getAcceptRecommendation()* does not _only_ work when _acceptMatch_ is configured in the scenario
  - schema correctness is a precondition, if the checked instance is not valid, this evaluates to _REJECTED_
  - if _acceptMatch_ is configured, the result is based on the boolean result of the xpath expression evaluated against
    the generated report
  - if *no* _acceptMatch_ is configured, the result is based on evaluation of schema and schematron correctness
  - _UNDEFINED_ is only returned, when processing is stopped somehow
- *Result#isAcceptable()* can now evaluate to true, when no _acceptMatch_ is configured (see above)

## 1.1.3

### Fixed

- XXE vulnerability when reading xml documents with Saxon [#44](https://github.com/itplr-kosit/validator/issues/44)
- validator unintentionally stopped when schematron processing has errors.
  See  [#41](https://github.com/itplr-kosit/validator/issues/41).

## 1.1.2

### Fixed

- NPE in Result.getReportDocument for malformed xml input

## 1.1.1

### Added

- Convenience method for accessing information about well-formedness in Result
- Convenience method for accessing information about schema validation result in Result

### Fixed

- NPE when validating non-XML files

## 1.1.0

### Added

- Enhanced API-Usage e.g. return _Result_ object with processing information
- Support loading scenarios and content from a JAR-File
- Simple Daemon-Mode exposing validation functionality via http
- cli option to serialize the 'report input' xml document to _cwd_ (current working directory)
- Documentation in `docs` folder

### Changed

- Use s9api (e.g. XdmNode) internally for loading and holding xml objects (further memory optimization)
- Builds with java 8 and >= 11
- Packages for java8 and java >= 11 (with jaxb included)
- Translated README.md

## 1.0.2

### Fixed

- Memory issues when validating multiple targets

## 1.0.1

### Changed

- Removed XRechnung configuration from release artifacts and source (moved
  to [own repository](https://github.com/itplr-kosit/validator-configuration-xrechnung) )

## 1.0.0

- Initial Release
