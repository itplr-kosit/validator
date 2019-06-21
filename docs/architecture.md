# General Architecture

The validator itself is just an engine which executes validation according to a certain configuration (see [configuration documentation](docs/configurations.md)).

The validator takes a sceanrio.xml and the configured directory with all artifacts necessary for validation (scenario repository). Then it performs 
the validation and generates a report in XML format. This report is then the input to an XSLT provided by the configuration.

## Separation of concerns

* The purpose of the validator is to only report if an XML instance is valid or not
* A configuration can provide an XSLT which takes the validator report and generates an own report
  * This report may choose to conclude acceptance of the XML instance or not

The validator reports valid/invalid, a configuration reports acceptance/rejection!


## General process
The general process is like this: 

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
  e->>-c: return XPATH of acceptance message
  
```
 
1. *XML-Parsing*: Is the XML instance valid in the basic sense. If not, validation is stopped and the validator report is returned with status *invalid*.
2. *Identifikation of applicable scenario*: The configuration must have a defined scenario which matches the XML instance (it is an XPATH expression). If no scenario matches, validation is stopped and the validator report is returned with status *invalid*.
3. *XML-Schema validation*: The XML instance must be valid according to the configured XSD. If not, validation is stopped and the validator report is returned with status *invalid*.
4. *Schematron validation*
5. *Aggregation of validation results*: All results are aggregated into the validation report:
    * In case there is a single *error* or *warning* the report will have status erhält*invalid*, otherwise the status will be *valid*.


