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
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| none | `BR-CL-24` | [BR-CL-24]-For Mime code in attribute use MIMEMediaType. |
| none | `BR-DE-TMP-32` | [BR-DE-TMP-32] Eine Rechnung sollte zur Angabe des Liefer-/Leistungsdatums entweder BT-72 "Actual delivery date", BG-14 "Invoicing period" oder in jeder Rechnungsposition BG-26 "Invoice line period" enthalten. |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant |
