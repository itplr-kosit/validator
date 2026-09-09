# config-cases/cen-unit-test/XRechnung-CEFACT-2020-12-17-with-invalid-attachment-without-extension-urn.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (CII)
- **Dokument-Hash**: `SHA-512=ccf074f9a61ce797a11fbf90cbe0dc865675886ed0e9b291638db1510f5024094a7e395832e0ed230e8b06e29438901ff9541d7fb5a783dd49852da54536fa3a`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → NON_CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=ccf074f9a61ce797a11fbf90cbe0dc865675886ed0e9b291638db1510f5024094a7e395832e0ed230e8b06e29438901ff9541d7fb5a783dd49852da54536fa3a |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (CII)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| error | `BR-CL-24` | [BR-CL-24]-For Mime code in attribute use MIMEMediaType. |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (CII)' non-conformant: 1 error detection(s) from rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| error | `decision-reject` | Target 'EN16931 XRechnung (CII)' non-conformant (rule set resources/cii/16b/xsl/EN16931-CII-validation.xsl — 1 error detection(s) from rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl') |
