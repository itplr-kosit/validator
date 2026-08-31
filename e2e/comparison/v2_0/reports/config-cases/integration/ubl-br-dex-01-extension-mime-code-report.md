# config-cases/integration/ubl-br-dex-01-extension-mime-code.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung Extension (UBL Invoice)
- **Dokument-Hash**: `SHA-512=8c7d6082b19dd5af6a818dc998ea2fca6f265c5b82d798bf0597054c4a37fbbbf514aa88c59453d55e69110d7c72bdcfddf554b81b7e8396051a2aabacfd8694`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → CONFORMANT
  - XRechnung-UBL-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=8c7d6082b19dd5af6a818dc998ea2fca6f265c5b82d798bf0597054c4a37fbbbf514aa88c59453d55e69110d7c72bdcfddf554b81b7e8396051a2aabacfd8694 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung Extension (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung Extension (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' retrieved as xsd |
| none | `artifacts-retrieved` | Artifact 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' retrieved as schematron-xslt2 |
| none | `artifacts-retrieved` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' retrieved as schematron-xslt2 |
| none | `rule-compiled` | Artifact 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' compiled (XML Schema) |
| none | `rule-precompiled` | Artifact 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' passed through (transpiled ahead of time) |
| none | `rule-precompiled` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' passed through (transpiled ahead of time) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| none | `BR-CL-24` | [BR-CL-24]-For Mime code in attribute use MIMEMediaType. (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}AdditionalDocumentReference[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}Attachment[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2}EmbeddedDocumentBinaryObject[1]) |
| none | `BR-DE-TMP-32` | [BR-DE-TMP-32] Eine Rechnung sollte zur Angabe des Liefer-/Leistungsdatums entweder BT-72 "Actual delivery date", BG-14 "Invoicing period" oder in jeder Rechnungsposition BG-26 "Invoice line period" enthalten. (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]) |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant (rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd') |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant (rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl') |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant (rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl') |
