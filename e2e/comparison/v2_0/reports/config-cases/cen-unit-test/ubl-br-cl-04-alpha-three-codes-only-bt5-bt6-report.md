# config-cases/cen-unit-test/ubl-br-cl-04-alpha-three-codes-only-bt5-bt6.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung (UBL Invoice)
- **Dokument-Hash**: `SHA-512=11ee35e809ab1c266622b995e805c6affe6464f2a5847ea06a81a98a846f7ad0ee4e0e328d65af52f911d3b9987d100ba4a03dfd607de1221c64a542aa627016`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → CONFORMANT
  - XRechnung-UBL-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=11ee35e809ab1c266622b995e805c6affe6464f2a5847ea06a81a98a846f7ad0ee4e0e328d65af52f911d3b9987d100ba4a03dfd607de1221c64a542aa627016 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' applied without findings |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
