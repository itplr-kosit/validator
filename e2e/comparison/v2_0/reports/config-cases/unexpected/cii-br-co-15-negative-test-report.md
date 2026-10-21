# config-cases/unexpected/cii-br-co-15-negative-test.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (CII)
- **Dokument-Hash**: `SHA-512=fe2d5ac4eb74ca15d6252a476c8e0694ae6126482b1c5b6b5cb4158afadcff3733cc8801ed304c28c4fa28ba6d778be818a64c6be55b8689fcfed6b4eba583fd`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - CrossIndustryInvoice_100pD16B.xsd → CONFORMANT
  - EN16931-CII-validation.xsl → NON_CONFORMANT
  - XRechnung-CII-validation.xsl → NON_CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=fe2d5ac4eb74ca15d6252a476c8e0694ae6126482b1c5b6b5cb4158afadcff3733cc8801ed304c28c4fa28ba6d778be818a64c6be55b8689fcfed6b4eba583fd |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (CII)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (CII)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/cii/16b/xsd/CrossIndustryInvoice_100pD16B.xsd' applied without findings |
| error | `BR-53` | [BR-53]-If the VAT accounting currency code (BT-6) is present, then the Invoice total VAT amount in accounting currency (BT-111) shall be provided. |
| error | `PEPPOL-EN16931-R005` | VAT accounting currency code MUST be different from invoice currency code when provided. |
| error | `PEPPOL-EN16931-R054` | Only one tax total amount must be provided where currency id equals tax currency code, if tax currency code (BT-6) is provided. |
| none | `target-conformant` | Target 'EN16931 XRechnung (CII)' conformant |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (CII)' non-conformant: 1 error detection(s) from rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl' |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (CII)' non-conformant: 2 error detection(s) from rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl' |
| error | `decision-reject` | Target 'EN16931 XRechnung (CII)' non-conformant (rule set resources/cii/16b/xsl/EN16931-CII-validation.xsl — 1 error detection(s) from rule set 'resources/cii/16b/xsl/EN16931-CII-validation.xsl'); Target 'EN16931 XRechnung (CII)' non-conformant (rule set resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl — 2 error detection(s) from rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-CII-validation.xsl') |
