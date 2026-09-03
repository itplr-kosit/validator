# XRechnung E2E — Findings je Instanz (alle Nicht-INFO-Detections)

## config-cases/cen-unit-test/cii-br-e-10-test-2.xml — CONFORMANT
- `warn` **PEPPOL-EN16931-R120** — Invoice line net amount MUST equal (Invoiced quantity * (Item net price/item price base quantity) + Sum of invoice line charge amount - sum of invoice line allowance amount

## config-cases/cen-unit-test/cii-br-e-10-test.xml — CONFORMANT
- `warn` **PEPPOL-EN16931-R120** — Invoice line net amount MUST equal (Invoiced quantity * (Item net price/item price base quantity) + Sum of invoice line charge amount - sum of invoice line allowance amount

## config-cases/cen-unit-test/cii-bt-20-cardinality-check-2.xml — NON_CONFORMANT
- `error` **CII-SR-452** — [CII-SR-452] - Only one SpecifiedTradePaymentTerms should be present
- `error` **CII-SR-453** — [CII-SR-453] - Only one SpecifiedTradePaymentTerms Description should be present

## config-cases/cen-unit-test/cii-bt-20-cardinality-check.xml — NON_CONFORMANT
- `error` **CII-SR-453** — [CII-SR-453] - Only one SpecifiedTradePaymentTerms Description should be present

## config-cases/cen-unit-test/cii-cii-sr-454-negative-test.xml — NON_CONFORMANT
- `error` **CII-SR-454** — [CII-SR-454] - Only one ApplicableTradeTax should be present

## config-cases/cen-unit-test/cii-cii-sr-461-bt-7-cardinality-test.xml — NON_CONFORMANT
- `error` **CII-SR-461** — [CII-SR-461] - Only one TaxPointDate shall be present

## config-cases/cen-unit-test/cii-cii-sr-462-bt-8-cardinality-test.xml — NON_CONFORMANT
- `error` **CII-SR-462** — [CII-SR-462] - Only one DueDateTypeCode shall be present
- `error` **BR-CL-06** — [BR-CL-06]-Value added tax point date code MUST be coded using a restriction of UNTDID 2475.

## config-cases/cen-unit-test/cii-cii-sr-465-negative-test.xml — NON_CONFORMANT
- `error` **CII-SR-465** — [CII-SR-465] - Only one BT-41 element is allowed on an invoice.

## config-cases/cen-unit-test/cii-cii-sr-466-negative-test.xml — NON_CONFORMANT
- `error` **CII-SR-466** — [CII-SR-466] - Only one BT-56 element is allowed on an invoice.

## config-cases/cen-unit-test/ubl-br-co-15-no-multiple-bt-110-allowed-test.xml — NON_CONFORMANT
- `error` **BR-CO-15** — [BR-CO-15]-Invoice total amount with VAT (BT-112) = Invoice total amount without VAT (BT-109) + Invoice total VAT amount (BT-110).
- `error` **PEPPOL-EN16931-R053** — Only one tax total with tax subtotals MUST be provided.

## config-cases/cen-unit-test/ubl-br-o-02-and-br-o-03-with-bt-31.xml — NON_CONFORMANT
- `error` **BR-O-02** — [BR-O-02]-An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48).
- `error` **BR-O-03** — [BR-O-03]-An Invoice that contains a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48).
- `error` **UBL-SR-53** — [UBL-SR-53]- CompanyID (VAT Identifier) must be stated when providing the PartyTaxScheme/TaxScheme/ID.

## config-cases/cen-unit-test/ubl-br-o-02-and-br-o-03-with-bt-63.xml — NON_CONFORMANT
- `error` **BR-O-02** — [BR-O-02]-An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48).
- `error` **BR-O-03** — [BR-O-03]-An Invoice that contains a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48).
- `error` **UBL-SR-53** — [UBL-SR-53]- CompanyID (VAT Identifier) must be stated when providing the PartyTaxScheme/TaxScheme/ID.
- `error` **UBL-SR-53** — [UBL-SR-53]- CompanyID (VAT Identifier) must be stated when providing the PartyTaxScheme/TaxScheme/ID.

## config-cases/cen-unit-test/ubl-br-o-02-and-br-o-04-with-bt-31.xml — NON_CONFORMANT
- `error` **BR-O-02** — [BR-O-02]-An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48).
- `error` **BR-O-04** — [BR-O-04]-An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48).
- `error` **UBL-SR-53** — [UBL-SR-53]- CompanyID (VAT Identifier) must be stated when providing the PartyTaxScheme/TaxScheme/ID.
- `error` **BR-DE-1** — [BR-DE-1] Eine Rechnung (INVOICE) muss Angaben zu "PAYMENT INSTRUCTIONS" (BG-16) enthalten.

