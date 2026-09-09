# Validator API

The Validator offers an API which allows you to integrate the Validator in your own applications.

## Dependency Management

Currently, we *do not* deploy to Maven Central or similar. Hence, you need to build and optionally deploy the Validator artifacts to your own shared (or local) repository  (see for example [Maven Documentation](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html)).

### Maven

Then you can declare the dependency as follows:

```xml
<dependency>
   <groupId>org.kosit</groupId>
   <artifactId>validator-core</artifactId>
   <version>${validator.version}</version>
</dependency>
```

### Gradle

```js
dependencies {
    compile group: 'org.kosit', name: 'validator-core', version: '2.0.0-SNAPSHOT'
}
```

Hint: prior to v1.5.1 the group ID was `de.kosit` and the artifact ID was `validationtool`. Up to 1.6 the library was the single artifact `validator`; since 2.0 it is the module `validator-core` (see [modules](modules.md)).

## Usage

Prerequisite for use is a valid [scenario definition](configurations.md) and the a folder with all necessary artifacts for validation (repository) either on the filesystem or on the classpath.

The validation is done by a `ValidationEngine` - the contract of the validator since 2.0 (ADR-008). `ConformanceValidation` is the engine over a scenario configuration: it runs the canonical pipeline of the conformatron API from parsing the document up to the decision recommendation and answers with a `ConformanceValidationResult`. The following example demonstrates loading scenario.xml and whole configuration from classpath and validating one XML document:

```java
package org.kosit.validator.docs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.conformatron.api.model.source.CTReadResource;
import org.kosit.base.io.ResourceHelper;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.ConformanceValidation;
import org.kosit.validator.impl.TestEngineInformation;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kost.validator.api.saxon.ProcessorProvider;

/**
 * Example code that is used in the docs/api.md file: load a configuration, build the engine, validate a document, read
 * the verdict and write the report.
 */
public class StandardExample {

    public void run(final Path testDocument) throws URISyntaxException, IOException {
        // Load scenarios.xml from classpath
        final URL scenarios = this.getClass().getClassLoader().getResource("examples/simple/scenarios-with-relative-paths.xml");
        // Load the rest of the specific Validator configuration from classpath
        final ScenarioSet config = ScenarioSet.load(scenarios.toURI()).build(ProcessorProvider.getProcessor());
        // The engine over that configuration - the canonical pipeline, steps 2 to 9
        final ValidationEngine<ConformanceValidationResult> validator = new ConformanceValidation(new TestEngineInformation(),
                ProcessorProvider.getProcessor(), config);
        // Temporary file helper
        try ( ResourceHelper resHelper = new ResourceHelper() ) {
            // Validate a single document
            final CTReadResource document = ReadResource.of(Resource.of(testDocument), resHelper);
            // The result: the verdict of step 9, and the run behind it
            final ConformanceValidationResult result = validator.validate(document);
            System.out.println("Completed=" + result.isCompleted() + " decision=" + result.getDecision() + " - " + result.getRationale());
            // The report is a CVR - also for a cancelled run, which yields a partial report
            try ( OutputStream out = Files.newOutputStream(testDocument.resolveSibling(testDocument.getFileName() + "-cvr.xml")) ) {
                result.writeCvr(out);
            }
            // continue processing results...
        }
    }

    public static void main(final String[] args) throws Exception {
        // Use e.g. "test-data/src/main/resources/examples/simple/input/foo.xml"
        if (args.length == 0) {
            throw new IllegalStateException("Provide a test document filename on the commandline");
        }
        // Path of document for validation
        final Path testDoc = Paths.get(args[0]);
        final StandardExample example = new StandardExample();
        // run example validation
        example.run(testDoc);
    }
}
```

`TestEngineInformation` stands for your own implementation of `EngineInformation`: it names the application that ran the validation in the report.

The `ConformanceValidationResult` has convenience methods to retrieve details about the run:

* `getDecision()` and `getRationale()` - the verdict of the decision recommendation and the text explaining it (see below)
* `isConformant()` - whether the conformance targets of the selected scenario are met
* `isCompleted()` and `getCancelledAt()` - whether the pipeline ran to the end, or the step at which it was cancelled (e.g. because the document is not well formed or matched no scenario)
* `getSelectedScenarioName()`, `getFindingsByRuleSet()`, `getAllDetections()` and `getProcessingErrors()` - the details behind the verdict
* `toCvr()` and `writeCvr(OutputStream)` - the report, a CVR (Conformance Validation Report, the XVRL profile of the validator). A cancelled run yields a partial report

A `ScenarioSet` is the result of loading a configuration: its scenarios plus the identity of the configuration (name, author, date, where it was read from). The engine works on the scenarios alone; it can be built over one or more `ScenarioSet`s or directly over a `List<Scenario>`, and every `Scenario` carries its own artifact repository. Loading validates the scenarios.xml and compiles the match expressions, nothing else: the validation artifacts are resolved and compiled by the pipeline on first use (steps 5 and 6) and cached in the repository of the scenario, so a missing or broken artifact is a finding in the report of the document rather than an exception at start. Compiling is expensive, therefore we recommend to create one `ConformanceValidation` instance per configuration and re-use it.

Beside the validator's configuration the only input is a `CTReadResource` of the conformatron API, created through `ReadResource` - from a file (`ReadResource.of(Resource.of(path), resourceHelper)`) or from bytes already in memory (`ReadResource.inMemory(Resource.of(name, bytes))`). The report carries a digest of the document. The engine validates one document per call; there is no batch interface.

## The decision

The verdict of a run is a `CTDecision` of the conformatron API, retrieved from the `ConformanceValidationResult` using `getDecision()`. The three defined states are:

