# Validation Configuration

## Scenarios

The core of each validation configuration is the scenarios.xml file. The scenarios.xml itself must be valid according to the [Scenarios XML Schema](/src/main/model/xsd/scenarios.xsd) with the following namespace `http://www.xoev.de/de/validator/framework/1/scenarios`.

Several validation scenarios (`<scenario>` XML Elements) can be described for each configuration.

Each scenario allows to define the matching criterion. It is an XPATH expression which must evaluate to true matched against the test xml candidate. Only then this scenario will apply to the test candidate.

Within a scenario you can define the XML Schema and several Schematrons against which a test xml candidate has to be validated. You can give each a name and define where to find the resources/artifacts for validation.

Lastly, you can define in an `<createReport>` element a XSLT transformation which takes the validator's report in order to create an own styled report.

If no scenario matches you can also define a XSLT transformation in `<noScenarioReport>` element.

## Validators Report

The Validator's report is defined in [createReportInput.xsd](/src/main/model/xsd/createReportInput.xsd) and contains all errors from all validation steps and some additional information on time of validation, engine used, the scenario which applied and a document identification.

In general all errors will be classified in the following levels:

* *warning*,
* *error*, or
* *fatal error*

### Customization of error levels

In each single scenario each error level can be configured to the following error types

* error
* warning
* information

This can be done by adding `customLevel` elements in
`createReport`.

Here is an example:

```xml
<scenario>
  <name>EN16931 CIUS XRechnung (UBL Invoice)</name>
   ...
  <createReport>
    <resource>
      <name>Prüfbericht für XRechnung</name>
       <location>resources/xrechnung/xrechnung-report.xsl</location>
    </resource>
    <customLevel level="warning">BR-15</customLevel>
  </createReport>
</scenario>
```

Here the errors reported by violating the schematron rule `BR-15` are translated from *error* to *warning*.
