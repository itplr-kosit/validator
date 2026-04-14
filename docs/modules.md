# Module Overview

The KoSIT XML Validator is divided into several modules to allow a clear separation of responsibilities.

## Module Structure

- **api**: Contains all public interfaces and model classes.
- **core**: Contains the core logic of the validation (XSD, Schematron, XVRL generation).
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

### Compact Model (`org.kosit.validator.api.compact`)

For use cases requiring a simplified view of the complex XVRL results, the `compact` package offers wrapper classes:

- `CompactXVRLReportSummary`: Summary of multiple validation results.
- `CompactXVRLReport`: Detailed but essential information about an individual report. It provides direct access to:
  - Selected scenario
  - Acceptance recommendation (`AcceptRecommendation`)
  - Error summary
  - Validation layers (Schema, Schematron)
- `ValidatorEngineInformation`: Provides name and version of the used validator engine.

## Core Module (`validator-core`)

The core module contains the actual implementation of the validation logic. It handles:

- Parsing of XML documents.
- Selection of matching scenarios.
- Execution of XSD and Schematron validations.
- Generation of XVRL (XML Validation Report Language) reports.

## Server Module (`validator-server`)

The server module provides the validator as a microservice, leveraging the Quarkus framework. For more details, see [Server Documentation](server.md).

### REST Interfaces

The server exposes a REST API. An OpenAPI specification is provided by default.

- `/validate`: Performs a full validation and returns the complete XVRL report.
- `/validateMinimal`: Returns the compact model, which is much easier to process for typical UI applications.

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
