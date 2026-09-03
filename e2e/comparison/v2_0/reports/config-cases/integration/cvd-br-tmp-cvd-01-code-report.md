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
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| none | `BR-CL-13` | [BR-CL-13]-Item classification identifier identification scheme identifier MUST be       coded using one of the UNTDID 7143 list. |
| none | `BR-DE-TMP-32` | [BR-DE-TMP-32] Eine Rechnung sollte zur Angabe des Liefer-/Leistungsdatums entweder BT-72 "Actual delivery date", BG-14 "Invoicing period" oder in jeder Rechnungsposition BG-26 "Invoice line period" enthalten. |
| none | `target-conformant` | Target 'EN16931 XRechnung CVD (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung CVD (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung CVD (UBL Invoice)' conformant |
