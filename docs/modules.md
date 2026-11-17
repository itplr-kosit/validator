# Module Overview

The KoSIT XML Validator is divided into modules with one responsibility each. The modules form layers: a module only
depends on the modules below it, and Maven refuses a build in which a lower module reaches up. The layering is
therefore not a convention but enforced by the build.

## Module Structure

In build order, with the modules each one depends on (compile scope; every artifact ID is `validator-` plus the
directory name):

| Module | Responsibility | Depends on |
|--------|----------------|------------|
| `conformatron` | The Conformatron API: interfaces only, `org.conformatron.api` | - |
| `base` | Generic helpers for objects, strings, errors, URIs and XML; the DVR coordinates | `conformatron` |
| `conformatron-model` | The data types of the Conformatron API as the validator implements them - detections, resources, rule sets, validation artifacts, conformance statements. No behaviour | `conformatron`, `base` |
| `xml` | Reading XML: the secured Saxon processor, the parser of the step `parse-document`, the handlers that collect parser and schema errors as detections | `jaxb`, `conformatron-model` |
| `jaxb` | Generic JAXB read/write helpers and adapters | `base`, `conformatron` |
| `svrl` | JAXB binding of SVRL, the Schematron Validation Report Language | `jaxb` |
| `schematron` | The technical validation engine: XML Schema and Schematron, compilers, content repositories and resolving | `svrl`, `xml` |
| `xvrl` | JAXB binding of XVRL, the XML Validation Report Language | `jaxb` |
| `cvr` | The CVR profile of XVRL - the format of the report, nothing that writes it | `xvrl`, `schematron` |
| `conformatron-impl` | The canonical pipeline steps, the result of a run and the writer of the report | `conformatron-model`, `xml`, `schematron`, `svrl`, `xvrl`, `cvr` |
| `scenario` | The scenario configuration: JAXB bindings of version 2 and 3, the version independent model, the migration | `jaxb` |
| `core` | Configuration and engine: loads a scenario configuration, detects and selects the scenario, runs the pipeline | `conformatron-impl`, `scenario`, `schematron`, `xml` |
| `cli` | The command line application | `core` |
| `server` | The REST service, based on Quarkus | `core` |
| `client` | A Java client of the REST service | `core`, `xvrl`, `svrl` |
| `distribution` | The release archive | `cli`, `server` |

`test-data` holds the scenario configurations, schemas and sample instances the tests of all modules share; it is a
test dependency only.

### Where a class belongs

- A type of the Conformatron API without behaviour goes into `conformatron-model`.
- A pipeline step, the result of a run, and writing the report go into `conformatron-impl`.
- Whatever needs the scenario configuration - loading it, the steps `detect-scenarios` and `select-scenario`, the
  engine over a configuration - goes into `core`.
- A format - a schema, a Schematron, a JAXB binding - is a module of its own: `svrl`, `xvrl`, `cvr`, `scenario`.

`conformatron`, `conformatron-model` and `conformatron-impl` together with the format modules know nothing about the
KoSIT scenario configuration. That is the part another application can reuse to implement the Conformatron API without
adopting the configuration format of the validator.

### The pipeline steps

| Step | Class | Module |
|------|-------|--------|
| `parse-document` | `org.kosit.conformatron.action.parsedoc.xml.ParseXmlAction` | `conformatron-impl` |
| `detect-scenarios` | `org.kosit.validator.impl.action.DetectScenariosAction` | `core` |
| `select-scenario` | `org.kosit.validator.impl.action.SelectScenarioAction` | `core` |
| `retrieve-artifacts` | `org.kosit.conformatron.action.RetrieveArtifactsAction` | `conformatron-impl` |
| `prepare-rules` | `org.kosit.conformatron.action.PrepareRulesAction` | `conformatron-impl` |
| `apply-rules` | `org.kosit.conformatron.action.ApplyRulesAction` | `conformatron-impl` |
| `compute-conformance` | `org.kosit.conformatron.action.ComputeConformanceAction` | `conformatron-impl` |
| `decision-recommendation` | `org.kosit.conformatron.action.DecisionRecommendationAction` | `conformatron-impl` |

The results of `detect-scenarios` and `select-scenario` (`DetectScenariosResult`, `SelectScenarioResult`) are in
`conformatron-impl` all the same: the result of a run is assembled from the results of all steps, and it must not
depend on where the action that produced one of them lives.

### Package roots

Every package lives in exactly one module, so a package name tells where a class is.

