# Validator Server

The Validator Server provides the validation as a RESTful service, built on Quarkus. It runs the same engine as the
CLI — `ConformanceValidation`, the canonical pipeline — and answers with the same report: the Conformance Validation
Report (CVR), the XVRL profile of the validator.

It enables:

* **Interoperability**: validation can be consumed by non-Java clients.
* **Scalability**: independent deployment and scaling in container environments.
* **Centralized configuration**: scenarios and repositories are provided once for all clients.

## Features

- **Resource-oriented REST API**: a validation is created with `POST` and its result fetched with `GET`
  (RFC 9110, section 9.3.3).
- **OpenAPI, contract first**: the API is `META-INF/openapi.yml`; the JAX-RS interface is generated from it.
- **Health checks**: `/q/health/ready` for Kubernetes and other orchestrators.
- **Multiple configurations**: several scenario files, each with its own repository.
- **Ad hoc validation**: a document against a set of posted artifacts — XML Schemas, Schematrons, precompiled
  Schematron XSLTs, as single files or as a ZIP repository — without any scenario configuration
  (`POST /api/validation/adhoc`).

| Operation | Request | Answer |
|---|---|---|
| `POST /api/validation` | the document as body, `application/xml` | `201 Created`, `Location` of the result, run description as JSON |
| `POST /api/validation/adhoc` | `multipart/form-data`: `document` + `resource`… and/or `repository` + `artifact`… | as above; `400` without document or artifact, or for an artifact of unknown kind |
| `GET /api/validation/result/{id}` | — | `200` the CVR (`application/xml`), or `404` when unknown or evicted |

## Starting the Server

The server is packaged as an executable JAR (uber-jar).

### Prerequisites

- Java 25 or higher.

### Command Line

```bash
java -jar validator-server-2.0.0-SNAPSHOT-runner.jar
```

During development:

```bash
mvn quarkus:dev -pl server
```

The server listens on port `8080` by default.

## REST API

### Create a validation run — `POST /api/validation`

Send the document to validate as the request body.

- **Consumes**: `application/xml`
- **Answers**: `201 Created`
    - `Location`: where the result can be fetched
    - body (`application/json`): a description of the run

```json
{
  "id": "0d8f3b6e-7d2a-4d1c-9a0e-5b4f6c2e8a11",
  "status": "completed",
  "result": "http://localhost:8080/api/validation/result/0d8f3b6e-7d2a-4d1c-9a0e-5b4f6c2e8a11"
}
```

Validation runs synchronously today, so every run is `completed` when the response arrives. The `status` field is
there so that a deferred execution can be introduced later without changing the contract.

A document that can not be parsed is **not** a bad request. It creates a run like any other document; the pipeline
cancels at `parse-document`, and the result is the partial CVR that says so (`cvr:status="CANCELLED"`). `400` is
reserved for a malformed request itself, e.g. an empty body.

### Create an ad hoc validation run — `POST /api/validation/adhoc`

Validate a document against a set of posted artifacts instead of the configured scenarios: the same pipeline over one
scenario assembled from them — no match (it applies to the document), the XML Schemas as its schema step, every
Schematron or precompiled Schematron XSLT as one rule set, in the order posted — and the same result resource.

- **Consumes**: `multipart/form-data`
    - `document` (required): the XML document
    - `resource` (repeatable): an artifact as a single file — XML Schema (`.xsd`), Schematron (`.sch`) or precompiled
      Schematron XSLT (`.xsl`). The file name of the part decides the kind; a part without a usable name is typed by its
      root element (`xs:schema`, `sch:schema`, `xsl:stylesheet`) and named `resource-N`. One part per file.
    - `repository`: a ZIP archive that becomes the artifact repository of the run. Its entries keep their paths, so
      `xs:import`, `sch:include` and `xsl:import` between them resolve (limit: 256 MB unpacked).
    - `artifact` (repeatable): the entries of the ZIP to apply as artifacts, in this order.
    - `schematron`: the part of the first version of this operation — still accepted as one `resource`.
- **Answers**: `201 Created`, exactly like `POST /api/validation`; `400` without `document`, without any artifact, for a
  `resource` that is neither XSD, Schematron nor XSLT, for an `artifact` the ZIP does not contain

