# config-cases/cen-unit-test/ubl-br-o-02-and-br-o-04-with-bt-31.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (UBL Invoice)
- **Dokument-Hash**: `SHA-512=22614fff0e4dec04d8c8f229e7e36f640e5fdcdf27b7bb6db5c6749a56c9fa41a9f790853ec27f2c5a5a46c87c248203b556af7792939f0ea04616a81c4b3b0e`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → NON_CONFORMANT
  - XRechnung-UBL-validation.xsl → NON_CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=22614fff0e4dec04d8c8f229e7e36f640e5fdcdf27b7bb6db5c6749a56c9fa41a9f790853ec27f2c5a5a46c87c248203b556af7792939f0ea04616a81c4b3b0e |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' retrieved as xsd |
| none | `artifacts-retrieved` | Artifact 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' retrieved as schematron-xslt2 |
| none | `artifacts-retrieved` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' retrieved as schematron-xslt2 |
| none | `rule-compiled` | Artifact 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' compiled (XML Schema) |
| none | `rule-precompiled` | Artifact 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' passed through (transpiled ahead of time) |
| none | `rule-precompiled` | Artifact 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' passed through (transpiled ahead of time) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| error | `BR-O-02` | [BR-O-02]-An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48). (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]) |
| error | `BR-O-04` | [BR-O-04]-An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48). (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]) |
| error | `UBL-SR-53` | [UBL-SR-53]- CompanyID (VAT Identifier) must be stated when providing the PartyTaxScheme/TaxScheme/ID. (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}AccountingCustomerParty[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}Party[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}PartyTaxScheme[1]) |
| error | `BR-DE-1` | [BR-DE-1] Eine Rechnung (INVOICE) muss Angaben zu "PAYMENT INSTRUCTIONS" (BG-16) enthalten. (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]) |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant (rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd') |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant: 3 error detection(s) from rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant: 1 error detection(s) from rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' |
