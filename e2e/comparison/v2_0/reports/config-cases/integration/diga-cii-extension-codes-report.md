# config-cases/integration/diga-cii-extension-codes.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung Extension (CII)
- **Dokument-Hash**: `SHA-512=c6e7508a0335297abec86b4b6e4400da2d816866801cae9328d5d8bf8cd0968740714d3e41cc0e74bc9bd64102d33ada7f2e95c32276f573714f39c1cff6b89e`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=c6e7508a0335297abec86b4b6e4400da2d816866801cae9328d5d8bf8cd0968740714d3e41cc0e74bc9bd64102d33ada7f2e95c32276f573714f39c1cff6b89e |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung Extension (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung Extension (CII)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| none | `BR-CL-21` | [BR-CL-21]-Item standard identifier scheme identifier MUST belong to the ISO 6523 ICD       code list |
| none | `BR-CL-11` | [BR-CL-11]-Any registration identifier identification scheme identifier MUST be coded using one of the ISO 6523 ICD list. |
| none | `BR-CL-11` | [BR-CL-11]-Any registration identifier identification scheme identifier MUST be coded using one of the ISO 6523 ICD list. |
| none | `BR-CL-11` | [BR-CL-11]-Any registration identifier identification scheme identifier MUST be coded using one of the ISO 6523 ICD list. |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant |
