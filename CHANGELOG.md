# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 2.0.0 - work in progress

### Changed

- (CORE) A match expression that can not be evaluated over the document cancels step 3 with the new detection `scenario-match-error` instead of counting as a non-match. `Scenario.matches(XdmNode)` propagates the `SaxonApiException` rather than answering `false`, and `DetectScenariosAction` reports it. Before, "the engine could not tell whether this scenario applies" was recorded as "this scenario does not apply": the document was then validated against the next scenario, or against none, and nothing in the report said so - a wrong verdict with no trace. All scenarios are evaluated before the run is cancelled, so the report names every broken expression at once. The expression itself is still compiled when the configuration is loaded, so this covers dynamic errors only, e.g. a cast that fails on the content of a particular document
- (API) `scenarios-v2.xsd`: `scenario` and `resource` take an optional `id` attribute whose value is a Maven-like coordinate `groupId:artifactId:version[:classifier]` (schema-checked shape: three parts plus an optional classifier, each of letters, digits, `_`, `-`, `.`, at most 64 characters, following the DVR coordinate rules; deliberately no `xs:ID`, because a coordinate is no NCName). It states which artifact in which exact version a configuration was written for, while `location` stays the place in the artifact repository
- (CORE) The report identifies by the declared id where a configuration declares one: `cvr:scenario-id` (steps 3, 4) and `cvr:target-id` (step 8) carry the id of the scenario instead of its name, `cvr:artifact-id` (steps 5, 6) the id of the resource instead of its location. A configuration that declares no id is reported by name and location exactly as before, ad hoc runs included, and a scenario with an id whose resources have none is reported by id and location. `location/@href` keeps pointing at the artifact next to the digest either way, and the `name` stays the display name in every message text. `ScenarioMatch.getScenarioID()` and `ScenarioRuleSetReference.reportedId(...)` are the two places that decide it. Detection and resolution are untouched (`match`, `location`); a caller that names the scenario itself may hand over its id or its name. Resolving an artifact through its id without a location needs a repository that serves artifacts by coordinate and stays a matter of the scenario configuration of version 2
- (API) The validation of a document goes through `org.kosit.validator.api.ValidationEngine<R>` now (ADR-008). `org.kosit.validator.impl.ConformanceValidation` is the engine over a `VConfiguration`: it runs the canonical pipeline of the conformatron API from `PARSE_DOCUMENT` to `DECISION_RECOMMENDATION` and answers `validate(CTReadResource)` with a `ConformanceValidationResult` carrying the decision (`CTDecision`), its rationale, the run state (`isCompleted()`, `getCancelledAt()`), the findings and the CVR (`toCvr()`, `writeCvr(OutputStream)`). `SchematronValidation` is the ad hoc engine against a single Schematron. The decision is computed from the schema and Schematron findings the way 1.6 computed its accept recommendation; a scenario's `acceptMatch` is not evaluated
- (API) `scenarios-v2.xsd`: `validateWithSchematron` takes the `customLevel` overrides of its rule set, so the severity overrides are declared with the rules they belong to instead of below `createReport`. `createReport` and with it `createReport/customLevel` are removed from the schema (`CreateReportType`), so `ScenarioSeverityOverrides` reads the rule sets only. `match` and `validateWithXmlSchema` are optional: a scenario without `match` applies unconditionally, a scenario without schema validates with Schematron alone
- (CORE) `validateWithSchematron/@compiler` is honoured by step 6: a `.sch` is compiled with the declared processor instead of the default of `PrepareRulesAction`, and for a precompiled `.xsl` the declared processor is recorded as the transpiler of the rule set, so the CVR states how the rule set was built. The declaration travels with the artifact reference as `ScenarioRuleSetReference`
- (CORE) An empty `acceptMatch` element is a placeholder and no longer fails the configuration load with an XPath error; 2.0 does not evaluate `acceptMatch`
- (API) `VConfiguration` is replaced by `org.kosit.validator.api.ScenarioSet`: the scenarios of one configuration plus the identity of that configuration (name, author, date, definition file). `ScenarioSet.load(URI[, URI])` returns the `ConfigurationLoader`, `ScenarioSet.create()` the `ConfigurationBuilder`, both build a `ScenarioSet`. `ConformanceValidation` is constructed over a `List<Scenario>` or over one or more `ScenarioSet`s; every `Scenario` carries its artifact repository (`getRepository()`), so scenarios of several configurations run in one engine and the artifacts of the selected scenario are retrieved from and compiled in its repository
- (API) `Scenario` is the scenario as the engine runs it: declaration (`ScenarioType`), repository, compiled match expression, definition file and the artifacts handed over compiled. It no longer holds compiled schema, Schematron or report executables, and the `ConfigurationLoader` no longer resolves or compiles the validation artifacts - steps 5 and 6 do, so a missing or broken artifact is a finding in the report of the document instead of an exception while loading. A scenario without `match` applies unconditionally and is a candidate for every document (`Scenario.isUnconditional()`); `Scenario.matches(XdmNode)` evaluates the match. The loader without an explicit repository resolves relative to the directory of the scenarios.xml, also inside an archive
- (API) The builder API keeps its shape - `ConfigurationBuilder`, `ScenarioBuilder`, `SchemaBuilder`, `SchematronBuilder` - without `report(...)` and `fallback()`. Schema and match are optional, `SchematronBuilder` gained `compiler(String)` and `customLevel(ErrorLevelType, String...)`. Artifacts handed over compiled (`schema(Schema)`, `executable(XsltExecutable)`) are passed through by step 5 as `ScenarioRuleSetReference.compiled()` under a synthetic `precompiled:` location; artifacts given by location are checked when the configuration is built and compiled by the pipeline from the repository, where the cache makes that a lookup. A builder without `useRepository` resolves relative to the working directory
- (API) `DetectScenariosAction` is constructed over `List<Scenario>` instead of a `ScenarioRepository`; `withDefinitionFile` is gone because the scenario knows the file it was read from. `ScenarioMatch.of(Scenario, CTParsedValidationSource)` and `userSelected(...)` expose the scenario through `getScenario()`; a scenario that applies unconditionally is reported as `scenario-matched` with the text "applies unconditionally"
- (API) A scenario without `match` applies unconditionally, in addition to the scenario detected by its match (ADR `validator-scenario-failure-semantics`, point 5): `SelectScenarioAction` selects the one scenario matched by expression - several are still `scenario-ambiguous`, unconditional candidates do not count - plus every unconditional candidate, and its result carries `applied()` next to `selected()` (the matched one, or the first unconditional one). `ConformanceValidation` retrieves and prepares the artifacts of every applied scenario in its own repository, applies all rule sets with the overrides of all applied scenarios (`ScenarioSeverityOverrides.ofAll`) and computes one conformance target per applied scenario, so the decision of step 9 covers all of them. `ConformanceValidationResult.getAppliedScenarioNames()` lists them. A scenario named by the caller is applied alone
- (API) The ad hoc validation is the same engine over a scenario assembled at runtime from a set of artifacts: `Scenario.adHoc(Processor, List<URI>, URI repository, boolean)` builds one unconditional scenario whose `.xsd` artifacts are the XML Schema and whose `.sch`/`.xsl`/`.xslt` artifacts are its rule sets, in the given order, named after the artifacts; the repository is the given root or the common directory of the artifacts, and an artifact outside it, or of another extension, is an `IllegalArgumentException`. `Scenario.adHoc(Processor, URI, boolean)` and `ConformanceValidation.adHoc(EngineInformation, Processor, URI, boolean)` remain as the single-Schematron form, `ConformanceValidation.adHoc(EngineInformation, Processor, List<URI>, URI, boolean)` is the engine over a set. The result and the report are a `ConformanceValidationResult` and a CVR like for every other run
- (CLI) `-S`/`--artifact <file>` (alias `--schematron`) runs an ad hoc validation: every document is validated against the given artifacts - XML Schemas (`.xsd`), Schematrons (`.sch`, compiled) and precompiled Schematron XSLTs (`.xsl`, run as is) - as one scenario instead of a scenario configuration. `-S` may be given several times; `-s` is required unless `-S` is given, the two exclude each other; `-r` names the repository root of the artifacts (default: their common directory), and an artifact outside it or of unknown kind is a configuration error. The result table shows `-` in a rule set column when the completed run applied no rule set of that kind
- (SERVER) `POST /api/validation/adhoc` creates an ad hoc validation run against a set of posted artifacts and answers like `POST /api/validation`. Multipart parts: `document`; `resource` (repeatable) for single files - `.xsd`, `.sch` or `.xsl` by file name, a part without usable name is typed by its root element and named `resource-N`; `repository`, a ZIP whose entries keep their paths so that `xs:import`, `sch:include` and `xsl:import` resolve (256 MB unpacked at most), with `artifact` (repeatable) naming the entries to apply; `schematron` of the first version is still accepted as one resource. The scenario of the CVR is named after the artifacts. Without document or artifact, for a resource of unknown kind or a missing ZIP entry the answer is a `400`. The operation is served by the hand-written `AdHocValidationController` over the raw multipart form, because the generated `AdHocResource` loses file names and collapses the repeatable part
- (CLIENT) `ValidationClient.createAdHocRun(File, File)`, `createAdHocRun(File, List<File>)` (the files as one ZIP repository under their names), `createAdHocRun(File, File zip, List<String> entries)` and `validateAdHoc(File, File | List<File>)` for the ad hoc operation, over the hand-written `AdHocMultipartApi` (`ClientMultipartForm`) instead of the generated `AdHocApi`, which drops file names and cannot send a repeatable file part
- (CLI) The CLI drives the `ConformanceValidation` engine instead of the legacy check chain. The text output - result table, memory statistics, exit code - is unchanged, and the written report is the CVR
- (SERVER) The REST API follows RFC 9110 §9.3.3: `POST /api/validation` runs the validation of the posted document and answers `201 Created` with the `Location` of the result and a `ValidationRunStatus` (`id`, `status`, `result`); `GET /api/validation/result/{id}` returns the CVR of that run or `404 Not Found` once it is unknown or expired. Results are kept in memory for a configurable time (`validator.results.retention`, default `PT10M`) and count (`validator.results.capacity`, default `500`). A document that is not XML is a completed run with a cancelled CVR, not a `400`
- (CLIENT) `ValidationClient` follows the two steps of the server protocol: `createRun(File)` returns the `ValidationRunStatus`, `fetchResult(UUID)` and `fetchResultRaw(UUID)` return the CVR as `XvrlReports` or as file, `validate(File)` and `validateRaw(File)` combine both steps
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
- (API) The foreign attributes of an XVRL object keep their insertion order, so the order of the CVR attributes in the compact report is deterministic. Previously they were emitted in `HashMap` order
- (API) `CompactXvrlReport.addSchematronViolation` converts the rich text children (`dir`, `span`, `emph`) of an SVRL `failed-assert` text to their string representation, instead of copying the SVRL JAXB objects into the XVRL message content
- (API) `CompactXvrlReport.addSchemaReference` takes the mandatory `schematypens` as the third parameter, because the XSD requires that attribute on every `schema` element
- (BUILD) Extracted the test data shared by the tests of `core`, `cli`, `server` and `client` into the new submodule `test-data`, built as the first module and depending on no other module. It was previously copied into every one of those modules, where 89 of the 152 files were redundant copies and four of them had silently drifted apart
- (BUILD) The test helpers of `validator-core` (`TestHelper`, `TestObjectFactory`, `TestProcessBuilder`, `TestScenarioBuilder`, `TestConfigurationFactory`) are attached as a `test-jar` and reused by `validator-cli`, which previously kept its own, partly outdated copies of all five
- (BUILD) Test data is located through `org.kosit.validator.testdata.TestData` (`file`, `dir` and `missing`) instead of the working directory relative `Paths.get("src/test/resources")`. A resource that is not on the classpath now fails immediately instead of yielding a URI that points nowhere
- (CORE) `TestHelper.JAR_REPOSITORY` became the method `TestHelper.getJarRepository()`, so that the packaged test scenario artifact is only resolved when a test actually uses it
- (BUILD) Test data that has to live inside an archive is located through `TestData.inArchive(String)`, which returns a `jar:` URI. Whether the `test-data` module itself is a jar on the classpath depends on the Maven goal - `mvn verify` packages it, `mvn test` and the IDE hand out the plain output directory - so the test data is packed into a temporary jar on demand. The tests covering the archive code path behave the same in every build mode now
- (BUILD) The previously external dependency `org.conformatron:conformatron-api` was integrated as the new submodule `conformatron`, built as the second module and depending on no other module. It is available as `org.kosit:validator-conformatron` and the Java package `org.conformatron.api` is unchanged
- (API) `RelativeUriResolver` and the resolving strategies do not resolve within an archive base URI like `jar:file:/some.jar!/repository/` any more, unless it is enabled explicitly through the new `RelativeUriResolver(URI, boolean)`, `RelativeUriResolver.resolve(URI, URI, boolean)`, `StrictRelativeResolvingStrategy(boolean)`, `StrictLocalResolvingStrategy(boolean)` or `RemoteResolvingStrategy(boolean)`. Reaching into an archive makes the content of a file addressable that is a single opaque resource to everybody who only looks at the base URI, so it is a decision of the caller now. A scenario configuration shipped as a jar therefore needs `VConfiguration.load(...).setResolvingStrategy(new StrictRelativeResolvingStrategy(true))` or `VConfiguration.create().resolvingStrategy(new StrictRelativeResolvingStrategy(true))`; the `ResolvingMode` constants keep the default and do not resolve into archives
- (BUILD) Extracted the Schematron and XML Schema validation engine into the new submodule `schematron`, built after `svrl` and before `xvrl`, and depending on `svrl` (and through it on `jaxb`, `base` and `conformatron`) only. It carries the canonical pipeline steps 2 (`PARSE_DOCUMENT`), 5 (`RETRIEVE_ARTIFACTS`), 6 (`PREPARE_RULES`) and 7 (`APPLY_RULES`), the Schematron compilers, the content repository, the resolving strategies and the secured Saxon processor. It contains no scenario handling and no XVRL generation - Schematron results are bound via SVRL only. `validator-core` depends on it
- (API) The classes of the new submodule live in two package roots: `org.kosit.schematron` for the engine (`ContentRepository`, `SchematronCompiler`, `SchXsltCompiler`, `SchXslt2Compiler`, `IsoSchematronCompiler`, `SchematronCompilerRegistry`, `CollectingErrorEventHandler`, `SchematronValidation`), `org.kosit.schematron.resolve` for the resolving strategies (previously `org.kosit.validator.xml.resolve`, plus `ResolvingConfigurationStrategy` and `ResolvingMode`), `org.kosit.schematron.saxon` for `ProcessorProvider`, and `org.kosit.cvr` for the Conformance Validation Report model - `org.kosit.cvr.ValidationEngine`, `org.kosit.cvr.action`, `org.kosit.cvr.action.parsedoc`, `org.kosit.cvr.action.parsedoc.xml`, `org.kosit.cvr.model`, `org.kosit.cvr.source` and `org.kosit.cvr.util` (all previously below `org.kosit.validator.impl.conformatron`)
- (API) `ContentRepository` is scenario agnostic now. The methods that read a scenario configuration (`createSchema(ScenarioType)`, `createReportTransformations`, `createTransformation`, `createMatchExecutable`, `createAccepptExecutable`, `createSchematronTransformations`, `createSchematronTransformation` and `createIdentityTransformation`) moved to the new `org.kosit.validator.impl.ScenarioArtifacts` in `validator-core` and take the repository as their first parameter
- (API) `SeverityOverrides` is a plain detection code to severity map now and is created through `SeverityOverrides.of(Map)`. Reading the overrides out of a scenario configuration moved to the new `org.kosit.validator.impl.conformatron.model.ScenarioSeverityOverrides` in `validator-core`, which offers the previous `fromConfiguration(ScenarioType)` and `of(CTScenarioMatch)`
- (BUILD) The shared test helper `TestHelper` moved to `org.kosit.schematron` in the `test-jar` of `validator-schematron`; `serialize(List<BusinessReport>)` and `parseDocument(...)` stayed behind in `org.kosit.validator.impl.TestObjectFactory`, because they need the legacy task pipeline of `validator-core`
- (BUILD) Added the new submodule `cvr`, built after `xvrl` and depending on `xvrl` and `schematron`. It carries the CVR profile - CVR is the Conformance Validation Report, the XVRL profile of the validator - as `xsd/cvr-1.0.xsd` for the extension vocabulary and `sch/cvr-1.0.sch` for the profile constraints, applied through `org.kosit.cvr.report.CvrProfile`
- (API) `CvrProfile.validate(ReadResource)` checks a report three times and returns a `DetailedValidationResult`: it parses the report, validates it against `cvr-1.0.xsd` plus `xvrl-1.0.xsd` for the structure, and applies the profile Schematron for everything that makes an XVRL report a CVR report - the canonical step reports and their order, the extension vocabulary, and the internal consistency of digest, detections and run status. Each check only runs when the preceding one found no error, so the corresponding detection list is `null` otherwise. The constraints are not in the XSD because XVRL admits foreign attributes with `processContents="skip"` and because most of them relate one part of the report to another
- (API) The profile Schematron is transpiled to XSLT once per JVM and applied through `SchXslt2Compiler`; a failure of the rule engine itself is reported as a `rule-engine-error` detection instead of an exception
- (API) The report is named CVR - Conformance Validation Report - throughout; the abbreviation `CVRL` is gone. The extension vocabulary of the CVR profile is the namespace `urn:conformatron:cvr:draft` with the prefix `cvr`, replacing `urn:conformatron:cvrl:draft` with the prefix `cvrl`, `CvrlWriter` is now `CvrWriter` and `CvrlWriter.NS_CVRL` is now `CvrWriter.NS_CVR`. The `hash` element and its `algorithm` attribute are serialized with the `cvr` prefix instead of `cvrl`, the report fixtures moved from `/cvrl/` to `/cvr/` on the test classpath, and the e2e material of `XRechnungE2ERunner` is written as `<instance>-cvr.xml` instead of `<instance>-cvrl.xml`
- (CORE) The CVR tests validate the generated reports against the profile instead of only against the XVRL schema
- (API) `org.kosit.conformatron.detection.Detection` implements the complete `CTDetection` interface now, so `getId()`, `getField()` and `getSummary()` return the values provided instead of always `null`. Following the XVRL data model, instances are created through the static `builder()` methods (`builder()`, `builderError()`, `builderWarning()`, `builderNone()`) and can be derived from an existing instance via `toBuilder()`. The public constructor and the factory method `Detection.of(...)` were removed; `Detection.overridden(...)` remains, keeps the detection time of the base detection and now also carries over its ID, field and summary
- (API) `org.kosit.conformatron.detection.DetectionLocation` is created through `DetectionLocation.builder()` and can be derived from an existing instance via `toBuilder()`, matching the XVRL data model. The two public constructors were removed; the factory methods `of(String)`, `of(String, SAXParseException)` and `ofXPath(String, String)` remain and delegate to the builder. `Builder.location(SAXParseException)` takes line and column number from a parse exception
- (BUILD) The generated e2e material of `DetectScenariosExamplesTest`, `CvrUnhappyPathTest` and `XRechnungE2ERunner` keeps only the local part of every URL: `LocalUris` reduces the `jar:` URL of the packaged test data to the path inside the archive, the URL of an unpacked one to the path below `target/classes` and a URL below the checkout to the checkout relative path, right before the file is written. Where the test data resolved to differs between two machines and between two Maven goals, so every run rewrote all of the committed reports with its own absolute paths - like `FixedTimestamps` for the serialization time, this keeps a diff of them to real changes

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
- (API) Added `org.kosit.base.uri.UriHelper` to the submodule `base`, offering `resolve(URI, URI[, boolean])`, `resolve(URI, String[, boolean])`, `relativize(URI, URI)`, `normalize(URI)`, `getPath(URI)`, `getHierarchicalUri(URI)` and `isArchiveUri(URI)`. Their `java.net.URI` counterparts return the passed argument unchanged as soon as one side is an opaque URI, which every URI addressing something inside an archive is - so resolving `simple.xsd` against `jar:file:/some.jar!/repository/` yielded `simple.xsd`, and normalizing or relativizing such a URI did nothing at all. `UriHelper` performs the operation on the URL the archive URI wraps, whose path carries the entry path, and wraps the result again. Resolving *into* an archive is opt in through the `resolveInArchive` parameter and defaults to `false`, because it makes the content of a file addressable that is a single opaque resource to everybody who only looks at the base URI
- (BUILD) Added the XSD `scenarios-v3.xsd` for the scenario configuration version 3 in the new XML namespace `urn:kosit:validator:scenario:3` to the submodule `scenario`, plus the JAXB model generated from it in the package `org.kosit.validator.scenario.v3`. Compared to version 2 it adds DVR coordinates to the scenarios and to every resource, distinguishes `scenarioXml` from `scenarioPdf`, splits the single `date` into `lastModificationDate` and `validFromDate`, makes `frameworkVersion` and the resource `location` optional and allows a description to be plain text. Like version 2 it declares the `customLevel` overrides with the rule set they belong to and has no `createReport`
- (API) Added `org.kosit.validator.scenario.v3.Scenario3Converter` to read and write the scenario configuration version 3, analogous to `Scenario2Converter`
- (API) Added the version independent scenario data model in the new package `org.kosit.validator.scenario.generic`, covering the union of the requirements of both scenario configuration versions: `ScenarioConfiguration`, `Scenario`, `ScenarioCoordinate`, `ScenarioDescription`, `ScenarioDescriptionBlock`, `ScenarioNamespace`, `ScenarioResource`, `ScenarioSchematron`, `ScenarioCustomErrorLevel`, `ScenarioRequirement` and the enums `EScenarioKind`, `EScenarioErrorLevel` and `EScenarioDescriptionBlockKind`
- (API) Added `org.kosit.validator.scenario.v2.Scenario2Mapper` and `org.kosit.validator.scenario.v3.Scenario3Mapper` converting between the respective JAXB model and the generic model. Their `fromGeneric(ScenarioConfiguration, List<SimpleError>)` reports every dropped value and every value the target version requires but that is not set
- (API) Added `JaxbHelper.getAsLocalDate(XMLGregorianCalendar)` and `JaxbHelper.getAsXmlDate(LocalDate)` to convert between `xs:date` and `java.time.LocalDate`

