# config-cases/instances/processing-valid/ubl008.xml

- **Ergebnis**: NON_CONFORMANT
- **Szenario**: EN16931 XRechnung (UBL CreditNote)
- **Dokument-Hash**: `SHA-512=5e3062fbd1b75acaed3296f666cf2c0f5a5389c6709adfa62751109aab671bdad18d65c1b410c2f776dbc140f238762c7f4d38cde00456517cb6f4a52db34a34`
- **RuleSets**: 3
- **Conformance je RuleSet**:
  - UBL-CreditNote-2.1.xsd → CONFORMANT
  - EN16931-UBL-validation.xsl → NON_CONFORMANT
  - XRechnung-UBL-validation.xsl → NON_CONFORMANT

## Detections (Steps 2–8, in Pipeline-Reihenfolge)

| Severity | Code | Meldung |
|---|---|---|
| none | `document-parsed` | SHA-512=5e3062fbd1b75acaed3296f666cf2c0f5a5389c6709adfa62751109aab671bdad18d65c1b410c2f776dbc140f238762c7f4d38cde00456517cb6f4a52db34a34 |
| none | `scenario-matched` | Scenario 'EN16931 XRechnung (UBL CreditNote)' matched |
| none | `scenario-selected` | Scenario 'EN16931 XRechnung (UBL CreditNote)' selected |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `artifacts-retrieved` | Artifact retrieved |
| none | `rule-compiled` | Compiled (XML Schema) |
| none | `rules-applied` | Rule set 'resources/ubl/2.1/xsd/maindoc/UBL-CreditNote-2.1.xsd' applied without findings |
| error | `BR-S-01` | [BR-S-01]-An Invoice that contains an Invoice line (BG-25), a Document level allowance (BG-20) or a Document level charge (BG-21) where the VAT category code (BT-151, BT-95 or BT-102) is "Standard rated" shall contain in the VAT breakdown (BG-23) at least one VAT category code (BT-118) equal with "Standard rated". |
| error | `BR-S-08` | [BR-S-08]-For each different value of VAT category rate (BT-119) where the VAT category code (BT-118) is "Standard rated", the VAT category taxable amount (BT-116) in a VAT breakdown (BG-23) shall equal the sum of Invoice line net amounts (BT-131) plus the sum of document level charge amounts (BT-99) minus the sum of document level allowance amounts (BT-92) where the VAT category code (BT-151, BT-102, BT-95) is "Standard rated" and the VAT rate (BT-152, BT-103, BT-96) equals the VAT category rate (BT-119). |
| error | `BR-S-08` | [BR-S-08]-For each different value of VAT category rate (BT-119) where the VAT category code (BT-118) is "Standard rated", the VAT category taxable amount (BT-116) in a VAT breakdown (BG-23) shall equal the sum of Invoice line net amounts (BT-131) plus the sum of document level charge amounts (BT-99) minus the sum of document level allowance amounts (BT-92) where the VAT category code (BT-151, BT-102, BT-95) is "Standard rated" and the VAT rate (BT-152, BT-103, BT-96) equals the VAT category rate (BT-119). |
| error | `BR-CO-10` | [BR-CO-10]-Sum of Invoice line net amount (BT-106) = Σ Invoice line net amount (BT-131). |
| error | `BR-22` | [BR-22]-Each Invoice line (BG-25) shall have an Invoiced quantity (BT-129). |
| error | `BR-23` | [BR-23]-An Invoice line (BG-25) shall have an Invoiced quantity unit of measure code (BT-130). |
| error | `BR-24` | [BR-24]-Each Invoice line (BG-25) shall have an Invoice line net amount (BT-131). |
| error | `BR-25` | [BR-25]-Each Invoice line (BG-25) shall contain the Item name (BT-153). |
| error | `BR-26` | [BR-26]-Each Invoice line (BG-25) shall contain the Item net price (BT-146). |
| error | `BR-27` | [BR-27]-The Item net price (BT-146) shall NOT be negative. |
| error | `BR-CO-04` | [BR-CO-04]-Each Invoice line (BG-25) shall be categorized with an Invoiced item VAT category code (BT-151). |
| error | `UBL-SR-48` | [UBL-SR-48]-Invoice lines shall have one and only one classified tax category. |
| error | `BR-CL-01` | [BR-CL-01]-The document type code MUST be coded by the invoice and credit note related code lists of UNTDID 1001. |
| error | `BR-DE-15` | [BR-DE-15] Das Element "Buyer reference" (BT-10) muss übermittelt werden. |
| none | `BR-DE-TMP-32` | [BR-DE-TMP-32] Eine Rechnung sollte zur Angabe des Liefer-/Leistungsdatums entweder BT-72 "Actual delivery date", BG-14 "Invoicing period" oder in jeder Rechnungsposition BG-26 "Invoice line period" enthalten. |
| none | `target-conformant` | Target 'EN16931 XRechnung (UBL CreditNote)' conformant |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (UBL CreditNote)' non-conformant: 13 error detection(s) from rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl' |
| error | `target-non-conformant` | Target 'EN16931 XRechnung (UBL CreditNote)' non-conformant: 1 error detection(s) from rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl' |
| error | `decision-reject` | Target 'EN16931 XRechnung (UBL CreditNote)' non-conformant (rule set resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl — 13 error detection(s) from rule set 'resources/ubl/2.1/xsl/EN16931-UBL-validation.xsl'); Target 'EN16931 XRechnung (UBL CreditNote)' non-conformant (rule set resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl — 1 error detection(s) from rule set 'resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl') |
