# config-cases/instances/processing-valid/ubl004.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (UBL Invoice)
- **Dokument-Hash**: `SHA-512=ae65708ae5d1dc3ddc3e5785d36c8ff916f30f9de9452102153c24ebe706d8f63a4fa0e1e9a506257b788463441d5b38bc4ff5a184180628e182a093efb3899d`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → CONFORMANT
  - XRechnung-UBL-validation.xsl → NON_CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=ae65708ae5d1dc3ddc3e5785d36c8ff916f30f9de9452102153c24ebe706d8f63a4fa0e1e9a506257b788463441d5b38bc4ff5a184180628e182a093efb3899d |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' applied without findings |
| none | `BR-DE-TMP-32` | [BR-DE-TMP-32] Eine Rechnung sollte zur Angabe des Liefer-/Leistungsdatums entweder BT-72 "Actual delivery date", BG-14 "Invoicing period" oder in jeder Rechnungsposition BG-26 "Invoice line period" enthalten. |
| error | `BR-DE-3` | [BR-DE-3] Das Element "Seller city" (BT-37) muss übermittelt werden. |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant: 1 error detection(s) from rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' |
| error | `decision-reject` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant (rule set resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl — 1 error detection(s) from rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl') |