### Removed

- (API) Removed the legacy check chain, which the `ValidationEngine` replaces: `VCheck`, `VResult`, `DefaultVCheck`, `DefaultResult`, the package `org.kosit.validator.impl.tasks` (`CheckTask`, `DocumentParseTask`, `CreateDocumentIdentificationTask`, `ScenarioSelectionTask`, `SchemaValidationTask`, `SchematronValidationTask`, `CreateReportsTask`, `ComputeAcceptanceTask`, `BusinessReport`), `ProcessStepResult`, `ActionMetadata`, `XvrlHelper`, `XvrlSerializer` and the models `ValidationResultsSchematron`, `ValidationResultsXmlSchema`, `DocumentHash` and `DocumentIdentificationType`
- (API) Removed `SchematronValidation` and `org.kosit.cvr.report.AdHocValidationResult`: the ad hoc validation is `ConformanceValidation.adHoc(...)`, and its result is a `ConformanceValidationResult` with a CVR (ADR-008 revised: one engine, two assemblies)
- (API) Removed the configuration model of 1.x: `VConfiguration`, `DefaultConfiguration`, `ConfigurationKeys`, `ScenarioRepository` including the fallback scenario (a document no scenario applies to cancels the run with `no-scenario-matched`, see ADR `validator-scenario-failure-semantics`), `ScenarioArtifacts`, `Scenario.VTransformation`, `ReportBuilder`, `FallbackBuilder` and `ConfigurationLoader.addParameter`
- (API) Removed the compact report `org.kosit.validator.api.xvrl.compact` (`CompactXvrlReport`, `CompactXvrlReportSummary`, `ValidatorEngineInformation`, `AcceptRecommendation`) together with the `compactvrl` namespace. The output of 2.0 is the CVR; `AcceptRecommendation` survives only as the display vocabulary of the CLI result table (`org.kosit.validator.cmd`, package private)
- (SERVER) Removed the previous `validate` endpoints with their compact XML and JSON responses, the `X-VALIDATOR-*` response headers and the `Content-Disposition` header, as well as the DTOs `CompactValidationResultsDto`, `CompactResultDto`, `CompactResultLayerDto`, `CompactViolationDto`, `ValidatorEngineDto` and the `CompactXvrlReportSummaryMapper`
- (CLIENT) Removed the `validateMinimal*` and `*WithMetadata` methods, `ValidationResponse` and the request and response filters of the client (`ValidationRequestFilter`, `ValidationRequestConfig`, `ValidationContentTypeCaptureFilter`, `ValidationResponseMetadata`)
- (BUILD) Removed the test only dependency `de.kosit.validationtool:packaged-test-scenarios`, which was checked into the repository as a binary jar in `core/libs` and served through the `project.local` file repository. Its content was unwrapped into `validator-test-data` (`packaged/` and `simple/packaged/`), so the binary, the file repository declaration and the dependency are gone
- (CORE) Removed the unused `TestHelper.LARGE_XML`, which resolved to the `pom.xml` of whichever module happened to be running
- (API) Removed the XVRL mix-in types `org.kosit.xvrl.api.BaseDetection`, `org.kosit.xvrl.api.BaseMessage` and `org.kosit.xvrl.api.BaseReportSummary` as well as the base classes `org.kosit.xvrl.impl.AbstractXvrlReport` and `org.kosit.xvrl.impl.AbstractXvrlReportSummary`. They only existed to graft convenience methods onto the generated JAXB classes, which the data model provides natively: `BaseDetection.getAllMessages()` is `XvrlDetection.getAllMessageStrings()`, `BaseMessage.getMessageStrings()` is `AbstractXvrlContentObject.getContentStrings()` and `getAllErrors()` stayed on `XvrlReport` and `XvrlReports`. The `inheritance` JAXB plugin is no longer used for the XVRL model
- (BUILD) The classes of the new submodule `api` are covered by unit tests: `XmlDetectionTest`, `XmlParserTest`, `CollectingSaxErrorHandlerTest`, `CollectingSaxonErrorReporterTest`, `CollectingErrorEventHandlerTest`, `ProcessorProviderTest`, `XdmNodeValidationSourceTest` and `DetailedValidationResultTest`. None of the modules these classes came from had a test dedicated to them, so there was nothing to move along with them
- (BUILD) The previously untested classes of the submodule `base` are covered by unit tests: `ObjectHelperTest`, `StreamHelperTest`, `ResourceHelperTest`, `ResourceHelperExtensionTest`, `VersionTest`, `DVRGlobalCoordinateSettingsTest`, `DVRPseudoVersionTest`, `LoggingSaxErrorHandlerTest`, `SchemaResolverTest` and `XmlReaderWrapperTest`. `StringHelperTest`, `XmlHelperTest` and `DVRPseudoVersionRegistryTest` were extended to cover the remaining public methods of `StringHelper` (exploding, int parsing, hashing) and `XmlHelper` (the hardened parser, schema, StAX and transformer factories)

