# config-cases/integration/cii-br-dex-01-extension-mime-code.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung Extension (CII)
- **Dokument-Hash**: `SHA-512=4c4bbfce5230ace782c60d0574d3bfd5845382422229b9061946d6dd0992c1013cf338afae2ca4df8a67467886c568dc8e4ab866e278d3ba3545e1fb3de48df7`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=4c4bbfce5230ace782c60d0574d3bfd5845382422229b9061946d6dd0992c1013cf338afae2ca4df8a67467886c568dc8e4ab866e278d3ba3545e1fb3de48df7 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung Extension (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung Extension (CII)' selected |
| none | `artifacts-retrieved` | Artifact 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' retrieved as xsd |
| none | `artifacts-retrieved` | Artifact 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' retrieved as schematron-xslt2 |
| none | `artifacts-retrieved` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' retrieved as schematron-xslt2 |
| none | `rule-compiled` | Artifact 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' compiled (XML Schema) |
| none | `rule-precompiled` | Artifact 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' passed through (transpiled ahead of time) |
| none | `rule-precompiled` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' passed through (transpiled ahead of time) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| none | `BR-CL-24` | [BR-CL-24]-For Mime code in attribute use MIMEMediaType. (at /Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}CrossIndustryInvoice[1]/Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}SupplyChainTradeTransaction[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}ApplicableHeaderTradeAgreement[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}AdditionalReferencedDocument[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}AttachmentBinaryObject[1]) |
| none | `BR-DE-TMP-32` | [BR-DE-TMP-32] Eine Rechnung sollte zur Angabe des Liefer-/Leistungsdatums entweder BT-72 "Actual delivery date", BG-14 "Invoicing period" oder in jeder Rechnungsposition BG-26 "Invoice line period" enthalten. (at /Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}CrossIndustryInvoice[1]/Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}SupplyChainTradeTransaction[1]) |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant (rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd') |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant (rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl') |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant (rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl') |
