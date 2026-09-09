# config-cases/instances/processing-valid/ubl002.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (UBL Invoice)
- **Dokument-Hash**: `SHA-512=e768a4de35fb0eb1c55c290de081ba900996cb1329081ee7299cd761d315d1b745aadd0ebf4e27de15a5389cc55abbae2e151977a195ede386753b013d6aba5f`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → NON_CONFORMANT
  - XRechnung-UBL-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=e768a4de35fb0eb1c55c290de081ba900996cb1329081ee7299cd761d315d1b745aadd0ebf4e27de15a5389cc55abbae2e151977a195ede386753b013d6aba5f |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| error | `BR-06` | [BR-06]-An Invoice shall contain the Seller name (BT-27). |
| warn | `UBL-CR-001` | [UBL-CR-001]-A UBL invoice should not include extensions |
| none | `BR-DE-TMP-32` | [BR-DE-TMP-32] Eine Rechnung sollte zur Angabe des Liefer-/Leistungsdatums entweder BT-72 "Actual delivery date", BG-14 "Invoicing period" oder in jeder Rechnungsposition BG-26 "Invoice line period" enthalten. |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant: 1 error detection(s) from rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| error | `decision-reject` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant (rule set resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl — 1 error detection(s) from rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl') |
