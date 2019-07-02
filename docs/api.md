# Validator API

The Validator offers an API which allows you to integrate Validator in your own applications.

## Dependency Management

Currently, we *do not* deploy to Maven Central or similar. Hence you need to build and optionally deploy the Validator artifacts to your own shared repository  (see for example [Maven Documentation](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html)).

### Maven

Then you can declare the dependency as follows:

```xml
<dependency>
   <groupId>de.kosit</groupId>
   <artifactId>validationtool</artifactId>
   <version>${validator.version}</version>
</dependency>
```

### Gradle

```js
dependencies {
    compile group: 'de.kosit', name: 'validationtool', version: '1.1.0'
}
```

## Usage

Prerequisite for use is a valid [scenario definition](configurations.md) and the a folder with all necessary artifacts for validation (repository) either on the filesystem or on the classpath.

The following example demonstrates loading scenario.xml and whole configuration from classpath and validating one XML document:

```java
package org.kosit.validator.example;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.CheckConfiguration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.DefaultCheck;
import org.w3c.dom.Document;

public class StandardExample {

    public void run(Path testDocument) throws URISyntaxException {
        // Load scenarios.xml from classpath
        URL scenarios = this.getClass().getClassLoader().getResource("scenarios.xml");
        // Load the rest of the specific Validator configuration from classpath
        CheckConfiguration config = new CheckConfiguration(scenarios.toURI());
        // Use the default validation procedure
        Check validator = new DefaultCheck(config);
        // Validate a single document
        Input document = InputFactory.read(testDocument);
        // Get Result including information about the whole validation
        Result report = validator.checkInput(document);
        System.out.println("Is processing succesful=" + report.isProcessingSuccessful());
        // Get report document if processing was successful
        Document result = null;
        if (report.isProcessingSuccessful()) {
            result = report.getReportDocument();
        }
        // continue processing results...
    }

    public static void main(String[] args) throws Exception {
        // Path of document for validation
        Path testDoc = Paths.get(args[0]);
        StandardExample example = new StandardExample();
        // run example validation
        example.run(testDoc);

    }
}
```

The `Result` interface has more methods to retrieve details about XSD validation errors and Schematron messages.

Initializing all XML artifacts and XSLT-executables is expensive. The `Check` instance is *threadsafe* and keeps all artifacts. Therefore, we recommend the re-use of an `Check` instance.

* Batch use is serial and *not parallel*

The only input `de.kosit.validationtool.api.Input` which can be created by various methods of `de.kosit.validationtool.api.InputFactory`.
The `InputFactory` calculates a hash sum for each Input which is also written to the Report. _SHA-256_ from the JDK is the default algorithm. It can be changed using the `read`-methods of `InputFactory`.
