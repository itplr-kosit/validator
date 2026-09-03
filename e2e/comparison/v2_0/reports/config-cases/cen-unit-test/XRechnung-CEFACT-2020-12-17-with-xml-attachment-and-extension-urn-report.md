# config-cases/cen-unit-test/XRechnung-CEFACT-2020-12-17-with-xml-attachment-and-extension-urn.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung Extension (CII)
- **Dokument-Hash**: `SHA-512=4be9612e98c9232710d618a47c836d7d5935eaefc7ad657f9c344e17c329d1e84c27578c5274812453cb13dbc4c968a06b00cc4f4e3da7948d7e1d9cfe1c6cd4`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=4be9612e98c9232710d618a47c836d7d5935eaefc7ad657f9c344e17c329d1e84c27578c5274812453cb13dbc4c968a06b00cc4f4e3da7948d7e1d9cfe1c6cd4 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung Extension (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung Extension (CII)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| none | `BR-CL-24` | [BR-CL-24]-For Mime code in attribute use MIMEMediaType. |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (CII)' conformant |
