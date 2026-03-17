# Validator Client

The `validator-client` module provides a lightweight Java client for interacting with the **Validator Server**. It is based on the **MicroProfile REST Client** and handles all low-level communication and data conversion.

## Features

- **Typed API**: Use Java objects instead of raw XML/JSON.
- **MicroProfile Integration**: Easy integration into any Quarkus or MicroProfile application.
- **Format Negotiation**: Support for both full XVRL (XML) and compact formats (XML/JSON).
- **Error Handling**: Transformation of HTTP errors and validation issues into manageable Java exceptions.

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
import org.kosit.validator.api.compact.CompactXVRLReportSummary;
import jakarta.inject.Inject;
import java.io.File;

public class MyService {

    @Inject
    ValidationClient client;

    public void validateDocument(File xmlFile) {
        // Minimal/Compact validation
        CompactXVRLReportSummary result = client.validateMinimal(xmlFile);
        
        System.out.println("Acceptable: " + result.getAcceptable());
        System.out.println("Scenario: " + result.getReports().get(0).getScenario());
    }
}
```

### 2. Manual Setup

If you are not using CDI, you can instantiate the client manually using the MicroProfile Rest Client Builder (see MicroProfile documentation for specific builder patterns).

## API Overview

The `ValidationClient` offers various methods for validating XML documents. These differ in the level of detail of the result (Full vs. Minimal/Compact) and the return format (Java object vs. raw file).

### Validation Methods

The methods are divided into three categories: Base methods (returning Java objects), Raw methods (returning files), and Metadata methods (returning with HTTP metadata).

#### 1. Base Methods (Java Objects)

These methods unmarshal the server response directly into Java objects from the `validator-api` module.

| Method | Description | Return Type |
| :--- | :--- | :--- |
| `validate(File)` | Performs a full validation and returns the XVRL report. | `XVRLReportSummary` |
| `validateMinimal(File)` | Performs a minimal validation and returns the compact report. | `CompactXVRLReportSummary` |

#### 2. Raw Methods (File Return)

These methods return the server response as a temporary `File`. This is useful if the report should be saved or processed further manually.

| Method | Description | Return Type |
| :--- | :--- | :--- |
| `validateRaw(File)` | Performs a full validation and returns the XVRL report as an XML file. | `File` |
| `validateMinimalRaw(File)` | Performs a minimal validation and returns the compact report as an XML file. | `File` |
| `validateMinimalRawAsJson(File)` | Performs a minimal validation and returns the compact report as a JSON file. | `File` |

#### 3. Methods with Metadata (`ValidationResponse`)

These methods return a `ValidationResponse<T>`, which contains HTTP metadata such as the status code and content type in addition to the actual result (`T`).

| Method | Description | Return Type |
| :--- | :--- | :--- |
| `validateWithMetadata(File)` | Full validation with metadata. | `ValidationResponse<XVRLReportSummary>` |
| `validateMinimalWithMetadata(File)` | Minimal validation with metadata. | `ValidationResponse<CompactXVRLReportSummary>` |
| `validateRawWithMetadata(File)` | Full validation (Raw XML) with metadata. | `ValidationResponse<File>` |
| `validateMinimalRawWithMetadata(File)` | Minimal validation (Raw XML) with metadata. | `ValidationResponse<File>` |
| `validateMinimalRawAsJsonWithMetadata(File)` | Minimal validation (Raw JSON) with metadata. | `ValidationResponse<File>` |

### The `ValidationResponse<T>` Class

The `ValidationResponse` encapsulates the result and provides access to HTTP information:
- `getBody()`: The actual result (e.g., `XVRLReportSummary` or `File`).
- `getStatusCode()`: The HTTP status code of the server response.
- `getContentType()`: The `MediaType` of the response.

### Working with the Results

The client uses the models from `validator-api`, which allow easy access to the validation status:

```java
XVRLReportSummary summary = client.validate(xmlFile);

// Check if there are errors in the full report
List<String> allErrors = summary.getAllErrors();
if (allErrors.isEmpty()) {
    System.out.println("The document is valid!");
}
```
```java
CompactXVRLReportSummary summary = client.validateMinimal(xmlFile);
CompactXVRLReport report = summary.getReports().get(0);

if (report.isAcceptable()) {
    System.out.println("The document is acceptable!");
}
if (report.isSchemaValid() && report.isSchematronValid()) {
        System.out.println("The document is valid!");
}
```


## Supported Output Formats

The client handles the unmarshalling of server responses:
- **XML (XVRL)**: Automatically converted into JAXB objects (`XVRLReportSummary` or `CompactXVRLReportSummary`).
- **JSON (Compact)**: Supported via raw methods or by manual processing of the JSON file.
- **Compact Report**: Encapsulated in `CompactXVRLReportSummary` to allow easier access to attributes. It facilitates the xvrl-schema as xml marshalling format. Elements are used as a subset arranged in a more compact form. Ist is enriched by custom attributes using the `compactvrl` namespace-refix.
