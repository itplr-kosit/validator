# config-cases/cen-unit-test/ubl-br-o-02-and-br-o-04-with-bt-63.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (UBL Invoice)
- **Dokument-Hash**: `SHA-512=2a2fcc1af0f83c7b2fb4b9793cedb973c114e8224a1eb75a86bb20c8e098aece592289a99773b7d571d845be2b08a819292f0d7a3892867678a084e3916cc596`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-Invoice-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → NON_CONFORMANT
  - XRechnung-UBL-validation.xsl → CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=2a2fcc1af0f83c7b2fb4b9793cedb973c114e8224a1eb75a86bb20c8e098aece592289a99773b7d571d845be2b08a819292f0d7a3892867678a084e3916cc596 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (UBL Invoice)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (UBL Invoice)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd' applied without findings |
| error | `BR-O-02` | [BR-O-02]-An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48). |
| error | `BR-O-04` | [BR-O-04]-An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48). |
| error | `UBL-SR-53` | [UBL-SR-53]- CompanyID (VAT Identifier) must be stated when providing the PartyTaxScheme/TaxScheme/ID. |
| error | `UBL-SR-53` | [UBL-SR-53]- CompanyID (VAT Identifier) must be stated when providing the PartyTaxScheme/TaxScheme/ID. |
| none | `rules-applied` | Rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' applied without findings |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant: 4 error detection(s) from rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL Invoice)' conformant |
| error | `decision-reject` | Target 'EN16931 XRechnung (UBL Invoice)' non-conformant (rule set resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl — 4 error detection(s) from rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl') |
