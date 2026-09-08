# Validation Configuration

## Scenarios

The core of each validation configuration is the scenarios.xml file. The scenarios.xml itself must be valid according to the [Scenarios XML Schema](//xsd/scenarios.xsd) with the following namespace `http://www.xoev.de/de/validator/framework/2/scenarios`.

Several validation scenarios (`<scenario>` XML Elements) can be described for each configuration.

Each scenario allows to define the matching criterion. It is an XPATH expression which must evaluate to true matched against the test xml candidate. Only then this scenario will apply to the test candidate.

Within a scenario you can define the XML Schema and several Schematrons against which a test xml candidate has to be validated. You can give each a name and define where to find the resources/artifacts for validation.

Each `validateWithSchematron` names the Schematron processor of its rule set in the `compiler` attribute (`schxslt`, `schxslt2` or `iso-schematron`). For a `.sch` this is the processor the validator compiles it with; for a precompiled `.xsl` it states the processor that produced it, and the report names it as the transpiler of the rule set.

A `<createReport>` element - an XSLT over the report of 1.x - may still be declared by configurations shared with 1.x, but 2.0 does not execute it: the report of 2.0 is the CVR. The same holds for `<acceptMatch>`, which 2.0 does not evaluate; configurations written for 2.0 keep the element empty.

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
