# Validation Configuration

## Scenarios

The core of each validation configuration is the scenarios.xml file. The scenarios.xml itself must be valid according to the [Scenarios XML Schema](//xsd/scenarios.xsd) with the following namespace `http://www.xoev.de/de/validator/framework/2/scenarios`.

Several validation scenarios (`<scenario>` XML Elements) can be described for each configuration.

Each scenario allows to define the matching criterion. It is an XPATH expression which must evaluate to true matched against the test xml candidate. Only then this scenario will apply to the test candidate.

Within a scenario you can define the XML Schema and several Schematrons against which a test xml candidate has to be validated. You can give each a name and define where to find the resources/artifacts for validation.

Each `validateWithSchematron` names the Schematron processor of its rule set in the `compiler` attribute (`schxslt`, `schxslt2` or `iso-schematron`). For a `.sch` this is the processor the validator compiles it with; for a precompiled `.xsl` it states the processor that produced it, and the report names it as the transpiler of the rule set.

The `<createReport>` element of 1.x - an XSLT over the report - does not exist in the scenario schema of 2.0: the report of 2.0 is the CVR. `<acceptMatch>` is still allowed but not evaluated; configurations written for 2.0 keep the element empty.

The `<match>` is optional. A scenario without match applies unconditionally, in addition to the scenario detected by its match, and gets its own conformance statement in the report. That is the shape of a scenario assembled at runtime from validation artifacts (ad hoc validation); a declared scenario normally carries a match.

## Validators Report

The Validator's report is defined in [xvrl-1.0.xsd](//xsd/xvrl-1.0.xsd) and contains all errors from all validation steps and some additional information on time of validation, engine used, the scenario which applied and a document identification.

In general all errors will be classified in the following levels:

* *warning*,
* *error*, or
* *fatal error*

### Customization of error levels

In each single scenario each error level can be configured to the following error types

* error
* warning
* information

This can be done by adding `customLevel` elements to the `validateWithSchematron` of the rule set the codes belong to. Configurations written for 1.x declare them below `createReport`; that place is still read, and for a code named in both places the rule set decides.

Here is an example:

```xml
<scenario>
  <name>EN16931 CIUS XRechnung (UBL Invoice)</name>
   ...
  <validateWithSchematron compiler="schxslt">
    <resource>
      <name>Schematron rules for EN16931 (UBL)</name>
      <location>resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl</location>
    </resource>
    <customLevel level="warning">BR-15</customLevel>
  </validateWithSchematron>
</scenario>
```

Here the errors reported by violating the schematron rule `BR-15` are translated from *error* to *warning*.
