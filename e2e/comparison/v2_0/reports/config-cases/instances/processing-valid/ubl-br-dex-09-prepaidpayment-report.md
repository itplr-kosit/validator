# config-cases/instances/processing-valid/ubl-br-dex-09-prepaidpayment.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung Extension (UBL Invoice)
- **Dokument-Hash**: `SHA-512=be9dc444b21fc22f1ad144ea6f0d1f3fa659ad5e56ea7b28f7c805e3f427bd7b157d00feb8fb94e1dea9f4d7a245834aa4e74ce6a6c59e4cc5881d4948bd813d`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → CONFORMANT
  - XRechnung-UBL-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=be9dc444b21fc22f1ad144ea6f0d1f3fa659ad5e56ea7b28f7c805e3f427bd7b157d00feb8fb94e1dea9f4d7a245834aa4e74ce6a6c59e4cc5881d4948bd813d |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung Extension (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung Extension (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| none | `BR-CO-16` | [BR-CO-16]-Amount due for payment (BT-115) = Invoice total amount with VAT (BT-112) -Paid amount (BT-113) +Rounding amount (BT-114). |
| none | `UBL-CR-470` | [UBL-CR-470]-A UBL invoice should not include the PrepaidPayment |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant |
