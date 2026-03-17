# Validator Server

The Validator Server provides the XML validation functionality as a RESTful microservice. It is built using the Quarkus framework and is designed for high performance and easy integration into modern cloud environments.
It enables:

*   **Interoperability**: Validation can be consumed by non-Java clients.
*   **Scalability**: Independent deployment and scaling of the validation in container environments.
*   **Centralized Configuration**: Scenarios and repositories are provided once for all clients of the server.


## Features

- **REST API**: Standardized interface for XML validation.
- **OpenAPI support**: Contract first approach with automatic generation of API documentation and Swagger UI.
- **Health Checks**: Integration with Kubernetes and other orchestrators via `/q/health`.
- **Flexible Configuration**: Support for multiple validation scenarios and resource repositories.

## Starting the Server

The server is packaged as an executable JAR (Uber-JAR).

### Prerequisites

- Java 17 or higher.

### Command Line

To start the server with the default configuration:

```bash
java -jar validator-server-2.0.0-SNAPSHOT-runner.jar
```

Alternatively, you can start it using Maven during development:

```bash
mvn quarkus:dev -pl server
```

The server starts by default on port `8080`.

## REST API Endpoints

### 1. Full Validation (`/validate`)

Performs a complete validation of an XML document according to the matching scenario.

- **Method**: `POST`
- **Consumes**: `application/xml` or `multipart/form-data`
- **Produces**: `application/xml`
- **Response**: Full **XVRL** (XML Validation Report Language) report.

### 2. Compact Validation (`/validateMinimal`)

Returns a simplified validation report, optimized for user interfaces and quick evaluation.

- **Method**: `POST`
- **Consumes**: `application/xml` or `multipart/form-data`
- **Produces**: `application/xml` or `application/json` (negotiated via `Accept` header)
- **Response**: **Compact Report** (XML or JSON).

## Output Formats

### XVRL (Full Report)

The full report follow the XVRL standard. It contains:
- Metadata about the validation (engine, version, time).
- Details about all detection steps.
- Summarized results (error counts).

### Compact Model

The compact model provides a flattened view of the results:
- **`acceptance`**: Recommendation (`ACCEPTABLE`, `REJECT`, `UNDEFINED`).
- **`scenario`**: The identified validation scenario.
- **`layers`**: Specific results for Schema (XSD) and Schematron validations.
- **`error-summary`**: A semicolon-separated string of all error messages.

#### Response Headers

Both endpoints include custom HTTP headers for quick access to the results:
- `X-VALIDATOR-Schema-Valid`: `true` or `false`.
- `X-VALIDATOR-Schematron-Valid`: `true` or `false`.
- `X-VALIDATOR-Acceptance`: The recommendation status.

## Configuration

Configuration is managed via `application.yml` or environment variables.

### Important Properties

| Property | Description | Default |
|----------|-------------|---------|
| `validator.scenarios` | List of scenario files and repositories. | - |
| `quarkus.http.port` | The port the server listens on. | `8080` |
| `validator.logging.json` | Enable JSON logging for ELK/Splunk. | `false` |

Example `application.yml`:

```yaml
validator:
  scenarios:
    - scenarioPath: /path/to/scenarios.xml
      repositoryPath: /path/to/repository
```

## API Documentation

When the server is running, the following documentation is available:
- **Swagger UI**: `http://localhost:8080/docs`
- **OpenAPI Spec**: `http://localhost:8080/q/openapi`
