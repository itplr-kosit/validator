# config-cases/instances/processing-valid/ubl005.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (UBL Invoice)
- **Dokument-Hash**: `SHA-512=b7f013af5787ec64fb5dffe7ed8351f43da356e0756f04c96f54d3cf47d65ecbb5c003ef729673555a38b7fccccb6142c2e1f56b6d5ac196f4bfd691907a3624`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → NON_CONFORMANT
  - EN16931-UBL-validation.xsl → NON_CONFORMANT
  - XRechnung-UBL-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=b7f013af5787ec64fb5dffe7ed8351f43da356e0756f04c96f54d3cf47d65ecbb5c003ef729673555a38b7fccccb6142c2e1f56b6d5ac196f4bfd691907a3624 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| error | `schema-violation` | cvc-complex-type.2.4.a: Ungültiger Content wurde beginnend mit Element '{"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":DueDate}' gefunden. '{"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":CopyIndicator, "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":UUID, "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":IssueDate}' wird erwartet. |
| error | `BR-03` | [BR-03]-An Invoice shall have an Invoice issue date (BT-2). |
| none | `BR-DE-TMP-32` | [BR-DE-TMP-32] Eine Rechnung sollte zur Angabe des Liefer-/Leistungsdatums entweder BT-72 "Actual delivery date", BG-14 "Invoicing period" oder in jeder Rechnungsposition BG-26 "Invoice line period" enthalten. |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant: 1 error detection(s) from rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant: 1 error detection(s) from rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| error | `decision-reject` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant (rule set resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd — 1 error detection(s) from rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd'); Target 'EN16931 XRechnung (UBL Invoice)' non-conformant (rule set resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl — 1 error detection(s) from rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl') |
