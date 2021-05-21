# Validator API

The Validator offers an API which allows you to integrate the Validator in your own applications.

## Dependency Management

Currently, we *do not* deploy to Maven Central or similar. Hence, you need to build and optionally deploy the Validator artifacts to your own shared (or local) repository  (see for example [Maven Documentation](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html)).

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
import de.kosit.validationtool.api.Configuration;
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
        Configuration config = Configuration.load(scenarios.toURI()).build();
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

The `Result` interface has convenience methods to retrieve details about XSD validation errors and Schematron messages and other processing results. See
[Result.java](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/api/Result.java) for details.

Initializing all XML artifacts and XSLT-executables is expensive. The `Check` instance is *threadsafe* and keeps all artifacts. Therefore,
we recommend the re-use of a `Check` instance.

Beside the validator's configuration the only input are instances of [Input](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/api/Input.java)
which can be created by various methods of the [InputFactory](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/api/InputFactory.java).
The [InputFactory](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/api/InputFactory.java)
 calculates a hash sum for each Input which is also written to the Report. _SHA-256_ from the JDK is the default algorithm.
It can be changed using other `read`-methods of [InputFactory](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/api/InputFactory.java).

The main interface [Check.java](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/api/Check.java)
allows using a batch interface (processing list of [Inputs](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/api/Input.java)).
However, there is no parallel processing implemented at the moment.

## Accept Recommendation and Accept Match

A tri-state object [AcceptRecommendation](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/api/AcceptRecommendation.java)
can be retrieved from the [Result](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/api/Result.java) using `getAcceptRecommendation()`.

The three defined states are:

1. `ACCEPTABLE` i.e. the recommendation is to accept input based on the evaluation of the overall validation.
1. `REJECT` i.e. the recommendation is to reject input based on the evaluation of the overall validation.
1. `UNDEFINED` i.e. the evaluation of the overall validation could not be computed (overall processing is incomplete)

The accept recommendation is based on either:

1. Schema and Schematron validation results
1. or on _acceptMatch_ configuration of the scenario (see below)

### Accept match in scenario configuration

For your own configuration you can add an `acceptMatch` element in each scenario. It can contain an XPATH expression over your own defined `Report` to compute a boolean value. An XPATH expression evaluating to true will lead to an `ACCEPTABLE` and otherwise to a `REJECT` recommendation.

This allows to have control over what validation result is to be considered _acceptable_ for your own application context. E.g. you can overrule Schematron validation errors with _acceptMatch_ configuration and consider certain errors as _acceptable_. Nevertheless you can *not* overrule schema errors with accept match.

## Building scenario configurations with the Builder API

Instead of pre-configured [scenario files](configurations.md) it is possible to create a validator configuration using a builder API. A valid configuration consists of the following:

* at least one valid scenario configuration, this includes
  * a valid match configuration to identify/activate this scenario
  * a valid XML schema configuration
  * a valid report transformation configuration
  * valid schematron validation configurations (optional)
  * a valid accept match configuration to compute acceptance information (optional)
* a valid fallback scenario configuration

A simple configuration looks like this:

```java
import static de.kosit.validationtool.config.ConfigurationBuilder.*;
import de.kosit.validationtool.api.Configuration;
import java.net.URI;
import java.nio.file.Path;

public class MyValidator {

 public static void main(String[] args) {
    Configuration config = Configuration.create().name("myconfiguration")
                          .with(scenario("firstScenario")
                                          .match("//myNode")
                                          .validate(schema("Sample Schema").schemaLocation(URI.create("simple.xsd")))
                                          .validate(schematron("my rules").source("myRules.xsl"))
                                          .with(report("my report").source("report.xsl")))
                          .with(fallback().name("default-report").source("fallback.xsl"))
                          .useRepository(Paths.get("/opt/myrepository"))
                          .build();
    Check validator = new DefaultCheck(config);
    // .. run your checks
 }
}
```

The build API provides various methods to configure your scenarios and the validation process.

It is also possible to provide runtime artifacts like `XsltExecutable`, `XPathExecutable` or `Schema` to configure the validator.
This gives you complete control over loading these artifacts.

---
**Note:** Creating these objects requires usage of the same instance of the saxon `Processor` as used during validation later. Therefore, you need to supply a custom `ResolvingConfigurationStrategy` or use the internal one to create these objects. See below.

---

## Configure XML Security and Resolving

When using XML related technologies you are supposed to handle certain security issues properly. The KoSIT validator pursues a rather strict strategy. The default configuration:

* disables DTD validation completely
* allows loading/resolving only from a configured local content repository (a specific folder)
* tries to prevent known XML security issues (see [OWASP XML_Security_Cheat_Sheet.html](https://cheatsheetseries.owasp.org/cheatsheets/XML_Security_Cheat_Sheet.html))

However, you can configure certain aspects related to resolving and security yourself. The validator uses a single interface for accessing or creating the necessary XML API objects like `SchemaFactory`, `Validator`,`URIResolver` or `Processor`:  [ResolvingConfigurationStrategy.java](https://github.com/itplr-kosit/validator/tree/master/src/main/java/de/kosit/validationtool/api/ResolvingConfigurationStrategy.java)

There are 3 implementations available out of the box:

1. [StrictRelativeResolvingStrategy.java](https://github.com/itplr-kosit/validator/tree/master/src/main/java/de/kosit/validationtool/impl/xml/StrictRelativeResolvingStrategy.java)
which is the **default**, prevents known XML attacks and only allows loading from a specific local repository location
1. [StrictLocalResolvingStrategy.java](https://github.com/itplr-kosit/validator/tree/master/src/main/java/de/kosit/validationtool/impl/xml/StrictLocalResolvingStrategy.java)
which opens the first strategy to load resources from local locations
1. [RemoteResolvingStrategy.java](https://github.com/itplr-kosit/validator/tree/master/src/main/java/de/kosit/validationtool/impl/xml/RemoteResolvingStrategy.java)
which further opens the second to load resources also from remote locations via http and https

You can configure usage of one of these implementations using the `ResolvingMode` via

````java
Conifuguration config = Configuration.load(URI.create("myscenarios.xml"))
                            .resolvingMode(ResolvingMode.STRICT_LOCAL)
                            .build();
````

If you decide to implement your own strategy, you can configure this via:

````java
Conifuguration config = Configuration.load(URI.create("myscenarios.xml"))
                            .resolvingStrategy(new MyCustomResolvingConfigurationStrategy())
                            .build();
````

---

:warning: **Attention:** If you decide to implement a custom strategy you need to handle XML security risks on your own. Please make sure, that you prevent XXE and other kind of attacks. Consider using [BaseResolvingStrategy.java](https://github.com/itplr-kosit/validator/tree/master/src/main/java/de/kosit/validationtool/impl/xml/BaseResolvingStrategy.java) and the protected methods within to disable certain features.

---
