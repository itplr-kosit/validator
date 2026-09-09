# config-cases/unexpected/ubl-br-cl-14-15-and-br-co-09-country-codes-kosovo.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung (UBL Invoice)
- **Dokument-Hash**: `SHA-512=f675735dc78fdc85039d93f5eaed54399c25798d5bdb8a9c47de6dfc30cb5007193139c7a5bf1fb4b91f305fb8fd839a5e1c6c9723bf35ce3c07ffb7d0f164b0`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → CONFORMANT
  - XRechnung-UBL-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=f675735dc78fdc85039d93f5eaed54399c25798d5bdb8a9c47de6dfc30cb5007193139c7a5bf1fb4b91f305fb8fd839a5e1c6c9723bf35ce3c07ffb7d0f164b0 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' applied without findings |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| none | `decision-accept` | All 1 conformance target(s) conformant: EN16931 XRechnung (UBL Invoice) |
