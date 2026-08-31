# config-cases/integration/ubl-cr-646-sub-invoice-lines-extension.xml

- **Ergebnis**: CONFORMANT
- **Szenario**: EN16931 XRechnung Extension (UBL Invoice)
- **Dokument-Hash**: `SHA-512=49af08bab815fd0c48af866e3d39aa9b0cfe92bcc255a4c5fe4e8b6d9a25b5aa2b4e683356c673a427bc78fbb6642f4e4961f6f47bb2d5fe152b100fbb992b70`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → CONFORMANT
  - XRechnung-UBL-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=49af08bab815fd0c48af866e3d39aa9b0cfe92bcc255a4c5fe4e8b6d9a25b5aa2b4e683356c673a427bc78fbb6642f4e4961f6f47bb2d5fe152b100fbb992b70 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung Extension (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung Extension (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' retrieved as xsd |
| none | `artifacts-retrieved` | Artifact 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' retrieved as schematron-xslt2 |
| none | `artifacts-retrieved` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' retrieved as schematron-xslt2 |
| none | `rule-compiled` | Artifact 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' compiled (XML Schema) |
| none | `rule-precompiled` | Artifact 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' passed through (transpiled ahead of time) |
| none | `rule-precompiled` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' passed through (transpiled ahead of time) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| none | `UBL-CR-646` | [UBL-CR-646]-A UBL invoice should not include the InvoiceLine SubInvoiceLine (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]) |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant (rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd') |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant (rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl') |
| none | `target-conformant` | Target 'EN16931 XRechnung Extension (UBL Invoice)' conformant (rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl') |
