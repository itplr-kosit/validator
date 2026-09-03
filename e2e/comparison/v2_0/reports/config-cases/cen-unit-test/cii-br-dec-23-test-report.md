# config-cases/cen-unit-test/cii-br-dec-23-test.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung (CII)
- **Dokument-Hash**: `SHA-512=b6735feb0d08d8934862f522c056d916c36bede9f1494e124f97f4f3ecc6014c44f84331da87da6e3afb0ecc681485696127f0d99758d38758588880ff844eb6`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=b6735feb0d08d8934862f522c056d916c36bede9f1494e124f97f4f3ecc6014c44f84331da87da6e3afb0ecc681485696127f0d99758d38758588880ff844eb6 |
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