### Fixed

- (BUILD) The `validator-api` module no longer emits stub `ObjectFactory` classes for the `svrl` and `scenario` packages, which shadowed the complete ones of the respective modules on the classpath
- (CLI) `Format.mergeCodes` no longer discards the plain formatting codes like `BOLD` or `UNDERLINE` and no longer emits background colors twice. `Code.isColor()` is `true` for the background colors as well, so the previous filter combination only ever selected the `BG_*` codes
- (API) The `xml:id` of all XVRL data model types is converted to a valid `xs:NCName` now. Values like `Report for eInvoice` previously made the marshalling of the XVRL report fail with `cvc-datatype-valid.1.2.1`
- (CORE) `CreateReportsTask` puts the name of the report resource into the `code` attribute of the created detection instead of into `xml:id`. As `xml:id` is of type `xs:ID` it must be unique per document, which a report resource name is not - two `createReport` elements using the same resource name made the marshalling of the XVRL report fail with `cvc-id.2`
- (API) `XvrlJaxbCreator` always writes the `metadata` element of `reports` and of `report` as well as the `digest` element of `report`, because the XSD requires them. An `XvrlReport` that was not passed through `XvrlHelper.finalizeAndBuild` previously made the marshalling fail with `cvc-complex-type.2.4.b`
- (API) The schema references of the compact report carry the required `schematypens` attribute now - `http://www.w3.org/2001/XMLSchema` for XML Schema and `http://purl.oclc.org/dsdl/schematron` for Schematron. Serializing a compact report to XML previously failed with `cvc-complex-type.4`, so `POST /api/validate/minimal` with `Accept: application/xml` answered with HTTP 500
- (CORE) `ConversionServiceTest.testInvalid` and `testIllformed` asserted on a file that did not exist: `TestHelper.Invalid.ROOT` pointed at `examples/invaid/` and `SCENARIOS_ILLFORMED` at `scenarios-illformed.xml` while the file was named `scenarios-illforned.xml`. Both tests passed for the wrong reason
- (CORE) `TestScenarioBuilder` puts the name of the artifact relative to the repository into the scenario `location`, as a real scenario configuration does, instead of the absolute raw path of its URI
- (CLI) `CommandlineApplicationTest.testValidDirectoryInput` derives the number of expected documents from the input directory instead of hard coding it, so adding a sample to the shared test data cannot break it
- (CORE) A `scenarioPath` or `repositoryPath` configured for the server is normalized before use. `Path.toUri()` keeps a `..` segment, so for such a path the repository base URI no longer matched the resolved artifact URI and every schema reference failed with "is not within the configured repository"
- (CORE) `ArtifactResolver` can resolve artifact references against a repository that lives inside an archive, as in `jar:file:/some.jar!/repository/`. Resolution went through `URI.resolve`, which hands the bare reference back for such an opaque base, so every artifact of a packaged repository was rejected with `artifact-access-denied`. Resolution and normalization go through `UriHelper` now, and the confinement compares the URL the archive URI wraps, so that it covers the archive and the entry path within it in one go. It has to be enabled explicitly through the new `ArtifactResolver(URI, boolean)` and `RetrieveArtifactsAction(URI, boolean)`; the existing single argument constructors keep the previous behaviour and reject every reference into an archive repository, including an absolute one already in archive form
- (CORE) `RelativeUriResolver.resolve` uses `UriHelper.resolve` instead of its own jar handling, which prefixed an already absolute `jar:` reference a second time. Loading `jar:file:/some.jar!/repository/report.xsl` from the repository `jar:file:/some.jar!/repository/` produced `jar:jar:file:/...` and failed with "Can not resolve ... in repository ...". Resolving within an archive is opt in there as well now, see the Changed section
- (CORE) `SchematronValidation` derives the repository of an ad hoc run from the schematron URI through `UriHelper`. For a schematron inside a jar, `schematron.resolve(".")` returned `.` and the run failed with "repository must be an absolute URI". A schematron inside an archive has to be announced through the new `SchematronValidation(Processor, URI, boolean)` or `validate(CTReadResource, URI, boolean)`, which also decide the archive permission of the `ContentRepository` of that run
- (CORE) `SchematronValidation.validate` reports a schematron whose repository can not be derived - a relative URI, or an archive URI without the permission to resolve into it - as an `artifact-access-denied` detection of a failed run, as it does for every other step failure. It previously let an `IllegalArgumentException` escape

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
