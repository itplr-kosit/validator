# Vergleich Validator 1.6 vs. kanonische Pipeline 2.0 (Steps 2–8)

Erzeugt: 2026-08-20T13:57:02+02:00 · Inputs: `input/` (Szenarien, Repository, 86 Instanzen) · Outputs: `v1_6/reports/`, `v2_0/reports/`

Vergleichslogik: 1.6-Verdikt = `rep:accept`/`rep:reject` aus dem varl-Report; 1.6-Findings = `rep:message` (code@level).
2.0-Verdikt = Conformance über alle RuleSets; 2.0-Findings = Detections aus APPLY_RULES (code@severity, ohne Pipeline-INFO-Trace).

| Instanz | 1.6 Verdikt | 2.0 Verdikt | Match | 1.6 Findings (code@level) | 2.0 Findings (code@severity) |
|---|---|---|---|---|---|
| business-cases/extension/04.01a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | UBL-CR-646@warning×1 | - |
| business-cases/extension/04.02a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | UBL-CR-646@warning×1 | - |
| business-cases/extension/04.03a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | UBL-CR-646@warning×1 | - |
| business-cases/extension/04.04a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 UBL-CR-646@warning×1 | BR-DE-TMP-32@info×1 |
| business-cases/extension/04.05a-INVOICE_uncefact | ACCEPT | NON_CONFORMANT | ❌ | BR-CL-10@error×3 BR-CL-21@error×1 | BR-CL-10@fatal-error×3 BR-CL-21@fatal-error×1 |
| business-cases/extension/05.01a-INVOICE_ubl | ACCEPT | NON_CONFORMANT | ❌ | BR-CO-16@error×1 BR-DE-TMP-32@information×1 UBL-CR-470@warning×1 | BR-CO-16@fatal-error×1 BR-DE-TMP-32@info×1 |
| business-cases/standard/01.01a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.01a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.02a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.02a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.03a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.03a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.04a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.04a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.05a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.05a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.06a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.06a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.07a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.07a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.08a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.08a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.09a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.09a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.10a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.10a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.11a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.11a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.12a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.12a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.13a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.13a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.14a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.14a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/01.15a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.15a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.17a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/01.17a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
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
| business-cases/standard/02.05a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/02.05a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/02.06a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/02.06a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/03.01a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.01a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.02a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/03.02a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/03.03a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/03.03a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| business-cases/standard/03.04a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.04a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.05a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.05a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.06a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.06a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.07a-INVOICE_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| business-cases/standard/03.07a-INVOICE_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.01_comprehensive_test_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.01_comprehensive_test_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.02_comprehensive_test_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.02_comprehensive_test_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.03_comprehensive_test_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.03_comprehensive_test_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.04_comprehensive_test_ubl | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.04_comprehensive_test_uncefact | ACCEPT | CONFORMANT | ✅ | - | - |
| technical-cases/cius/01.05_minimal_test_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| technical-cases/cius/01.05_minimal_test_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| technical-cases/cius/01.06_minimal_test_ubl | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| technical-cases/cius/01.06_minimal_test_uncefact | ACCEPT | CONFORMANT | ✅ | BR-DE-TMP-32@information×1 | BR-DE-TMP-32@info×1 |
| technical-cases/cvd/02.01a-cvd_INVOICE_ubl | ACCEPT | NON_CONFORMANT | ❌ | BR-CL-13@error×1 | BR-CL-13@fatal-error×1 |
| technical-cases/cvd/02.01a-cvd_INVOICE_uncefact | ACCEPT | NON_CONFORMANT | ❌ | BR-CL-13@error×1 | BR-CL-13@fatal-error×1 |

## Bilanz

- Verdikt identisch: **82**
- Verdikt abweichend: **4**
