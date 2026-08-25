# XRechnung E2E — Findings je Instanz (alle Nicht-INFO-Detections)

## business-cases/extension/04.01a-INVOICE_ubl.xml — CONFORMANT
- `warn` **UBL-CR-646** — [UBL-CR-646]-A UBL invoice should not include the InvoiceLine SubInvoiceLine (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1])

## business-cases/extension/04.02a-INVOICE_ubl.xml — CONFORMANT
- `warn` **UBL-CR-646** — [UBL-CR-646]-A UBL invoice should not include the InvoiceLine SubInvoiceLine (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1])

## business-cases/extension/04.03a-INVOICE_ubl.xml — CONFORMANT
- `warn` **UBL-CR-646** — [UBL-CR-646]-A UBL invoice should not include the InvoiceLine SubInvoiceLine (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1])

## business-cases/extension/04.04a-INVOICE_ubl.xml — CONFORMANT
- `warn` **UBL-CR-646** — [UBL-CR-646]-A UBL invoice should not include the InvoiceLine SubInvoiceLine (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1])

## business-cases/extension/04.05a-INVOICE_uncefact.xml — NON_CONFORMANT
- `error` **BR-CL-21** — [BR-CL-21]-Item standard identifier scheme identifier MUST belong to the ISO 6523 ICD
      code list (at /Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}CrossIndustryInvoice[1]/Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}SupplyChainTradeTransaction[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}IncludedSupplyChainTradeLineItem[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}SpecifiedTradeProduct[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}GlobalID[1])
- `error` **BR-CL-10** — [BR-CL-10]-Any identifier identification scheme identifier MUST be coded using one of the ISO 6523 ICD list. (at /Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}CrossIndustryInvoice[1]/Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}SupplyChainTradeTransaction[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}ApplicableHeaderTradeAgreement[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}SellerTradeParty[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}GlobalID[1])
- `error` **BR-CL-10** — [BR-CL-10]-Any identifier identification scheme identifier MUST be coded using one of the ISO 6523 ICD list. (at /Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}CrossIndustryInvoice[1]/Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}SupplyChainTradeTransaction[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}ApplicableHeaderTradeAgreement[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}BuyerTradeParty[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}GlobalID[1])
- `error` **BR-CL-10** — [BR-CL-10]-Any identifier identification scheme identifier MUST be coded using one of the ISO 6523 ICD list. (at /Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}CrossIndustryInvoice[1]/Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}SupplyChainTradeTransaction[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}ApplicableHeaderTradeSettlement[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}PayeeTradeParty[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}GlobalID[1])

## business-cases/extension/05.01a-INVOICE_ubl.xml — NON_CONFORMANT
- `error` **BR-CO-16** — [BR-CO-16]-Amount due for payment (BT-115) = Invoice total amount with VAT (BT-112) -Paid amount (BT-113) +Rounding amount (BT-114). (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}LegalMonetaryTotal[1])
- `warn` **UBL-CR-470** — [UBL-CR-470]-A UBL invoice should not include the PrepaidPayment (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1])

## technical-cases/cvd/02.01a-cvd_INVOICE_ubl.xml — NON_CONFORMANT
- `error` **BR-CL-13** — [BR-CL-13]-Item classification identifier identification scheme identifier MUST be
      coded using one of the UNTDID 7143 list. (at /Q{urn:oasis:names:specification:ubl:schema:xsd:Invoice-2}Invoice[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}InvoiceLine[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}Item[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}CommodityClassification[1]/Q{urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2}ItemClassificationCode[1])

## technical-cases/cvd/02.01a-cvd_INVOICE_uncefact.xml — NON_CONFORMANT
- `error` **BR-CL-13** — [BR-CL-13]-Item classification identifier identification scheme identifier MUST be coded using one of the UNTDID 7143 list. (at /Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}CrossIndustryInvoice[1]/Q{urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100}SupplyChainTradeTransaction[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}IncludedSupplyChainTradeLineItem[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}SpecifiedTradeProduct[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}DesignatedProductClassification[1]/Q{urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100}ClassCode[1])
