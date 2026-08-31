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
| none | `artifacts-retrieved` | Artifact 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' retrieved as xsd |
| none | `artifacts-retrieved` | Artifact 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' retrieved as schematron-xslt2 |
| none | `artifacts-retrieved` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' retrieved as schematron-xslt2 |
| none | `rule-compiled` | Artifact 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' compiled (XML Schema) |
| none | `rule-precompiled` | Artifact 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' passed through (transpiled ahead of time) |
| none | `rule-precompiled` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' passed through (transpiled ahead of time) |
| error | `schema-violation` | cvc-complex-type.2.4.a: Ungültiger Content wurde beginnend mit Element '{"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":DueDate}' gefunden. '{"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":CopyIndicator, "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":UUID, "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":IssueDate}' wird erwartet. |
| error | `BR-03` | [BR-03]-An Invoice shall have an Invoice issue date (BT-2). (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]) |
| none | `BR-DE-TMP-32` | [BR-DE-TMP-32] Eine Rechnung sollte zur Angabe des Liefer-/Leistungsdatums entweder BT-72 "Actual delivery date", BG-14 "Invoicing period" oder in jeder Rechnungsposition BG-26 "Invoice line period" enthalten. (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]) |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant: 1 error detection(s) from rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant: 1 error detection(s) from rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant (rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl') |
