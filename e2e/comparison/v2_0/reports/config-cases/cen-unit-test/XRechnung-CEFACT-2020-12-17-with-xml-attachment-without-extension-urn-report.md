# config-cases/cen-unit-test/XRechnung-CEFACT-2020-12-17-with-xml-attachment-without-extension-urn.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (CII)
- **Dokument-Hash**: `SHA-512=2e102c102bc278f52aa1173aa84db570b42d24c091f2d7fd5c70647c528fa5645a905d8e77852aaee3cb9620dc627e44a1bd59f3f1027bde245f35094c0db726`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → NON_CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=2e102c102bc278f52aa1173aa84db570b42d24c091f2d7fd5c70647c528fa5645a905d8e77852aaee3cb9620dc627e44a1bd59f3f1027bde245f35094c0db726 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (CII)' selected |
| none | `artifacts-retrieved` | Artifact 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' retrieved as xsd |
| none | `artifacts-retrieved` | Artifact 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' retrieved as schematron-xslt2 |
| none | `artifacts-retrieved` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' retrieved as schematron-xslt2 |
| none | `rule-compiled` | Artifact 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' compiled (XML Schema) |
| none | `rule-precompiled` | Artifact 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' passed through (transpiled ahead of time) |
| none | `rule-precompiled` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' passed through (transpiled ahead of time) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| error | `BR-CL-24` | [BR-CL-24]-For Mime code in attribute use MIMEMediaType. (at /Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}CrossIndustryInvoice[1]/Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}SupplyChainTradeTransaction[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}ApplicableHeaderTradeAgreement[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}AdditionalReferencedDocument[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}AttachmentBinaryObject[1]) |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant (rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd') |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (CII)' non-conformant: 1 error detection(s) from rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant (rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl') |
