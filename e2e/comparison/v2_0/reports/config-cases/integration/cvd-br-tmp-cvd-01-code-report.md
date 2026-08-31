# config-cases/integration/cvd-br-tmp-cvd-01-code.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung CVD (UBL Invoice)
- **Dokument-Hash**: `SHA-512=d847e403fd01d0c8e47c22614246fd4417f312211ff0a413a757aca0fdbc36697827fb4541ff918402017df358203cb943aa2cd33eb36c96a7cd18843dffd321`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → CONFORMANT
  - XRechnung-UBL-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=d847e403fd01d0c8e47c22614246fd4417f312211ff0a413a757aca0fdbc36697827fb4541ff918402017df358203cb943aa2cd33eb36c96a7cd18843dffd321 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung CVD (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung CVD (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' retrieved as xsd |
| none | `artifacts-retrieved` | Artifact 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' retrieved as schematron-xslt2 |
| none | `artifacts-retrieved` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' retrieved as schematron-xslt2 |
| none | `rule-compiled` | Artifact 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' compiled (XML Schema) |
| none | `rule-precompiled` | Artifact 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' passed through (transpiled ahead of time) |
| none | `rule-precompiled` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' passed through (transpiled ahead of time) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| none | `BR-CL-13` | [BR-CL-13]-Item classification identifier identification scheme identifier MUST be       coded using one of the UNTDID 7143 list. (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}InvoiceLine[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}Item[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}CommodityClassification[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2}ItemClassificationCode[1]) |
| none | `BR-DE-TMP-32` | [BR-DE-TMP-32] Eine Rechnung sollte zur Angabe des Liefer-/Leistungsdatums entweder BT-72 "Actual delivery date", BG-14 "Invoicing period" oder in jeder Rechnungsposition BG-26 "Invoice line period" enthalten. (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]) |
| none | `target-conformant` | Target 'EN16931 XRechnung CVD (UBL Invoice)' conformant (rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd') |
| none | `target-conformant` | Target 'EN16931 XRechnung CVD (UBL Invoice)' conformant (rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl') |
| none | `target-conformant` | Target 'EN16931 XRechnung CVD (UBL Invoice)' conformant (rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl') |
