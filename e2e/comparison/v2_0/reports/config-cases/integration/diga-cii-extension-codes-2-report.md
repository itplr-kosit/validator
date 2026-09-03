# config-cases/integration/diga-cii-extension-codes-2.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung Extension (CII)
- **Dokument-Hash**: `SHA-512=541662ff49c62afaee6da35cc84aa8c8e66ea66773e7ea44339f8a1d9ef487f065f3ca645c300c8c844e8e9449068e17ee5435f4371cab5df78fe5a50d8663e2`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=541662ff49c62afaee6da35cc84aa8c8e66ea66773e7ea44339f8a1d9ef487f065f3ca645c300c8c844e8e9449068e17ee5435f4371cab5df78fe5a50d8663e2 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung Extension (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung Extension (CII)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| none | `BR-CL-21` | [BR-CL-21]-Item standard identifier scheme identifier MUST belong to the ISO 6523 ICD       code list |
| none | `BR-CL-10` | [BR-CL-10]-Any identifier identification scheme identifier MUST be coded using one of the ISO 6523 ICD list. |
| none | `BR-CL-25` | [BR-CL-25]-Endpoint identifier scheme identifier MUST belong to the CEF EAS code list |
| none | `BR-CL-10` | [BR-CL-10]-Any identifier identification scheme identifier MUST be coded using one of the ISO 6523 ICD list. |
| none | `BR-CL-25` | [BR-CL-25]-Endpoint identifier scheme identifier MUST belong to the CEF EAS code list |
| none | `BR-CL-26` | [BR-CL-26]-Delivery location identifier scheme identifier MUST belong to the ISO 6523 ICD       code list |
| none | `BR-CL-10` | [BR-CL-10]-Any identifier identification scheme identifier MUST be coded using one of the ISO 6523 ICD list. |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant |
| none | `decision-accept` | All 1 conformance target(s) conformant: EN16931 XRechnung Extension (CII) |