| Module | Packages |
|--------|----------|
| `conformatron-model` | `org.kosit.conformatron.detection`, `.source`, `.rule`, `.validation`, `.conformance` |
| `conformatron-impl` | `org.kosit.conformatron.action` (the steps), `.pipeline` (`PipelineResults`, `ConformanceValidationResult`), `.report` (`CvrWriter`) |
| `xml` | `org.kosit.xml`, `org.kosit.xml.saxon`, `org.kosit.xml.error` |
| `schematron` | `org.kosit.schematron` and below |
| `cvr` | `org.kosit.cvr.report` |
| `scenario` | `org.kosit.validator.scenario` and below |
| `core` | `org.kosit.validator.api`, `org.kosit.validator.config`, `org.kosit.validator.impl` and below |

## Schematron Module (`validator-schematron`)

The schematron module is the technical validation engine. It validates a document against XML Schema and Schematron and
reports the findings as detections. It knows nothing about scenarios and produces no XVRL - Schematron results are bound
via SVRL only, so the module depends on `validator-svrl` and `validator-xml`, on Saxon and SchXslt, and on nothing else.

- `org.kosit.schematron`: `ContentRepository`, the Schematron compilers and the compiler registry.
- `org.kosit.schematron.compiler`: the compilers themselves.
- `org.kosit.schematron.resolve`: the resolving strategies and `ResolvingMode`.
- `org.kosit.schematron.util`: turning SVRL into detections.

## CVR Module (`validator-cvr`)

CVR - **Conformance Validation Report** - is the XVRL profile of the validator, and this module is that profile. It
ships two artifacts and the class that applies them, `org.kosit.cvr.report.CvrProfile`:

| Artifact | Classpath | Answers |
|----------|-----------|---------|
| `cvr-1.0.xsd` | `/xsd/cvr-1.0.xsd` | what the CVR extension vocabulary is and what its value spaces are |
| `cvr-1.0.sch` | `/sch/cvr-1.0.sch` | whether a report satisfies the profile |

A report is checked in two steps, because the two questions are different. *Is it XVRL?* is answered by
`xvrl-1.0.xsd` from `validator-xvrl`. *Is it CVR?* is answered by the profile Schematron: the canonical pipeline
steps a report is built from, the extension vocabulary it may use, and the internal consistency the producer
guarantees - a digest that matches the detections it summarises, a cancelled run that does not claim conformance, a
rule set identity only on the step that applied it.

Those constraints are deliberately not in the XSD: XVRL admits foreign attributes with `processContents="skip"`, so a
schema cannot tighten them, and most of the constraints relate one part of the report to another. `cvr-1.0.xsd` therefore
types the vocabulary for tooling and for the reader, and `cvr-1.0.sch` is what an actual report is held to.

The module is the format and nothing else. The writer that produces CVR, `CvrWriter`, lives in
`validator-conformatron-impl` next to the steps whose results it serializes, and depends on this module - not the other
way round.

## Core Module (`validator-core`)

The core module is the validator over a scenario configuration:

- loading and checking a scenario configuration of version 2 or 3 (`org.kosit.validator.config`),
- the steps `detect-scenarios` and `select-scenario` (`org.kosit.validator.impl.action`),
- the engine `ConformanceValidation`, which runs the pipeline of `validator-conformatron-impl` for the selected
  scenario, and the identity it states in the report (`EngineInformation`).

## Server Module (`validator-server`)

The server module provides the validator as a microservice, leveraging the Quarkus framework. For more details, see [Server Documentation](server.md).

### REST Interfaces

The server exposes a REST API. An OpenAPI specification is provided by default.

- `POST /api/validation`: Runs the validation of the posted document and answers `201 Created` with the run id and the `Location` of the result (RFC 9110, §9.3.3).
- `GET /api/validation/result/{id}`: Returns the report of that run - the CVR, the Conformance Validation Report of the validator.

### Configuration

Configuration is managed via Quarkus properties (e.g., `application.properties`). This includes paths to scenario configurations and repository locations.

## Client Module (`validator-client`)

The client module provides a lightweight Java client (`ValidationClient`) to interact with the validator server. For more details, see [Client Documentation](client.md).

The client handles:

- HTTP communication with the server.
- Marshalling and unmarshalling of XML and JSON responses.
- Translation of technical errors into Java exceptions.

## CLI Module (`validator-cli`)

The CLI allows for using the validator from the command line. It is suitable for batch processing and integration into shell scripts. For more details, see [CLI Documentation](cli.md).
