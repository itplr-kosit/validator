# config-cases/cen-unit-test/cii-br-e-10-test-2.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung (CII)
- **Dokument-Hash**: `SHA-512=83cfbce0ef0da72694bc0c23bd0ee9587069d484035a732cf9058dac65ef49382e2ed5a64e6df129f27323812d030d93d813f54ac323227b9b114163f6338d1c`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=83cfbce0ef0da72694bc0c23bd0ee9587069d484035a732cf9058dac65ef49382e2ed5a64e6df129f27323812d030d93d813f54ac323227b9b114163f6338d1c |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (CII)' selected |
| none | `artifacts-retrieved` | Artifact 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' retrieved as xsd |
| none | `artifacts-retrieved` | Artifact 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' retrieved as schematron-xslt2 |
| none | `artifacts-retrieved` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' retrieved as schematron-xslt2 |
| none | `rule-compiled` | Artifact 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' compiled (XML Schema) |
| none | `rule-precompiled` | Artifact 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' passed through (transpiled ahead of time) |
| none | `rule-precompiled` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' passed through (transpiled ahead of time) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' applied without findings |
| warn | `PEPPOL-EN16931-R120` | Invoice line net amount MUST equal (Invoiced quantity * (Item net price/item price base quantity) + Sum of invoice line charge amount - sum of invoice line allowance amount (at /Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}CrossIndustryInvoice[1]/Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}SupplyChainTradeTransaction[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}IncludedSupplyChainTradeLineItem[1]) |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant (rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd') |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant (rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl') |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant (rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl') |
