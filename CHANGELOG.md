# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2.0.0 - work in progress

### Changed

- Modularized. Server application made available as Quarkus App
- Changed all source code comments to English
- Updated to use the SVRL XSD that matches the Schematron 2025 standard
- (DAEMON) remove Daemon mode
- (BUILD) [#169](https://projekte.kosit.org/kosit/validator/-/work_items/169) Removed the usage of Lombok
- (BUILD) [#185](https://projekte.kosit.org/kosit/validator/-/work_items/185) The minimum Java version is now 25
- (CORE) [#198](https://projekte.kosit.org/kosit/validator/-/work_items/198) Replaced all `String.format` calls with native inline String concatenation
- (BUILD) Extracted the scenario XSD and its JAXB binding into the new submodule `scenario`, built as the fourth module and depending on `jaxb` only
- (BUILD) Extracted the generic helper classes of package `org.kosit.base` from the submodule `jaxb` into the new submodule `base`, built as the first module and depending on no other module. The submodule `jaxb` now depends on `base`
- (API) The generated scenario model moved from package `org.kosit.validator.model.scenarios` to `org.kosit.validator.scenario.model`
- (API) `org.kosit.validator.impl.ScenariosConversionService` moved and renamed to `org.kosit.validator.scenario.impl.ScenarioConversionService`
- (API) `ValidatorSchemas.SCENARIOS_XSD_PATH` was replaced by `org.kosit.validator.scenario.xsd.ScenarioSchemas.SCENARIOS_XSD_PATH`
- (API) `SchemaProvider.getScenarioSchema()` was replaced by `org.kosit.validator.scenario.xsd.ScenarioSchemaProvider.getScenarioSchema()`
- (API) Applied the Google Camel Case rules to all acronyms in type and method names, so `XML` became `Xml` and `XVRL` became `Xvrl`. This affects e.g. `XMLHelper` (now `XmlHelper`), `XMLReaderWrapper` (now `XmlReaderWrapper`), `AbstractXMLSyntaxError` (now `AbstractXmlSyntaxError`), `CompactXVRLReport` (now `CompactXvrlReport`), `CompactXVRLReportSummary` (now `CompactXvrlReportSummary`), `XVRLReportBuilder` (now `XvrlReportBuilder`), `XmlHelper.createSafeXMLInputFactory()` (now `createSafeXmlInputFactory()`) and `SchemaProvider.getXVRLSchema()` (now `getXvrlSchema()`)
- (API) The generated model types were renamed accordingly, because the underlying XSD type names changed: `XMLSyntaxError` is now `XmlSyntaxError`, `XMLSyntaxErrorSeverity` is now `XmlSyntaxErrorSeverity` and all `XVRL*Type` classes are now `Xvrl*Type`
- (API) All internal error handling was unified on `org.kosit.base.error.SimpleError`. The types `XmlError`, `XmlSyntaxError`, `XmlSeverity` and `XmlErrorImpl` were removed, and `VResult.getSchemaViolations()` now returns `List<SimpleError>`. The severity `FATAL_ERROR` no longer exists - it is mapped to `CTStandardSeverity.ERROR`
- (API) `SingleProcessingResult` takes and returns a `List` instead of a `Collection` of errors
- (API) The XVRL data model is serialization independent now and lives in package `org.kosit.xvrl.model`. All types are immutable, are created through the static `builder()` methods and can be derived from an existing instance via `toBuilder()`. A builder method that takes another data model type always has an overload that takes the builder of that type. Following the naming rules, the `Type` suffix was dropped, so `XvrlReportsType` is now `XvrlReports`, `XvrlReportType` is now `XvrlReport`, `XvrlDetectionType` is now `XvrlDetection` and so on
- (API) The JAXB generated XVRL model moved from package `org.kosit.xvrl.model` to `org.kosit.xvrl.jaxb` and is an implementation detail of reading and writing XML now. All XVRL usage in the modules `api`, `core`, `cli`, `server` and `client` was switched over to the new data model, including `VResult.getReportSummary()`, `ValidationClient.validate(File)`, `ValidationClient.validateWithMetadata(File)`, `CheckTask.Process`, `ProcessStepResult`, `BusinessReport`, `XvrlReportBuilder`, `XvrlDetectionBuilder`, `XvrlSupplementalBuilder`, `XvrlSerializer` and `XvrlHelper`
- (API) `XvrlConverter` reads and writes `org.kosit.xvrl.model.XvrlReports` instead of the JAXB type. It is a facade over the JAXB converter now and no longer extends `AbstractJaxbConverter`
- (API) The XVRL enums `XvrlSeverityType`, `XvrlValidityType` and `XvrlWorstType` are now `XvrlSeverity`, `XvrlValidity` and `XvrlWorst`. They offer `getID()`, `getFromIDOrNull(String)` and `getFromIDOrDefault(String, X)` instead of `value()` and `fromValue(String)`, so an unknown token no longer requires catching an `IllegalArgumentException`
- (API) Several XVRL accessors were renamed to match the project naming rules: `getId()` is now `getID()`, `XvrlReport.getDetection()` is now `getDetections()`, `XvrlProvenance.getLocation()` is now `getLocations()`, `XvrlSchema.getSchematypens()` is now `getSchemaTypeNs()`, `XvrlLocation.getXpath()` is now `getXPath()`, and `XvrlReports.getReportOrReportsOrDigest()` was replaced by `getAllItems()` plus the filtering accessors `getReports()`, `getReportSummaries()` and `getDigests()`
- (API) `XvrlTimestamp` carries a `java.time.OffsetDateTime` instead of a `javax.xml.datatype.XMLGregorianCalendar`
- (API) `CompactXvrlReport` and `CompactXvrlReportSummary` are mutable facades over the immutable data model now. `getOriginal()` materializes the collected state and returns a new object on every call
- (API) The foreign attributes of an XVRL object keep their insertion order, so the order of the CVRL attributes in the compact report is deterministic. Previously they were emitted in `HashMap` order
- (API) `CompactXvrlReport.addSchematronViolation` converts the rich text children (`dir`, `span`, `emph`) of an SVRL `failed-assert` text to their string representation, instead of copying the SVRL JAXB objects into the XVRL message content

### Added

- (API) Added `XmlHelper.createSafeSchemaFactory()` providing the hardened `SchemaFactory` that is now shared by the resolving strategies and the scenario schema provider
- (API) Added `DefaultSimpleError` as the default immutable implementation of `SimpleError`, to be created via the new fluent `SimpleErrorBuilder`
- (API) Added `SimpleError.getAsString()` and `SimpleError.log(Logger)` replacing the removed `XmlSyntaxError.log(Logger)`
- (API) Added the optional `SimpleError.getErrorCode()` including `hasErrorCode()` and the `SimpleErrorBuilder.errorCode(String)` setter. An empty error code is treated like none at all
- (API) Added a dependency free replica of the ph-diver DVR Coordinate and version handling to the submodule `base`: `org.kosit.base.dvr.coord` (`DVRCoordinate`, `IDVRCoordinate`, `DVRCoordinateException`), `org.kosit.base.dvr.version` (`DVRVersion`, `DVRVersionException`, `EDVRPreReleaseQualifier`, `DVRPseudoVersion`, `DVRPseudoVersionRegistry` and the pseudo version interfaces), `org.kosit.base.dvr.version.spi` (`IDVRPseudoVersionRegistrarSPI` plus the default registrar registered via `META-INF/services`) and `org.kosit.base.dvr.settings` (`DVRGlobalCoordinateSettings`, `DVRValidityHelper`)
- (API) Added `org.kosit.base.version.Version` as the generic 4 part version type (major, minor, micro, qualifier) backing the static DVR versions
- (API) Added `ObjectHelper.compare(T, T)` for `null`-safe comparisons, plus `StringHelper.getLength(CharSequence)`, `StringHelper.getExplodedArray(char, String[, int])`, `StringHelper.getExploded(char, String)`, `StringHelper.isInt(String)`, `StringHelper.parseInt(String, int)` and `StringHelper.parseIntObj(String)`
- (API) Added `org.kosit.xvrl.jaxb.XvrlJaxbCreator` and `org.kosit.xvrl.jaxb.XvrlJaxbReader` converting between the XVRL data model and its JAXB representation
- (API) Added `XvrlValueOf` modelling the XVRL `value-of` element that may occur inside an `XvrlMessage`
- (API) Added the XVRL base types `AbstractXvrlObject` (foreign attributes), `AbstractXvrlCommonObject` (the `common.attr` group) and `AbstractXvrlContentObject` (mixed content) together with their abstract builders. Mixed content is stored as `List<Object>`, but the `addContent` methods only accept the allowed types `String`, `org.w3c.dom.Node` and - for messages - `XvrlValueOf`
- (API) Added the marker interface `IXvrlReportsItem` implemented by `XvrlReport`, `XvrlReports` and `XvrlDigest`, denoting the types that may be a direct child of an `xvrl:reports` element
- (API) Added `XmlHelper.createValidNCName(String)` converting an arbitrary string to a valid `xs:NCName`, plus the accompanying `XmlHelper.isValidNCName(String)`, `XmlHelper.isNCNameStartChar(char)` and `XmlHelper.isNCNameChar(char)`

### Removed

- (API) Removed the XVRL mix-in types `org.kosit.xvrl.api.BaseDetection`, `org.kosit.xvrl.api.BaseMessage` and `org.kosit.xvrl.api.BaseReportSummary` as well as the base classes `org.kosit.xvrl.impl.AbstractXvrlReport` and `org.kosit.xvrl.impl.AbstractXvrlReportSummary`. They only existed to graft convenience methods onto the generated JAXB classes, which the data model provides natively: `BaseDetection.getAllMessages()` is `XvrlDetection.getAllMessageStrings()`, `BaseMessage.getMessageStrings()` is `AbstractXvrlContentObject.getContentStrings()` and `getAllErrors()` stayed on `XvrlReport` and `XvrlReports`. The `inheritance` JAXB plugin is no longer used for the XVRL model

### Fixed

- (BUILD) The `validator-api` module no longer emits stub `ObjectFactory` classes for the `svrl` and `scenario` packages, which shadowed the complete ones of the respective modules on the classpath
- (CLI) `Format.mergeCodes` no longer discards the plain formatting codes like `BOLD` or `UNDERLINE` and no longer emits background colors twice. `Code.isColor()` is `true` for the background colors as well, so the previous filter combination only ever selected the `BG_*` codes
- (API) The `xml:id` of all XVRL data model types is converted to a valid `xs:NCName` now. Values like `Report for eInvoice` previously made the marshalling of the XVRL report fail with `cvc-datatype-valid.1.2.1`
- (CORE) `CreateReportsTask` puts the name of the report resource into the `code` attribute of the created detection instead of into `xml:id`. As `xml:id` is of type `xs:ID` it must be unique per document, which a report resource name is not - two `createReport` elements using the same resource name made the marshalling of the XVRL report fail with `cvc-id.2`

## 1.6.3 - 2026-08-20

### Fixed

- (CORE) [GitHub Advisory](https://github.com/itplr-kosit/validator/security/advisories/GHSA-hg2c-p2m3-q29m) Fixed unrestricted URI resolution in STRICT_LOCAL mode allows remote stylesheet inclusion. Thanks to @gronke

### Changed

- (BUILD) Removed the usage of Lombok.

## 1.6.2 - 2026-02-17

### Changed

- (BUILD) [GitHub #173](https://github.com/itplr-kosit/validator/issues/173) The JAR files now contain details on the used third-party component licenses. Thanks to @cech12
- (BUILD) [GitHub #169](https://github.com/itplr-kosit/validator/issues/169) The `.zip` file created from `maven-assembly-plugin` now contains the correct xml-resolver dependencies. Thanks to @landrix for pointing that out
- (BUILD) [#179](https://projekte.kosit.org/kosit/validator/-/issues/179) Updated all dependencies to the latest suitable versions

## 1.6.1 - 2026-02-05

### Changed

- (CORE)  [#106](https://projekte.kosit.org/kosit/validator/-/issues/106) The `match` element in `scenarios.xml` is required to have at least one character (per XSD change)
- (BUILD) [#176](https://projekte.kosit.org/kosit/validator/-/issues/176) The Maven Central deployed `pom.xml` properly includes runtime dependencies
- (BUILD) [#175](https://projekte.kosit.org/kosit/validator/-/issues/175) The `.zip` file created from `maven-assembly-plugin` no longer contains the standalone validator, which reduces its size to 50%

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
