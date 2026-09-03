# config-cases/cen-unit-test/cii-br-e-10-test.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung (CII)
- **Dokument-Hash**: `SHA-512=132820d091430d0c2bc4914ec2ff4ec2535a624c3d06704ef6939b0fa82af7f0d68cf98da86dbc9673cb8298f218dd1afcfd174c530bab6645b27c61d1b098ac`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=132820d091430d0c2bc4914ec2ff4ec2535a624c3d06704ef6939b0fa82af7f0d68cf98da86dbc9673cb8298f218dd1afcfd174c530bab6645b27c61d1b098ac |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (CII)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' applied without findings |
| warn | `PEPPOL-EN16931-R120` | Invoice line net amount MUST equal (Invoiced quantity * (Item net price/item price base quantity) + Sum of invoice line charge amount - sum of invoice line allowance amount |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| none | `decision-accept` | All 1 conformance target(s) conformant: EN16931 XRechnung (CII) |