Both forms may be combined; the repository entries are applied first, then the resources. In the report the scenario is
named after the artifacts (`simple.xsd, simple.sch`, or `with-include.sch`). A rule set that does not compile is not a
`400` — it is a run that cancels at `prepare-rules`, like with a configured scenario.

```bash
# one Schematron
curl -s -i -X POST -F document=@rechnung.xml -F resource=@rules.sch http://localhost:8080/api/validation/adhoc

# schema and rules as single files
curl -s -i -X POST -F document=@rechnung.xml -F resource=@UBL-Invoice-2.1.xsd -F resource=@XRechnung-UBL-validation.xsl \
     http://localhost:8080/api/validation/adhoc

# a modular Schematron: the ZIP carries the included files, "artifact" names the entry to apply
curl -s -i -X POST -F document=@rechnung.xml -F repository=@rules.zip -F artifact=XRechnung-UBL-validation.sch \
     http://localhost:8080/api/validation/adhoc
```

RELAX NG (`.rnc`) is not supported yet.

### Fetch the result — `GET /api/validation/result/{id}`

- **Produces**: `application/xml` — the CVR of the run
- **Answers**: `200` with the report, or `404` if the identifier is unknown or the result has been evicted

The result can be fetched as often as needed while it is kept. It is the report the engine wrote, byte for byte.

### Example

```bash
curl -s -i -X POST -H 'Content-Type: application/xml' --data-binary @rechnung.xml \
     http://localhost:8080/api/validation
```

```
HTTP/1.1 201 Created
Location: http://localhost:8080/api/validation/result/0d8f3b6e-…
Content-Type: application/json

{"id":"0d8f3b6e-…","status":"completed","result":"http://localhost:8080/api/validation/result/0d8f3b6e-…"}
```

```bash
curl -s http://localhost:8080/api/validation/result/0d8f3b6e-… > rechnung-cvr.xml
```

## The report

Every result is a CVR: `xvrl:reports` in the namespace `http://www.xproc.org/ns/xvrl` with the extension namespace
`urn:conformatron:cvr:draft`. One `report` per executed pipeline step, the decision of step 9 last, the source document
embedded. The report validates against `xvrl-1.0.xsd` and against the CVR profile (`cvr-1.0.xsd` + `cvr-1.0.sch`).

Today the server delivers the report in the granularity `full`. The further granularities of the CVR specification
(`compact`, `digest`, `acceptable`) — the same report with less content — are not built yet; how they are requested
(query parameter or path) is decided with them.

## Result retention

Results are kept in memory, per server instance, within two limits:

| Property | Description | Default |
|---|---|---|
| `validator.results.retention` | How long a result stays fetchable after its run (ISO-8601 duration). | `PT10M` |
| `validator.results.capacity` | How many results are kept at most; beyond that the oldest is evicted. | `500` |

A result that has been evicted answers `404`, like one that never existed. Two instances behind a load balancer do not
share results; a deployment that needs that has to route a client to the instance that created its run, or put a
shared store behind `ValidationRunStore`.

## Starting with the XRechnung configuration

The configuration of the comparison corpus (`e2e/comparison/input`) is the one the 150 instances run against.
Started from the reactor root, with Quarkus' syntax for indexed lists:

```bash
java -Dvalidator.scenarios[0].scenarioPath=e2e/comparison/input/scenarios-v2.0-framework2.xml \
     -Dvalidator.scenarios[0].repositoryPath=e2e/comparison/input/repository \
     -jar server/target/validator-server-2.0.0-SNAPSHOT-runner.jar
```

Expect a few seconds more at startup: the eleven scenarios' Schematrons are compiled when the configuration loads.
`/q/health/ready` then reports `configurationCount: 1`.

## Configuration

Configuration is managed via `application.yml` or environment variables.

| Property | Description | Default |
|---|---|---|
| `validator.scenarios` | List of scenario files and repositories. | - |
| `validator.results.retention` | see above | `PT10M` |
| `validator.results.capacity` | see above | `500` |
| `quarkus.http.port` | The port the server listens on. | `8080` |
| `validator.logging.json` | JSON logging for ELK/Splunk. | `false` |

```yaml
validator:
  scenarios:
    - scenarioPath: /path/to/scenarios.xml
      repositoryPath: /path/to/repository
  results:
    retention: PT30M
    capacity: 1000
```

## API documentation

When the server is running:

- **Swagger UI**: `http://localhost:8080/docs`
- **OpenAPI**: `http://localhost:8080/q/openapi`
