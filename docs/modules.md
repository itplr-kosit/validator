# Module Overview

The KoSIT XML Validator is divided into several modules to allow a clear separation of responsibilities.

## Module Structure

- **api**: Contains all public interfaces and model classes.
- **schematron**: Contains the technical validation engine (XSD and Schematron), without scenario handling and without XVRL generation.
- **cvr**: Contains the CVR profile - the XSD of the extension vocabulary and the Schematron of the profile constraints.
- **core**: Contains the core logic of the validation (scenario selection, XVRL generation, acceptance recommendation).
- **server**: A REST API implementation based on Quarkus.
- **client**: A lightweight Java client for using the REST API.
- **cli**: Command Line Interface for using the validator via the console.

## API Module (`validator-api`)

The API module provides the interfaces necessary for integrating the validator into your own applications. It is designed to be the only dependency needed for clients.

### Important Interfaces

- `Check`: Main interface for performing validations.
- `Configuration`: Configuration of the validator (scenarios, repositories).
- `Result`: Result of a validation.
- `Input`: Abstraction of the document to be checked.

### Generated Model Classes

The `validator-api` module includes several model classes that are automatically generated from XSD definitions (found in `src/main/model/xsd`) during the build process. These represent the core data structures for validation and reporting:

- **Scenarios** (`org.kosit.validator.model.scenarios`): Classes representing the validation scenarios (from `scenarios.xsd`).
- **XVRL** (`org.kosit.validator.model.xvrl`): Full implementation of the XML Validation Report Language (from `xvrl-1.0.xsd`).
- **SVRL**: Support for Schematron Validation Report Language (from `svrl-kosit.xsd`).
- **Assertions** (`org.kosit.validator.cmd.assertions`): Model for defining validation assertions (from `assertions.xsd`).
- **General Models** (`org.kosit.validator.model`): General data structures like XML syntax errors (from `model.xsd`).

## Schematron Module (`validator-schematron`)

The schematron module is the technical validation engine. It validates a document against XML Schema and Schematron and
reports the findings as detections. It knows nothing about scenarios and produces no XVRL - Schematron results are bound
via SVRL only, so the module depends on `validator-svrl` (and through it on `validator-jaxb`, `validator-base` and
`validator-conformatron`), Saxon and SchXslt, and on nothing else.

It carries the canonical pipeline steps that need no scenario configuration:

| Step | Action |
|------|--------|
| 2 `PARSE_DOCUMENT` | `org.kosit.cvr.action.parsedoc.xml.ParseXmlAction` |
| 5 `RETRIEVE_ARTIFACTS` | `org.kosit.cvr.action.RetrieveArtifactsAction` |
| 6 `PREPARE_RULES` | `org.kosit.cvr.action.PrepareRulesAction` |
| 7 `APPLY_RULES` | `org.kosit.cvr.action.ApplyRulesAction` |

### Package Roots

- `org.kosit.schematron`: the engine itself - `ContentRepository`, the Schematron compilers and the compiler registry,
  the ad-hoc validation against a single Schematron is the same engine over a scenario assembled at runtime (`ConformanceValidation.adHoc`).
- `org.kosit.schematron.resolve`: the resolving strategies and `ResolvingMode`.
- `org.kosit.schematron.saxon`: `ProcessorProvider`, the secured Saxon processor.
- `org.kosit.cvr`: the Conformance Validation Report model - the `ValidationEngine` contract plus the `action`,
  `model`, `source` and `util` packages implementing `org.conformatron.api`.

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

The module depends on `validator-xvrl` for the XVRL schema and on `validator-schematron` to run its own rules. The
writer that produces CVR (`CvrWriter`) lives in `validator-core`, because it serializes the results of the full
pipeline including scenario detection and selection.

## Core Module (`validator-core`)

The core module contains the actual implementation of the validation logic. It handles:

- Selection of matching scenarios.
- Driving the `validator-schematron` engine for XSD and Schematron validation.
- Generation of XVRL (XML Validation Report Language) and CVR reports.
- The acceptance recommendation.

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