## config-cases/cen-unit-test/ubl-br-o-02-and-br-o-04-with-bt-63.xml — NON_CONFORMANT
- `error` **BR-O-02** — [BR-O-02]-An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48).
- `error` **BR-O-04** — [BR-O-04]-An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48).
- `error` **UBL-SR-53** — [UBL-SR-53]- CompanyID (VAT Identifier) must be stated when providing the PartyTaxScheme/TaxScheme/ID.
- `error` **UBL-SR-53** — [UBL-SR-53]- CompanyID (VAT Identifier) must be stated when providing the PartyTaxScheme/TaxScheme/ID.

## config-cases/cen-unit-test/XRechnung-CEFACT-2020-12-17-with-invalid-attachment-without-extension-urn.xml — NON_CONFORMANT
- `error` **BR-CL-24** — [BR-CL-24]-For Mime code in attribute use MIMEMediaType.

## config-cases/cen-unit-test/XRechnung-CEFACT-2020-12-17-with-xml-attachment-without-extension-urn.xml — NON_CONFORMANT
- `error` **BR-CL-24** — [BR-CL-24]-For Mime code in attribute use MIMEMediaType.

## config-cases/instances/processing-error/bin001.xml — FAILED@PARSE_DOCUMENT
- `error` **not-wellformed** — Content ist nicht zulässig in Prolog.

## config-cases/instances/processing-error/ubl007.xml — FAILED@PARSE_DOCUMENT
- `error` **not-wellformed** — XML-Dokumentstrukturen müssen innerhalb derselben Entity beginnen und enden.

