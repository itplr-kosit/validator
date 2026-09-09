# config-cases/integration/cii-cii-sr-454-negative-test.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (CII)
- **Dokument-Hash**: `SHA-512=39146d912b2503791d9e5ec96f4f4363bab48734c92089a81bef27c51a0e705004805d057ad93b61e242bd588e7a1aa85338c298f672ce9a73ecf2b6f462a55f`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → NON_CONFORMANT
  - XRechnung-CII-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=39146d912b2503791d9e5ec96f4f4363bab48734c92089a81bef27c51a0e705004805d057ad93b61e242bd588e7a1aa85338c298f672ce9a73ecf2b6f462a55f |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (CII)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| error | `CII-SR-454` | [CII-SR-454] - Only one ApplicableTradeTax should be present |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (CII)' non-conformant: 1 error detection(s) from rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| error | `decision-reject` | Target 'EN16931 XRechnung (CII)' non-conformant (rule set resources/cii/16b/xsl/EN16931-CII-validation.xsl — 1 error detection(s) from rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl') |
