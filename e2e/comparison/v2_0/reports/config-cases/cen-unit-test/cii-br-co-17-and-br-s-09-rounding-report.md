# config-cases/cen-unit-test/cii-br-co-17-and-br-s-09-rounding.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung (CII)
- **Dokument-Hash**: `SHA-512=dbda6da8798e4566a7e1c91ccf95f2a1350095ffbd8b9e9ad90b079bdee5a71d15865812a273caf73c26b2a9a8b046f7986e740da6386af2d74be635af7503a9`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=dbda6da8798e4566a7e1c91ccf95f2a1350095ffbd8b9e9ad90b079bdee5a71d15865812a273caf73c26b2a9a8b046f7986e740da6386af2d74be635af7503a9 |
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
