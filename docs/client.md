# Validator Client

The `validator-client` module provides a lightweight Java client for interacting with the **Validator Server**. It is based on the **MicroProfile REST Client**, generated from the OpenAPI contract of the server, and handles the two-step protocol of the server for you.

## Features

- **The protocol of the server in one call**: `POST /api/validation` creates a validation run (`201 Created`), `GET /api/validation/result/{id}` fetches its report - the client offers both steps and a `validate` combining them.
- **Typed API**: The report comes back as `XvrlReports`, the data model of the CVR (the Conformance Validation Report of the validator, an XVRL profile) - or as a raw file.
- **MicroProfile Integration**: Easy integration into any Quarkus or MicroProfile application.

## Maven Dependency

To use the client in your Maven project, add the following dependency:

```xml
<dependency>
    <groupId>org.kosit</groupId>
    <artifactId>validator-client</artifactId>
    <version>2.0.0-SNAPSHOT</version>
</dependency>
```

## Integration into a Java/Maven Application

The client is designed for seamless use in a CDI/Quarkus environment but can also be configured manually.

### 1. CDI / Quarkus Integration (Recommended)

In a Quarkus or CDI application, the `ValidationClient` can be injected directly.

#### Configuration (`application.properties`)

```properties
# Base URL of the Validator Server
quarkus.rest-client.validator.url=http://localhost:8080
```

#### Usage

```java
import org.kosit.validator.client.ValidationClient;
import org.kosit.xvrl.model.XvrlReports;
import jakarta.inject.Inject;
import java.io.File;

public class MyService {

    @Inject
    ValidationClient client;

    public void validateDocument(File xmlFile) {
        // creates the run and fetches its report
        XvrlReports report = client.validate(xmlFile);

        System.out.println("Errors: " + report.getAllErrors().size());
    }
}
```

### 2. Manual Setup

If you are not using CDI, you can instantiate the client manually using the MicroProfile Rest Client Builder for the generated `ValidationApi` and pass it to `new ValidationClient(api)`.

## API Overview

The `ValidationClient` follows the two steps of the server protocol and offers them combined:

| Method | Description | Return Type |
| :--- | :--- | :--- |
| `createRun(File)` | Posts the document; the server answers `201 Created`. The status carries the `id` of the run, its `status` (`completed`) and `result`, the reference of the report. | `ValidationRunStatus` |
| `fetchResult(UUID)` | Fetches the report of a run and parses it into the CVR data model. | `XvrlReports` |
| `fetchResultRaw(UUID)` | Fetches the report of a run as a temporary `File`. Useful if the report should be saved or processed further manually. | `File` |
| `validate(File)` | `createRun` followed by `fetchResult`. | `XvrlReports` |
| `validateRaw(File)` | `createRun` followed by `fetchResultRaw`. | `File` |
| `createAdHocRun(File, File)` | Posts the document and a Schematron (`.sch` or precompiled `.xsl`) to `/api/validation/adhoc`: the document is validated against that rule set alone, no scenario configuration of the server involved. | `ValidationRunStatus` |
| `createAdHocRun(File, List<File>)` | The same against a set of artifacts — XML Schemas (`.xsd`), Schematrons (`.sch`), precompiled Schematron XSLTs (`.xsl`) — applied in this order as one scenario. The files travel as one ZIP repository under their names, so includes between them resolve. | `ValidationRunStatus` |
| `createAdHocRun(File, File, List<String>)` | The same against a ZIP you built yourself: its entries keep their paths (imports and includes resolve), the list names the entries to apply. | `ValidationRunStatus` |
| `validateAdHoc(File, File)`, `validateAdHoc(File, List<File>)` | `createAdHocRun` followed by `fetchResult`. | `XvrlReports` |

The ad hoc operation is not driven through the generated `AdHocApi`: the generator sends every part under a fixed file name and turns the repeatable `resource` part into a text part, so `ValidationClient` uses the hand-written `AdHocMultipartApi` over a `ClientMultipartForm` instead. It posts a set of files as a repository ZIP rather than as several `resource` parts, because the REST client groups parts of the same name into a nested `multipart/mixed` body that the server does not unwrap.

The server keeps a result for a limited time only (10 minutes and 500 runs by default, see the [server documentation](server.md)). Fetching an unknown or expired run answers `404 Not Found`, which the generated REST client raises as a `jakarta.ws.rs.WebApplicationException`.

### Working with the Results

The report is a CVR: an XVRL report following the profile of the validator. The client hands it over as `XvrlReports` from the module `xvrl`:

```java
XvrlReports report = client.validate(xmlFile);

// the detections of all reports with severity error
List<String> allErrors = report.getAllErrors();
if (allErrors.isEmpty()) {
    System.out.println("The document is valid!");
}
```

The verdict of the run - `ACCEPT`, `REJECT` or `EVALUATE_FURTHER` - is the decision of the last step report of the CVR (`DECISION_RECOMMENDATION`); see the CVR documentation for the structure of the report.

## Supported Output Formats

The server answers with the CVR as XML (`application/xml`); the client parses it with `XvrlConverter` or hands the XML over as file. There is no compact or JSON variant of the report in 2.0.
