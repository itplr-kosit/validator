# config-cases/integration/cii-cii-sr-465-negative-test.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (CII)
- **Dokument-Hash**: `SHA-512=16344486cfbaca59eee75541b8027f7952338fc23a64b66e2a5feb4529abbe23bb053fcbfc85d7b18896c01283b2c415f8b53f21614f797eea7105ab1a5be64b`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → NON_CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=16344486cfbaca59eee75541b8027f7952338fc23a64b66e2a5feb4529abbe23bb053fcbfc85d7b18896c01283b2c415f8b53f21614f797eea7105ab1a5be64b |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (CII)' selected |
| none | `artifacts-retrieved` | Artifact 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' retrieved as xsd |
| none | `artifacts-retrieved` | Artifact 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' retrieved as schematron-xslt2 |
| none | `artifacts-retrieved` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' retrieved as schematron-xslt2 |
| none | `rule-compiled` | Artifact 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' compiled (XML Schema) |
| none | `rule-precompiled` | Artifact 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' passed through (transpiled ahead of time) |
| none | `rule-precompiled` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' passed through (transpiled ahead of time) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| error | `CII-SR-465` | [CII-SR-465] - Only one BT-41 element is allowed on an invoice. (at /Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}CrossIndustryInvoice[1]/Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}SupplyChainTradeTransaction[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}ApplicableHeaderTradeAgreement[1]) |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant (rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd') |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (CII)' non-conformant: 1 error detection(s) from rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant (rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl') |