## config-cases/instances/processing-valid/cii001.xml — NON_CONFORMANT
- `error` **BR-S-08** — [BR-S-08]-For each different value of VAT category rate (BT-119) where the VAT category code (BT-118) is "Standard rated", the VAT category taxable amount (BT-116) in a VAT breakdown (BG-23) shall equal the sum of Invoice line net amounts (BT-131) plus the sum of document level charge amounts (BT-99) minus the sum of document level allowance amounts (BT-92) where the VAT category code (BT-151, BT-102, BT-95) is "Standard rated" and the VAT rate (BT-152, BT-103, BT-96) equals the VAT category rate (BT-119).
- `warn` **PEPPOL-EN16931-R120** — Invoice line net amount MUST equal (Invoiced quantity * (Item net price/item price base quantity) + Sum of invoice line charge amount - sum of invoice line allowance amount
- `error` **BR-DE-15** — [BR-DE-15] Das Element "Buyer reference" (BT-10) muss übermittelt werden.
- `warn` **BR-DE-17** — [BR-DE-17] Mit dem Element "Invoice type code" (BT-3) sollen ausschließlich folgende Codes aus der Codeliste UNTDID 1001 übermittelt werden: 326 (Partial invoice), 380 (Commercial invoice), 384 (Corrected invoice), 389 (Self-billed invoice) und 381 (Credit note),875 (Partial construction invoice), 876 (Partial final construction invoice), 877 (Final construction invoice).
- `error` **BR-DE-2** — [BR-DE-2] Die Gruppe "SELLER CONTACT" (BG-6) muss übermittelt werden.

## config-cases/instances/processing-valid/ubl002.xml — NON_CONFORMANT
- `error` **BR-06** — [BR-06]-An Invoice shall contain the Seller name (BT-27).
- `warn` **UBL-CR-001** — [UBL-CR-001]-A UBL invoice should not include extensions

## config-cases/instances/processing-valid/ubl003.xml — NON_CONFORMANT
- `error` **BR-09** — [BR-09]-The Seller postal address (BG-5) shall contain a Seller country code (BT-40).

## config-cases/instances/processing-valid/ubl004.xml — NON_CONFORMANT
- `error` **BR-DE-3** — [BR-DE-3] Das Element "Seller city" (BT-37) muss übermittelt werden.

## config-cases/instances/processing-valid/ubl005.xml — NON_CONFORMANT
- `error` **schema-violation** — cvc-complex-type.2.4.a: Ungültiger Content wurde beginnend mit Element '{"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":DueDate}' gefunden. '{"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":CopyIndicator, "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":UUID, "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2":IssueDate}' wird erwartet.
- `error` **BR-03** — [BR-03]-An Invoice shall have an Invoice issue date (BT-2).

## config-cases/instances/processing-valid/ubl006.xml — FAILED@DETECT_SCENARIOS
- `error` **no-scenario-matched** — None of the configured scenarios matches the document

## config-cases/instances/processing-valid/ubl008.xml — NON_CONFORMANT
- `error` **BR-S-01** — [BR-S-01]-An Invoice that contains an Invoice line (BG-25), a Document level allowance (BG-20) or a Document level charge (BG-21) where the VAT category code (BT-151, BT-95 or BT-102) is "Standard rated" shall contain in the VAT breakdown (BG-23) at least one VAT category code (BT-118) equal with "Standard rated".
- `error` **BR-S-08** — [BR-S-08]-For each different value of VAT category rate (BT-119) where the VAT category code (BT-118) is "Standard rated", the VAT category taxable amount (BT-116) in a VAT breakdown (BG-23) shall equal the sum of Invoice line net amounts (BT-131) plus the sum of document level charge amounts (BT-99) minus the sum of document level allowance amounts (BT-92) where the VAT category code (BT-151, BT-102, BT-95) is "Standard rated" and the VAT rate (BT-152, BT-103, BT-96) equals the VAT category rate (BT-119).
- `error` **BR-S-08** — [BR-S-08]-For each different value of VAT category rate (BT-119) where the VAT category code (BT-118) is "Standard rated", the VAT category taxable amount (BT-116) in a VAT breakdown (BG-23) shall equal the sum of Invoice line net amounts (BT-131) plus the sum of document level charge amounts (BT-99) minus the sum of document level allowance amounts (BT-92) where the VAT category code (BT-151, BT-102, BT-95) is "Standard rated" and the VAT rate (BT-152, BT-103, BT-96) equals the VAT category rate (BT-119).
- `error` **BR-CO-10** — [BR-CO-10]-Sum of Invoice line net amount (BT-106) = Σ Invoice line net amount (BT-131).
- `error` **BR-22** — [BR-22]-Each Invoice line (BG-25) shall have an Invoiced quantity (BT-129).
- `error` **BR-23** — [BR-23]-An Invoice line (BG-25) shall have an Invoiced quantity unit of measure code (BT-130).
- `error` **BR-24** — [BR-24]-Each Invoice line (BG-25) shall have an Invoice line net amount (BT-131).
- `error` **BR-25** — [BR-25]-Each Invoice line (BG-25) shall contain the Item name (BT-153).
- `error` **BR-26** — [BR-26]-Each Invoice line (BG-25) shall contain the Item net price (BT-146).
- `error` **BR-27** — [BR-27]-The Item net price (BT-146) shall NOT be negative.
- `error` **BR-CO-04** — [BR-CO-04]-Each Invoice line (BG-25) shall be categorized with an Invoiced item VAT category code (BT-151).
- `error` **UBL-SR-48** — [UBL-SR-48]-Invoice lines shall have one and only one classified tax category.
- `error` **BR-CL-01** — [BR-CL-01]-The document type code MUST be coded by the invoice and credit note related code lists of UNTDID 1001.
- `error` **BR-DE-15** — [BR-DE-15] Das Element "Buyer reference" (BT-10) muss übermittelt werden.

## config-cases/integration/cii-bt-20-cardinality-check-2.xml — NON_CONFORMANT
- `error` **CII-SR-452** — [CII-SR-452] - Only one SpecifiedTradePaymentTerms should be present
- `error` **CII-SR-453** — [CII-SR-453] - Only one SpecifiedTradePaymentTerms Description should be present

## config-cases/integration/cii-bt-20-cardinality-check.xml — NON_CONFORMANT
- `error` **CII-SR-453** — [CII-SR-453] - Only one SpecifiedTradePaymentTerms Description should be present

## config-cases/integration/cii-cii-sr-454-negative-test.xml — NON_CONFORMANT
- `error` **CII-SR-454** — [CII-SR-454] - Only one ApplicableTradeTax should be present

## config-cases/integration/cii-cii-sr-465-negative-test.xml — NON_CONFORMANT
- `error` **CII-SR-465** — [CII-SR-465] - Only one BT-41 element is allowed on an invoice.

## config-cases/integration/cii-cii-sr-466-negative-test.xml — NON_CONFORMANT
- `error` **CII-SR-466** — [CII-SR-466] - Only one BT-56 element is allowed on an invoice.

## config-cases/integration/ubl-cr-646-sub-invoice-lines-cius.xml — NON_CONFORMANT
- `error` **UBL-CR-646** — [UBL-CR-646]-A UBL invoice should not include the InvoiceLine SubInvoiceLine

## config-cases/integration/XRechnung-CEFACT-2020-12-17-with-invalid-attachment-without-extension-urn.xml — NON_CONFORMANT
- `error` **BR-CL-24** — [BR-CL-24]-For Mime code in attribute use MIMEMediaType.

## config-cases/integration/XRechnung-CEFACT-2020-12-17-with-xml-attachment-without-extension-urn.xml — NON_CONFORMANT
- `error` **BR-CL-24** — [BR-CL-24]-For Mime code in attribute use MIMEMediaType.

## config-cases/unexpected/cii-br-co-15-negative-test.xml — NON_CONFORMANT
- `error` **BR-53** — [BR-53]-If the VAT accounting currency code (BT-6) is present, then the Invoice total VAT amount in accounting currency (BT-111) shall be provided.
- `error` **PEPPOL-EN16931-R005** — VAT accounting currency code MUST be different from invoice currency code when provided.
- `error` **PEPPOL-EN16931-R054** — Only one tax total amount must be provided where currency id equals tax currency code, if tax currency code (BT-6) is provided.
