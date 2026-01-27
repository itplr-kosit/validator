# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## Unreleased

## 1.6.1 - work in progress

### Changed

- (BUILD) [#176] The Maven Central deployed pom.xml properly includes runtime dependencies

## 1.6.0 - 2025-11-07

### Added

- (CORE) [GitHub #127](https://github.com/itplr-kosit/validator/issues/127) New API method `Result.getCustomFailedAsserts()` to access failed asserts with custom error levels

### Fixed

- (DOC) [GitHub PR#166](https://github.com/itplr-kosit/validator/pull/166) Fixed broken links in `docs/api.md` 

### Changed

- (CORE) Migration from javax to jakarta xml bind
- (DOC) [GitHub PR#132](https://github.com/itplr-kosit/validator/pull/132) Updated the link to the example Validator scenario configuration
- (BUILD) Support for *building and compilation* is restricted to the following Java versions:
    - Java 11: any version &ge; 11.0.23
    - Java 12 to 16 will not work
    - Java 17: any version &ge; 17.0.11
    - Java 18 to 20 will not work
    - Any version from Java 21 onwards will work
    - The reason for this is the usage of the `-proc:full` compiler parameter which in turn is needed for Lombok usage in JDK 23+.

### Removed

- (CORE) java 8 support. new default jdk 11

## 1.5.2 - 2025-09-01

### Fixed

- (BUILD) [#148](https://projekte.kosit.org/kosit/validator/-/issues/148) Regression that due to renaming from `validationtool` to `validator` the distribution zip did not contain all jars anymore

## 1.5.1 - 2025-09-01

### Fixed

- (CORE) [#130](https://projekte.kosit.org/kosit/validator/-/issues/130) Check result to stdout causes an exception. This also fixes [GitHub #131](https://github.com/itplr-kosit/validator/issues/131)
- (CORE) [#131](https://projekte.kosit.org/kosit/validator/-/issues/131) `UnsupportedOperationException` because of read-only list. This also fixes [GitHub #136](https://github.com/itplr-kosit/validator/issues/136)
- (CLI) [#104](https://projekte.kosit.org/kosit/validator/-/issues/104) made the usage of the `-r` parameter optional, if only one unnamed scenario is used
- (CLI) [#145](https://projekte.kosit.org/kosit/validator/-/issues/145) If the CLI is invoked without any parameter, the usage is shown twice
- (DOC) [#129](https://projekte.kosit.org/kosit/validator/-/issues/129) API documentation is outdated. This also fixes [GitHub #130](https://github.com/itplr-kosit/validator/issues/130)
- (BUILD) [#62](https://projekte.kosit.org/kosit/validator/-/issues/62) Surefire Test Error running `de.kosit.validationtool.impl.xml.RemoteResolvingStrategyTest` fails without `http.proxy` setting
- (BUILD) [#110](https://projekte.kosit.org/kosit/validator/-/issues/110) reactivated the GitLab CI environment

### Added

- (BUILD) [#140](https://projekte.kosit.org/kosit/validator/-/issues/140) prepare pom.xml to be able to release to Maven Central
- (BUILD) [#144](https://projekte.kosit.org/kosit/validator/-/issues/144) created a Maven profile to release on Maven Central

### Changed

- (CORE) [#109](https://projekte.kosit.org/kosit/validator/-/issues/109) dependencies were updated to the latest Java 1.8 compatible versions
  - Bump [Saxon HE](https://www.saxonica.com/documentation11/documentation.xml) to 12.8
  - Bump [jaxb-ri](https://github.com/eclipse-ee4j/jaxb-ri) to 2.3.9
  - Bump [SLF4J](https://www.slf4j.org/) to 2.0.17
- (CORE) [#136](https://projekte.kosit.org/kosit/validator/-/issues/136) removed IDE project folders from git
- (BUILD) [#135](https://projekte.kosit.org/kosit/validator/-/issues/135) protected specific git branches
- (BUILD) [#137](https://projekte.kosit.org/kosit/validator/-/issues/137) GitLab CI should only run on Java LTS versions as well as the latest Java version
- (BUILD) [#147](https://projekte.kosit.org/kosit/validator/-/issues/147) Change Maven coordinates from `de.kosit:validationtool` to `org.kosit:validator`

## 1.5.0

### Fixed

- (CLI) [#93](https://projekte.kosit.org/kosit/validator/-/issues/93) Remove usage information, when validation failed
- (CLI) [#95](https://projekte.kosit.org/kosit/validator/-/issues/95) NPE when using empty repository definition (-r "")
- (CORE) [GitHub #101](https://github.com/itplr-kosit/validator/issues/101) Role is null in FailedAssert

### Added

- (CLI)  Support for multiple configurations and multiple repositories. See [cli documentation](docs/cli.md) for details
- (API) Possibility to use preconfigured Saxon `Processor` instance for validation

### Changed

- (CORE) [GitHub #100](https://github.com/itplr-kosit/validator/issues/100) Make createReport optional
- (DAEMON) UI rewrite based on [Docusaurs](https://docusaurus.io)
- (API)  [ResolvingConfigurationStrategy.java#getProcessor()](de/kosit/validationtool/api/ResolvingConfigurationStrategy) is removed.
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
- [#75](https://github.com/itplr-kosit/validator/issues/75) Improve logging on invalid documents

## 1.4.1

### Fixed

- Allow more than 3 customLevel elements in scenarios (see xrechnung
  configuration [issue 49](https://github.com/itplr-kosit/validator-configuration-xrechnung/issues/49))
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
  in [DefaultResult.java](https://github.com/itplr-kosit/validator/blob/main/src/main/java/de/kosit/validationtool/impl/DefaultResult.java)
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
  through [Result.java](https://github.com/itplr-kosit/validator/blob/main/src/main/java/de/kosit/validationtool/api/Result.java)
  - *Result#getFailedAsserts()* returns a list of failed asserts found by schematron
  - *Result#isSchematronValid()* convinience access to evaluate whether schematron was processed without any *
    FailedAsserts*

### Changed

- *Result#getAcceptRecommendation()* does not *only* work when *acceptMatch* is configured in the scenario
  - schema correctness is a precondition, if the checked instance is not valid, this evaluates to *REJECTED*
  - if *acceptMatch* is configured, the result is based on the boolean result of the xpath expression evaluated against
    the generated report
  - if *no* *acceptMatch* is configured, the result is based on evaluation of schema and schematron correctness
  - *UNDEFINED* is only returned, when processing is stopped somehow
- *Result#isAcceptable()* can now evaluate to true, when no *acceptMatch* is configured (see above)

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

- Enhanced API-Usage e.g. return *Result* object with processing information
- Support loading scenarios and content from a JAR-File
- Simple Daemon-Mode exposing validation functionality via http
- cli option to serialize the 'report input' xml document to *cwd* (current working directory)
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
