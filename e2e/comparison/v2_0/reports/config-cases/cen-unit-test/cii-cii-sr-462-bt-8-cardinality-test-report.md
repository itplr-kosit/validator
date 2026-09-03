# config-cases/cen-unit-test/cii-cii-sr-462-bt-8-cardinality-test.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (CII)
- **Dokument-Hash**: `SHA-512=fff61e92893da0358bc877a4c1ddad17bd045d2737df7d9631df104a72bd4ffab3939ef0eb6471833f5c34f89478e965b415634e761e538e3339ef516cf23923`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → NON_CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=fff61e92893da0358bc877a4c1ddad17bd045d2737df7d9631df104a72bd4ffab3939ef0eb6471833f5c34f89478e965b415634e761e538e3339ef516cf23923 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (CII)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| error | `CII-SR-462` | [CII-SR-462] - Only one DueDateTypeCode shall be present |
| error | `BR-CL-06` | [BR-CL-06]-Value added tax point date code MUST be coded using a restriction of UNTDID 2475. |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (CII)' non-conformant: 2 error detection(s) from rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| error | `decision-reject` | Target 'EN16931 XRechnung (CII)' non-conformant (rule set resources/cii/16b/xsl/EN16931-CII-validation.xsl — 2 error detection(s) from rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl') |
