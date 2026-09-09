# config-cases/instances/processing-valid/ubl001.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung (UBL Invoice)
- **Dokument-Hash**: `SHA-512=38e3f33e7d27caf76b5d8ac212a49a64d76a5717492279e1a081b2c306b19791238c8abe98e27192b90802d0879eadb4b96e72a4fa8bc07f85a5b36b3948c05b`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → CONFORMANT
  - XRechnung-UBL-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=38e3f33e7d27caf76b5d8ac212a49a64d76a5717492279e1a081b2c306b19791238c8abe98e27192b90802d0879eadb4b96e72a4fa8bc07f85a5b36b3948c05b |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' applied without findings |
| none | `BR-DE-TMP-32` | [BR-DE-TMP-32] Eine Rechnung sollte zur Angabe des Liefer-/Leistungsdatums entweder BT-72 "Actual delivery date", BG-14 "Invoicing period" oder in jeder Rechnungsposition BG-26 "Invoice line period" enthalten. |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| none | `decision-accept` | All 1 conformance target(s) conformant: EN16931 XRechnung (UBL Invoice) |
