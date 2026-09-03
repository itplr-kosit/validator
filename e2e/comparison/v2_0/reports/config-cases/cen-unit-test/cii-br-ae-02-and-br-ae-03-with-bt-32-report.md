# config-cases/cen-unit-test/cii-br-ae-02-and-br-ae-03-with-bt-32.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung (CII)
- **Dokument-Hash**: `SHA-512=323884a4412219c4b19ab193fe3d2158e4d366e453b44dc291086605d9f216ba41563075af8c5a01693edf6e76e1d062e6625d2f418890815d9ceef08dfa63df`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=323884a4412219c4b19ab193fe3d2158e4d366e453b44dc291086605d9f216ba41563075af8c5a01693edf6e76e1d062e6625d2f418890815d9ceef08dfa63df |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (CII)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' applied without findings |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| none | `decision-accept` | All 1 conformance target(s) conformant: EN16931 XRechnung (CII) |
