# General Architecture

The validator itself is just an engine which executes validation according to a certain configuration (
see [configuration documentation](configurations.md)).

The validator takes a scenario.xml and the configured directory with all artifacts necessary for validation (scenario repository). Then it
performs the validation and generates a report in XML format. This report is then the VInput to an XSLT provided by the configuration.

## System Overview

The validator system is designed with a modular structure, where the core validation logic is decoupled from the access interfaces.

```mermaid
graph TD
    User([User / Application]) -->|HTTP REST| Server[Validator Server]
    User -->|CLI Call| CLI[Validator CLI]
    User -->|Java API| App[Embedding Java Application]

    Server -->|uses| Core[Validator Core Engine]
    CLI -->|uses| Core
    App -->|uses| Core

    subgraph "Validator Core (`validator-core`)"
        Core -->|selects| Scenario[Szenario Definition]
        Core -->|executes| XSD[XSD Validation]
        Core -->|executes| Schematron[Schematron Validation]
        Core -->|creates| XVRL[XVRL Report]
    end
```

## Separation of concerns

* The purpose of the validator is to only report if an XML instance is valid or not
* A configuration can provide an XSLT which takes the validator report and generates an own report
  * This report may choose to conclude acceptance of the XML instance or not

The validator reports valid/invalid, a configuration reports acceptance/rejection!

## Core Validation Process

The internal validation process within the Core Engine (the default is defined in `DefaultCheck`) follows these steps:

```mermaid

sequenceDiagram
  participant e as Validator
  participant c as Configuration
  e->>+c: create ScenarioRepository
  c->>-e: is available
  e->>e: parse XML
  e->>e: select scenario
  e->>e: validate XSD
  e->>e: validate Schematron
  e->>e: create Validator Report
  e->>+c: execute configuration report generator
  e->>e: Compute Recommendation

```

1. *parse XML*:

    Is the XML instance valid in the basic sense. If not, validation is stopped and the validator report is returned with status *invalid*.
2. *select scenario*:

    The configuration must have a defined scenario which matches the XML instance (it is an XPATH expression). If no scenario matches, validation is stopped and the validator report is returned with status *invalid*.
3. *validate XML-Schema*:

    The XML instance must be valid according to the configured XSD. If not, validation is stopped and the validator report is returned with status *invalid*.
4. *validate Schematron*
5. *create Validator Report*:

    All results are aggregated into the validation report:
    * Depending on the configuration in the scenario, if there is a single *error* or *warning* the report will have status *invalid*, otherwise the status will be *valid*.
6. *execute configuration report generator*

    The Validator will search for the XSLT as configured in scenario.xml and execute it with the Validator Report as VInput
7. compute Recommendation

    In case a scenario contains an `acceptMatch` element with an XPATH expression, this expression will be executed.

    In case the XPATH returns `true`, the recommendation will be set to `ACCEPT` else to `REJECT`. In case no such XPATH is defined it is `UNDEFINED`.
