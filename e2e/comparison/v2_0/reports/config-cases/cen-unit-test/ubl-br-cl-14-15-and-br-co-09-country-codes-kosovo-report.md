# config-cases/cen-unit-test/ubl-br-cl-14-15-and-br-co-09-country-codes-kosovo.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung (UBL Invoice)
- **Dokument-Hash**: `SHA-512=319ca9a38c350fa5029f8ecf2cb55c13640f54409e78d32722b68f965225dad0886ca6239b669e74eeb53a582b6da19d82ecb4d234056c05583a4718cb3bab4b`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → CONFORMANT
  - XRechnung-UBL-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=319ca9a38c350fa5029f8ecf2cb55c13640f54409e78d32722b68f965225dad0886ca6239b669e74eeb53a582b6da19d82ecb4d234056c05583a4718cb3bab4b |
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
| none | `decision-accept` | All 1 conformance target(s) conformant: EN16931 XRechnung (UBL Invoice) |
