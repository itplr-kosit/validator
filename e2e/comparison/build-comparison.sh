#!/usr/bin/env bash
# Erzeugt comparison.md: Instanz-für-Instanz-Vergleich Validator 1.6 vs. kanonische Pipeline 2.0.
# 1.6-Quelle: v1_6/reports/**/<name>-report.xml  (varl-Report: rep:accept/rep:reject + rep:message code/level)
# 2.0-Quelle: v2_0/reports/**/<name>-report.md   (Per-Instanz-Report des XRechnungE2ERunner)
set -uo pipefail
cd "$(dirname "$0")"

OUT=comparison.md
{
  echo "# Vergleich Validator 1.6 vs. kanonische Pipeline 2.0 (Steps 2–8)"
  echo
  echo "Erzeugt: $(date -Iseconds) · Inputs: \`input/\` (Szenarien, Repository, 86 Instanzen) · Outputs: \`v1_6/reports/\`, \`v2_0/reports/\`"
  echo
  echo "Vergleichslogik: 1.6-Verdikt = \`rep:accept\`/\`rep:reject\` aus dem varl-Report; 1.6-Findings = \`rep:message\` (code@level)."
  echo "2.0-Verdikt = Conformance über alle RuleSets; 2.0-Findings = Detections aus APPLY_RULES (code@severity, ohne Pipeline-INFO-Trace)."
  echo
  echo "| Instanz | 1.6 Verdikt | 2.0 Verdikt | Match | 1.6 Findings (code@level) | 2.0 Findings (code@severity) |"
  echo "|---|---|---|---|---|---|"
} > "$OUT"

same=0; diff=0
while IFS= read -r xml; do
  rel="${xml#v1_6/reports/}"                       # z.B. business-cases/standard/01.01a-INVOICE_ubl-report.xml
  name="${rel%-report.xml}"
  md="v2_0/reports/${name}-report.md"

  # --- 1.6 ---
  if grep -q "<rep:accept>" "$xml"; then v16="ACCEPT"; else v16="REJECT"; fi
  f16=$(grep -o '<rep:message[^>]*' "$xml" \
        | sed -E 's/.*level="([^"]*)".*code="([^"]*)".*/\2@\1/; t; s/.*code="([^"]*)".*level="([^"]*)".*/\1@\2/' \
        | grep -v '^<rep:message' | sort | uniq -c | awk '{print $2"×"$1}' | paste -sd' ' -)

  # --- 2.0 ---
  if [ -f "$md" ]; then
    v20=$(grep -m1 '^\- \*\*Ergebnis\*\*' "$md" | sed 's/.*: //')
    # Findings = Detection-Zeilen mit Regel-Codes (alles außer Pipeline-Trace-Codes)
    f20=$(grep -E '^\| (info|warning|error|fatal-error) \|' "$md" \
          | awk -F'|' '{gsub(/[ `]/,"",$3); gsub(/ /,"",$2); print $3"@"$2}' \
          | grep -vE '^(document-parsed|scenario-matched|scenario-user-selected|scenario-selected|artifacts-retrieved|rule-compiled|rule-precompiled|rules-applied|target-conformant|target-non-conformant|step-skipped)@' \
          | sort | uniq -c | awk '{print $2"×"$1}' | paste -sd' ' -)
  else
    v20="MISSING"; f20="-"
  fi

  # --- Match: accept↔conformant, Findings-Mengen (nur Codes) gleich? ---
  c16=$(echo "$f16" | tr ' ' '\n' | sed 's/@.*//' | sort | paste -sd' ' -)
  c20=$(echo "$f20" | tr ' ' '\n' | sed 's/@.*//;s/×.*//' | sort | paste -sd' ' -)
  verdictmatch="NO"
  { [ "$v16" = "ACCEPT" ] && [ "$v20" = "CONFORMANT" ]; } && verdictmatch="yes"
  { [ "$v16" = "REJECT" ] && [ "$v20" = "NON_CONFORMANT" ]; } && verdictmatch="yes"
  if [ "$verdictmatch" = "yes" ]; then same=$((same+1)); match="✅"; else diff=$((diff+1)); match="❌"; fi

  echo "| $name | $v16 | $v20 | $match | ${f16:--} | ${f20:--} |" >> "$OUT"
done < <(find v1_6/reports -name "*-report.xml" | sort)

{
  echo
  echo "## Bilanz"
  echo
  echo "- Verdikt identisch: **$same**"
  echo "- Verdikt abweichend: **$diff**"
} >> "$OUT"
echo "written: $OUT (same=$same, diff=$diff)"
