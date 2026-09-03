# Vergleich Validator 1.6 vs. kanonische Pipeline 2.0 (Steps 2–8)

Erzeugt: 2026-09-03T10:34:50+02:00 · Inputs: `input/` (Szenarien, Repository, 150 Instanzen) · Outputs: `v1_6/reports/`, `v2_0/reports/`

Vergleichslogik: 1.6-Verdikt = `rep:accept`/`rep:reject` aus dem varl-Report; 1.6-Findings = `rep:message` (code@level).
2.0-Verdikt = Conformance über alle RuleSets; 2.0-Findings = Detections aus APPLY_RULES (code@severity, ohne Pipeline-INFO-Trace).

| Instanz | 1.6 Verdikt | 2.0 Verdikt | Match | 1.6 Findings (code@level) | 2.0 Findings (code@severity) |
|---|---|---|---|---|---|
| business-cases/extension/04.01a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | UBL-CR-646@warning×1 | - |
| business-cases/extension/04.02a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | UBL-CR-646@warning×1 | - |
| business-cases/extension/04.03a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | UBL-CR-646@warning×1 | - |
| business-cases/extension/04.04a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 UBL-CR-646@warning×1 | - |
| business-cases/extension/04.05a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-CL-10@error×3 BR-CL-21@error×1 | - |
| business-cases/extension/05.01a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-CO-16@error×1 BR-DE-TMP-32@information×1 UBL-CR-470@warning×1 | - |
| business-cases/standard/01.01a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.01a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.02a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.02a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.03a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.03a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.04a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.04a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.05a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.05a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.06a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.06a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.07a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.07a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.08a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.08a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.09a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.09a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.10a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.10a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.11a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.11a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.12a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.12a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.13a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.13a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.14a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.14a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.15a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.15a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.17a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.17a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/01.18a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.18a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.19a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.19a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.20a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.20a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.21a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.21a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/02.01a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/02.01a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/02.02a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/02.02a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/02.03a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/02.03a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/02.04a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/02.04a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/02.05a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/02.05a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/02.06a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/02.06a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/03.01a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.01a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.02a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/03.02a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/03.03a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/03.03a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| business-cases/standard/03.04a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.04a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.05a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.05a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.06a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.06a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.07a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.07a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/XRechnung-CEFACT-2020-12-17-with-invalid-attachment-without-extension-urn | REJECT | NON_CONFORMANT | ✅ | BR-CL-24@error×1 | BR-CL-24@error×1 |
| config-cases/cen-unit-test/XRechnung-CEFACT-2020-12-17-with-xml-attachment-and-extension-urn | ACCEPT | CONFORMANT | ✅ | BR-CL-24@error×1 | - |
| config-cases/cen-unit-test/XRechnung-CEFACT-2020-12-17-with-xml-attachment-without-extension-urn | REJECT | NON_CONFORMANT | ✅ | BR-CL-24@error×1 | BR-CL-24@error×1 |
| config-cases/cen-unit-test/cii-br-53-test-1 | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/cii-br-53-test-2 | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/cii-br-65-test | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/cii-br-ae-02-and-br-ae-03-with-bt-32 | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/cii-br-ae-02-and-br-ae-04-with-bt-32 | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/cii-br-co-17-and-br-s-09-rounding | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/cii-br-dec-23-test | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/cii-br-e-10-test-2 | ACCEPT | CONFORMANT | ✅ | PEPPOL-EN16931-R120@warning×1 | - |
| config-cases/cen-unit-test/cii-br-e-10-test | ACCEPT | CONFORMANT | ✅ | PEPPOL-EN16931-R120@warning×1 | - |
| config-cases/cen-unit-test/cii-br-s-08-rounding-rule | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/cii-bt-20-cardinality-check-2 | REJECT | NON_CONFORMANT | ✅ | CII-SR-452@warning×1 CII-SR-453@warning×1 | CII-SR-452@error×1 CII-SR-453@error×1 |
| config-cases/cen-unit-test/cii-bt-20-cardinality-check | REJECT | NON_CONFORMANT | ✅ | CII-SR-453@warning×1 | CII-SR-453@error×1 |
| config-cases/cen-unit-test/cii-cii-sr-454-negative-test | REJECT | NON_CONFORMANT | ✅ | CII-SR-454@warning×1 | CII-SR-454@error×1 |
| config-cases/cen-unit-test/cii-cii-sr-454-test | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/cii-cii-sr-461-bt-7-cardinality-test | REJECT | NON_CONFORMANT | ✅ | CII-SR-461@error×1 | CII-SR-461@error×1 |
| config-cases/cen-unit-test/cii-cii-sr-462-bt-8-cardinality-test | REJECT | NON_CONFORMANT | ✅ | BR-CL-06@error×1 CII-SR-462@error×1 | BR-CL-06@error×1 CII-SR-462@error×1 |
| config-cases/cen-unit-test/cii-cii-sr-465-negative-test | REJECT | NON_CONFORMANT | ✅ | CII-SR-465@warning×1 | CII-SR-465@error×1 |
| config-cases/cen-unit-test/cii-cii-sr-466-negative-test | REJECT | NON_CONFORMANT | ✅ | CII-SR-466@warning×1 | CII-SR-466@error×1 |
| config-cases/cen-unit-test/ubl-br-cl-04-alpha-three-codes-only-bt5-bt6 | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/ubl-br-cl-14-15-and-br-co-09-country-codes-kosovo | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/ubl-br-co-15-no-multiple-bt-110-allowed-test | REJECT | NON_CONFORMANT | ✅ | BR-CO-15@error×1 PEPPOL-EN16931-R053@error×1 | BR-CO-15@error×1 PEPPOL-EN16931-R053@error×1 |
| config-cases/cen-unit-test/ubl-br-co-17-and-br-s-09-rounding | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/ubl-br-de-17-br-cl-01-construction-codes | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/ubl-br-dec-23-not-more-than-2-decimal-places-bt131 | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/cen-unit-test/ubl-br-o-02-and-br-o-03-with-bt-31 | REJECT | NON_CONFORMANT | ✅ | BR-O-02@error×1 BR-O-03@error×1 UBL-SR-53@error×1 | BR-O-02@error×1 BR-O-03@error×1 UBL-SR-53@error×1 |
| config-cases/cen-unit-test/ubl-br-o-02-and-br-o-03-with-bt-63 | REJECT | NON_CONFORMANT | ✅ | BR-O-02@error×1 BR-O-03@error×1 UBL-SR-53@error×2 | BR-O-02@error×1 BR-O-03@error×1 UBL-SR-53@error×2 |
| config-cases/cen-unit-test/ubl-br-o-02-and-br-o-04-with-bt-31 | REJECT | NON_CONFORMANT | ✅ | BR-DE-1@error×1 BR-O-02@error×1 BR-O-04@error×1 UBL-SR-53@error×1 | BR-DE-1@error×1 BR-O-02@error×1 BR-O-04@error×1 UBL-SR-53@error×1 |
| config-cases/cen-unit-test/ubl-br-o-02-and-br-o-04-with-bt-63 | REJECT | NON_CONFORMANT | ✅ | BR-O-02@error×1 BR-O-04@error×1 UBL-SR-53@error×2 | BR-O-02@error×1 BR-O-04@error×1 UBL-SR-53@error×2 |
| config-cases/instances/processing-error/bin001 | REJECT | FAILED@PARSE_DOCUMENT | ✅ | generic-error@error×1 | not-wellformed@error×1 |
| config-cases/instances/processing-error/ubl007 | REJECT | FAILED@PARSE_DOCUMENT | ✅ | generic-error@error×1 | not-wellformed@error×1 |
| config-cases/instances/processing-valid/cii001 | REJECT | NON_CONFORMANT | ✅ | BR-DE-15@error×1 BR-DE-17@warning×1 BR-DE-2@error×1 BR-DE-TMP-32@information×1 BR-S-08@error×1 PEPPOL-EN16931-R120@warning×1 | BR-DE-15@error×1 BR-DE-2@error×1 BR-S-08@error×1 |
| config-cases/instances/processing-valid/ubl-br-dex-09-prepaidpayment | ACCEPT | CONFORMANT | ✅ | BR-CO-16@error×1 UBL-CR-470@warning×1 | - |
| config-cases/instances/processing-valid/ubl001 | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| config-cases/instances/processing-valid/ubl002 | REJECT | NON_CONFORMANT | ✅ | BR-06@error×1 BR-DE-TMP-32@information×1 UBL-CR-001@warning×1 | BR-06@error×1 |
| config-cases/instances/processing-valid/ubl003 | REJECT | NON_CONFORMANT | ✅ | BR-09@error×1 BR-DE-TMP-32@information×1 | BR-09@error×1 |
| config-cases/instances/processing-valid/ubl004 | REJECT | NON_CONFORMANT | ✅ | BR-DE-3@error×1 BR-DE-TMP-32@information×1 | BR-DE-3@error×1 |
| config-cases/instances/processing-valid/ubl005 | REJECT | NON_CONFORMANT | ✅ | cvc-complex-type.2.4.a@error×1 | BR-03@error×1 schema-violation@error×1 |
| config-cases/instances/processing-valid/ubl006 | REJECT | FAILED@DETECT_SCENARIOS | ✅ | - | no-scenario-matched@error×1 |
| config-cases/instances/processing-valid/ubl008 | REJECT | NON_CONFORMANT | ✅ | BR-22@error×1 BR-23@error×1 BR-24@error×1 BR-25@error×1 BR-26@error×1 BR-27@error×1 BR-CL-01@error×1 BR-CO-04@error×1 BR-CO-10@error×1 BR-DE-15@error×1 BR-DE-TMP-32@information×1 BR-S-01@error×1 BR-S-08@error×2 UBL-SR-48@error×1 | BR-22@error×1 BR-23@error×1 BR-24@error×1 BR-25@error×1 BR-26@error×1 BR-27@error×1 BR-CL-01@error×1 BR-CO-04@error×1 BR-CO-10@error×1 BR-DE-15@error×1 BR-S-01@error×1 BR-S-08@error×2 UBL-SR-48@error×1 |
| config-cases/integration/XRechnung-CEFACT-2020-12-17-with-invalid-attachment-without-extension-urn | REJECT | NON_CONFORMANT | ✅ | BR-CL-24@error×1 | BR-CL-24@error×1 |
| config-cases/integration/XRechnung-CEFACT-2020-12-17-with-xml-attachment-and-extension-urn | ACCEPT | CONFORMANT | ✅ | BR-CL-24@error×1 | - |
| config-cases/integration/XRechnung-CEFACT-2020-12-17-with-xml-attachment-without-extension-urn | REJECT | NON_CONFORMANT | ✅ | BR-CL-24@error×1 | BR-CL-24@error×1 |
| config-cases/integration/cii-br-dex-01-extension-mime-code | ACCEPT | CONFORMANT | ✅ | BR-CL-24@error×1 BR-DE-TMP-32@information×1 | - |
| config-cases/integration/cii-br-s-08-rounding-rule | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/integration/cii-bt-20-cardinality-check-2 | REJECT | NON_CONFORMANT | ✅ | CII-SR-452@warning×1 CII-SR-453@warning×1 | CII-SR-452@error×1 CII-SR-453@error×1 |
| config-cases/integration/cii-bt-20-cardinality-check | REJECT | NON_CONFORMANT | ✅ | CII-SR-453@warning×1 | CII-SR-453@error×1 |
| config-cases/integration/cii-cii-sr-454-negative-test | REJECT | NON_CONFORMANT | ✅ | CII-SR-454@warning×1 | CII-SR-454@error×1 |
| config-cases/integration/cii-cii-sr-465-negative-test | REJECT | NON_CONFORMANT | ✅ | CII-SR-465@warning×1 | CII-SR-465@error×1 |
| config-cases/integration/cii-cii-sr-466-negative-test | REJECT | NON_CONFORMANT | ✅ | CII-SR-466@warning×1 | CII-SR-466@error×1 |
| config-cases/integration/cvd-br-tmp-cvd-01-code | ACCEPT | CONFORMANT | ✅ | BR-CL-13@error×1 BR-DE-TMP-32@information×1 | - |
| config-cases/integration/diga-cii-extension-codes-2 | ACCEPT | CONFORMANT | ✅ | BR-CL-10@error×3 BR-CL-21@error×1 BR-CL-25@error×2 BR-CL-26@error×1 | - |
| config-cases/integration/diga-cii-extension-codes | ACCEPT | CONFORMANT | ✅ | BR-CL-11@error×3 BR-CL-21@error×1 | - |
| config-cases/integration/ubl-br-dex-01-extension-mime-code | ACCEPT | CONFORMANT | ✅ | BR-CL-24@error×1 BR-DE-TMP-32@information×1 | - |
| config-cases/integration/ubl-cr-646-sub-invoice-lines-cius | REJECT | NON_CONFORMANT | ✅ | UBL-CR-646@warning×1 | UBL-CR-646@error×1 |
| config-cases/integration/ubl-cr-646-sub-invoice-lines-extension | ACCEPT | CONFORMANT | ✅ | UBL-CR-646@warning×1 | - |
| config-cases/unexpected/cii-br-03-wrong-date-format | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/unexpected/cii-br-co-15-negative-test | REJECT | NON_CONFORMANT | ✅ | BR-53@error×1 PEPPOL-EN16931-R005@error×1 PEPPOL-EN16931-R054@error×1 | BR-53@error×1 PEPPOL-EN16931-R005@error×1 PEPPOL-EN16931-R054@error×1 |
| config-cases/unexpected/cii-br-co-23-reason-codes | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/unexpected/cii-br-co-25 | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/unexpected/cii-sr-030-wrong-cardinality-bt-22 | ACCEPT | CONFORMANT | ✅ | - | - |
| config-cases/unexpected/ubl-br-cl-14-15-and-br-co-09-country-codes-kosovo | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.01_comprehensive_test_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.01_comprehensive_test_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.02_comprehensive_test_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.02_comprehensive_test_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.03_comprehensive_test_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.03_comprehensive_test_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.04_comprehensive_test_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.04_comprehensive_test_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.05_minimal_test_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| technical-cases/cius/01.05_minimal_test_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| technical-cases/cius/01.06_minimal_test_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| technical-cases/cius/01.06_minimal_test_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | - |
| technical-cases/cvd/02.01a-cvd_INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-CL-13@error×1 | - |
| technical-cases/cvd/02.01a-cvd_INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-CL-13@error×1 | - |

## Bilanz

- Verdikt identisch: **150**
- Verdikt abweichend: **0**