1. `ACCEPT` i.e. the recommendation is to accept input based on the evaluation of the overall validation.
1. `REJECT` i.e. the recommendation is to reject input based on the evaluation of the overall validation.
1. `EVALUATE_FURTHER` i.e. the evaluation of the overall validation could not be computed (overall processing is incomplete, the run was cancelled)

The decision is computed from the XML Schema and Schematron findings of the selected scenario, the way 1.6 computed its accept recommendation: schema errors and Schematron errors lead to `REJECT`, warnings and information do not. The `acceptMatch` of a scenario configuration is *not* evaluated by the 2.0 engine - it was an XPath over the rendered 1.6 report, which does not exist in the canonical pipeline (ADR-004 follow-up). The CLI maps the decision onto the familiar words of its result table (`ACCEPTABLE`, `REJECT`, `UNDEFINED`).

## Scenarios that apply unconditionally

A scenario without `match` applies to every document, in addition to the scenario detected by its match: step 4 selects the one scenario matched by expression (several are still `scenario-ambiguous`) plus every unconditional scenario, steps 5 to 7 run the rule sets of all of them - each from its own repository - and step 8 states the conformance per scenario, so the decision of step 9 covers all of them (one non-conformant target rejects). `ConformanceValidationResult.getAppliedScenarioNames()` lists them, the selected one first. A scenario the caller names (`validate(document, scenarioName)`) is applied alone.

## Ad hoc validation of a single Schematron

"Run this Schematron against this document" is the same engine over one scenario assembled at runtime:

```java
final ValidationEngine<ConformanceValidationResult> engine = ConformanceValidation.adHoc(engineInformation,
        ProcessorProvider.getProcessor(), Paths.get("rules/my-rules.sch").toUri(), false);
final ConformanceValidationResult result = engine.validate(document);
```

The scenario (`Scenario.adHoc`) has no match, no XML schema and exactly one rule set; the directory of the Schematron is its repository, so relative includes resolve there and nowhere else. The result and the report are the same as for a configured scenario. A Schematron URI no repository can be derived from - a relative URI, or one inside an archive without `resolveInArchive` - is an `IllegalArgumentException` when the engine is built, not a finding.

## Building scenario configurations with the Builder API

Instead of pre-configured [scenario files](configurations.md) it is possible to create a validator configuration using a builder API. A configuration consists of at least one scenario, and a scenario consists of

* a match configuration to identify/activate this scenario - a scenario without one applies unconditionally, which is the shape of a scenario assembled at runtime from validation artifacts ("run this Schematron against this file")
* an XML schema configuration (optional)
* schematron validation configurations (optional), each naming the Schematron processor of its rule set (`compiler`) and the severity overrides of its rules (`customLevel`)
* an `acceptMatch` expression (optional, kept in the declaration; 2.0 does not evaluate it)

There is no fallback scenario: a document no scenario applies to is a cancelled run with a partial report (`no-scenario-matched`), not a run along a fallback.

A simple configuration looks like this:

```java
package org.kosit.validator.docs;

import static org.kosit.validator.config.ConfigurationBuilder.scenario;
import static org.kosit.validator.config.ConfigurationBuilder.schema;
import static org.kosit.validator.config.ConfigurationBuilder.schematron;

import java.net.URI;
import java.nio.file.Paths;

import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.ConformanceValidation;
import org.kosit.validator.impl.TestEngineInformation;
import org.kosit.validator.impl.conformatron.ConformanceValidationResult;
import org.kost.validator.api.saxon.ProcessorProvider;

/**
 * Example code that is used in the docs/api.md file: a configuration assembled in Java, and the engine built over it.
 */
public class MyValidator {

    @SuppressWarnings("unused")
    public static void main(final String[] args) {
        final ScenarioSet config = ScenarioSet.create().name("myconfiguration")
                .with(scenario("firstScenario").match("//myNode").validate(schema("Sample Schema").schemaLocation(URI.create("simple.xsd")))
                        .validate(schematron("my rules").source("myRules.xsl").compiler("schxslt")))
                .useRepository(Paths.get("/opt/myrepository")).build(ProcessorProvider.getProcessor());
        // the engine: configuration is a construction concern, validate(...) takes nothing but the document
        final ValidationEngine<ConformanceValidationResult> validator = new ConformanceValidation(new TestEngineInformation(),
                ProcessorProvider.getProcessor(), config);
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

However, you can configure certain aspects related to resolving and security yourself. The validator uses a single interface for accessing or creating the necessary XML API objects like `SchemaFactory`, `Validator`,`URIResolver` or `Processor`: `org.kosit.schematron.resolve.ResolvingConfigurationStrategy` (module `schematron`).

There are 3 implementations available out of the box:

1. `StrictRelativeResolvingStrategy`
which is the **default**, prevents known XML attacks and only allows loading from a specific local repository location
1. `StrictLocalResolvingStrategy`
which opens the first strategy to load resources from local locations
1. `RemoteResolvingStrategy`
which further opens the second to load resources also from remote locations via http and https

You can configure usage of one of these implementations using the `ResolvingMode` via

```java
final ScenarioSet config = ScenarioSet.load(URI.create("myscenarios.xml")).setResolvingMode(ResolvingMode.STRICT_LOCAL)
        .build(ProcessorProvider.getProcessor());
```

If you decide to implement your own strategy, you can configure this via:

```java
final ScenarioSet config = ScenarioSet.load(URI.create("myscenarios.xml"))
        .setResolvingStrategy(new MyCustomResolvingConfigurationStrategy()).build(ProcessorProvider.getProcessor());
```

---

:warning: **Attention:** If you decide to implement a custom strategy you need to handle XML security risks on your own. Please make sure, that you prevent XXE and other kind of attacks. Consider using `BaseResolvingStrategy` and the protected methods within to disable certain features.

---
