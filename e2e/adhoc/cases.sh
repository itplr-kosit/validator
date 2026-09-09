#!/usr/bin/env bash
# Local test cases for the ad hoc validation (CLI -S and POST /api/validation/adhoc).
#
# Run from the validator root after `mvn install` (Git Bash on Windows works):
#     bash e2e/adhoc/cases.sh                # CLI cases; server cases if a server answers on $BASE
#     BASE=http://localhost:8080 bash e2e/adhoc/cases.sh
#
# Start the server for the S-cases from the server module, so that its default configuration resolves:
#     (cd server && java -jar target/validator-server-2.0.0-SNAPSHOT-runner.jar)
#
# Every case prints PASS or FAIL with what was expected; the exit code of the script is the number of failures.
# The cases and their rationale are described in e2e/adhoc/README.md.

set -u
cd "$(dirname "$0")/../.."

JAR=$(ls cli/target/validator-cli-*-standalone.jar 2>/dev/null | head -1)
T=test-data/src/main/resources/examples/simple
X=e2e/comparison/input
RULES=e2e/adhoc/rules
OUT=${OUT:-/tmp/adhoc-out}
BASE=${BASE:-http://localhost:8080}
fail=0

mkdir -p "$OUT"

check() { # name, expected, actual
    if [ "$2" = "$3" ]; then echo "PASS  $1"; else echo "FAIL  $1 — expected: $2, actual: $3"; fail=$((fail + 1)); fi
}

# exit codes of the CLI: 0 acceptable, n = documents not acceptable, -2 configuration error (shown as 254 or 127 by the shell)
cli() { java -jar "$JAR" "$@" >"$OUT/cli.out" 2>"$OUT/cli.err"; echo $?; }
cvr() { grep -o "$2" "$1" | sort -u | tr '\n' ' ' | sed 's/ $//'; }

if [ -z "$JAR" ]; then echo "no CLI jar in cli/target - run mvn install first"; exit 1; fi

echo "== CLI (-S) =="
check "A1 .sch, conformant document -> exit 0"            0 "$(cli -S $T/repository/simple.sch -o "$OUT" $T/input/simple.xml)"
check "A1 report names the rule set as scenario"          'scenario-id="simple.sch"' "$(cvr "$OUT/simple-report.xml" 'scenario-id="[^"]*"')"
check "A1 report decision"                                'cvr:decision="ACCEPT"' "$(cvr "$OUT/simple-report.xml" 'cvr:decision="[^"]*"')"
check "A2 .sch, document with findings -> exit 1"         1 "$(cli -S $T/repository/simple.sch -o "$OUT" $T/input/simple-schematron-invalid.xml)"
check "A2 report decision"                                'cvr:decision="REJECT"' "$(cvr "$OUT/simple-schematron-invalid-report.xml" 'cvr:decision="[^"]*"')"
check "A3 precompiled .xsl instead of .sch -> exit 0"     0 "$(cli -S $T/repository/simple.xsl -o "$OUT" $T/input/simple.xml)"
check "A4 rule set with runtime error -> exit 1, cancelled at apply-rules" \
      'cvr:status="CANCELLED"' "$(cli -S $T/repository/simple-runtime-error.sch -o "$OUT" $T/input/simple.xml >/dev/null; cvr "$OUT/simple-report.xml" 'cvr:status="[^"]*"')"
check "A5 rule set that does not compile -> exit 1, rule-prepare-error" \
      'code="rule-prepare-error"' "$(cli -S $T/repository/does-not-compile.sch -o "$OUT" $T/input/simple.xml >/dev/null; cvr "$OUT/simple-report.xml" 'code="rule-prepare-error"')"
check "A6 not well-formed document -> exit 1"             1 "$(cli -S $T/repository/simple.sch -o "$OUT" $T/input/simple-not-wellformed.xml)"
check "A7 -s and -S together -> configuration error"     "not both" "$(cli -s $T/scenarios.xml -S $T/repository/simple.sch $T/input/simple.xml >/dev/null; grep -o 'not both' "$OUT/cli.err" | head -1)"
check "A8 -r is the repository root of the artifacts -> exit 0" 0 "$(cli -S $T/repository/simple.sch -r $T/repository -o "$OUT" $T/input/simple.xml)"
check "A8 an artifact outside -r -> configuration error"  "outside" "$(cli -S $T/repository/simple.sch -r $RULES $T/input/simple.xml >/dev/null; grep -o 'outside' "$OUT/cli.err" | head -1)"
check "A9 missing artifact file -> configuration error"   "Not a valid path" "$(cli -S /nowhere/x.sch $T/input/simple.xml >/dev/null; grep -o 'Not a valid path' "$OUT/cli.err" | head -1)"
check "A10 XRechnung: precompiled XSL against a corpus instance -> exit 0" \
      0 "$(cli -S $X/repository/resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl -o "$OUT" $X/instances/business-cases/standard/01.01a-INVOICE_ubl.xml)"
check "A11 .sch with include: resolved next to the file, rule fires (foo.xml rejected)" \
      1 "$(cli -S $RULES/with-include.sch -o "$OUT" $T/input/foo.xml)"
check "A11 .sch with include: simple.xml accepted"        0 "$(cli -S $RULES/with-include.sch -o "$OUT" $T/input/simple.xml)"
check "A12 set: .xsd + .sch, schema-invalid document -> exit 1" \
      1 "$(cli -S $T/repository/simple.xsd -S $T/repository/simple.sch -o "$OUT" $T/input/simple-schema-invalid.xml)"
check "A12 set: one scenario named after both, schema violation reported" \
      'code="schema-violation" scenario-id="simple.xsd, simple.sch"' "$(cvr "$OUT/simple-schema-invalid-report.xml" 'scenario-id="[^"]*"\|code="schema-violation"')"
check "A12 set: conformant document -> exit 0"            0 "$(cli -S $T/repository/simple.xsd -S $T/repository/simple.sch -o "$OUT" $T/input/simple.xml)"
check "A13 artifact of unknown kind (.txt) -> configuration error" "Unsupported artifact" "$(cli -S $T/repository/some.txt $T/input/simple.xml >/dev/null; grep -o 'Unsupported artifact' "$OUT/cli.err" | head -1)"

echo
if ! curl -s -o /dev/null -w '%{http_code}' "$BASE/q/health/ready" 2>/dev/null | grep -q 200; then
    echo "== server (POST /api/validation/adhoc) == skipped, nothing answers on $BASE"
    echo "   start it with: (cd server && java -jar target/validator-server-2.0.0-SNAPSHOT-runner.jar)"
    exit $fail
fi

echo "== server (POST /api/validation/adhoc on $BASE) =="
adhoc() { # document, resource... -> Location header
    local doc=$1; shift
    local parts=()
    for r in "$@"; do parts+=(-F "resource=@$r"); done
    curl -s -i -F "document=@$doc" "${parts[@]}" "$BASE/api/validation/adhoc" | grep -i '^Location' | awk '{print $2}' | tr -d '\r'
}
status() { curl -s -o /dev/null -w '%{http_code}' "$@"; }
result() { curl -s "$1" | grep -o "$2" | sort -u | tr '\n' ' ' | sed 's/ $//'; }
(cd "$RULES" && zip -q -r "$OUT/rules.zip" with-include.sch abstracts.sch) 2>/dev/null || jar cfM "$OUT/rules.zip" -C "$RULES" with-include.sch -C "$RULES" abstracts.sch

check "S1 .sch, conformant document -> 201"               201 "$(status -F document=@$T/input/simple.xml -F resource=@$T/repository/simple.sch "$BASE/api/validation/adhoc")"
loc=$(adhoc $T/input/simple-schematron-invalid.xml $T/repository/simple.sch)
check "S2 .sch, findings -> REJECT, scenario named after the file" 'cvr:decision="REJECT" scenario-id="simple.sch"' "$(result "$loc" 'cvr:decision="[^"]*"\|scenario-id="[^"]*"')"
loc=$(adhoc $T/input/simple.xml $T/repository/simple.xsl)
check "S3 precompiled .xsl -> ACCEPT, scenario simple.xsl"    'cvr:decision="ACCEPT" scenario-id="simple.xsl"' "$(result "$loc" 'cvr:decision="[^"]*"\|scenario-id="[^"]*"')"
check "S4 no artifact at all -> 400"                      400 "$(status -F document=@$T/input/simple.xml "$BASE/api/validation/adhoc")"
check "S4 no document -> 400"                             400 "$(status -F resource=@$T/repository/simple.sch "$BASE/api/validation/adhoc")"
loc=$(adhoc $T/input/simple.xml $T/repository/does-not-compile.sch)
check "S5 rule set does not compile -> run cancelled with rule-prepare-error" 'code="rule-prepare-error" cvr:status="CANCELLED"' "$(result "$loc" 'code="rule-prepare-error"\|cvr:status="[^"]*"')"
check "S6 resource that is no XSD/SCH/XSL (.txt) -> 400"  400 "$(status -F document=@$T/input/simple.xml -F resource=@$T/repository/some.txt "$BASE/api/validation/adhoc")"
loc=$(adhoc $X/instances/business-cases/standard/01.01a-INVOICE_ubl.xml $X/repository/resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl)
check "S7 XRechnung XSL against a corpus instance -> ACCEPT"  'cvr:decision="ACCEPT"' "$(result "$loc" 'cvr:decision="[^"]*"')"
loc=$(adhoc $T/input/simple.xml $RULES/with-include.sch)
check "S8 .sch with include as single resource -> rule-prepare-error (the include is not there)" 'code="rule-prepare-error"' "$(result "$loc" 'code="rule-prepare-error"')"
check "S9 the regular endpoint still answers 201"         201 "$(status -H 'Content-Type: application/xml' --data-binary @$T/input/simple.xml "$BASE/api/validation")"
loc=$(adhoc $T/input/simple-schema-invalid.xml $T/repository/simple.xsd $T/repository/simple.sch)
check "S10 set: .xsd + .sch -> one scenario, schema violation, REJECT" 'code="schema-violation" cvr:decision="REJECT" scenario-id="simple.xsd, simple.sch"' "$(result "$loc" 'scenario-id="[^"]*"\|cvr:decision="[^"]*"\|code="schema-violation"')"
loc=$(curl -s -i -F "document=@$T/input/simple.xml" -F "resource=@$T/repository/simple.sch;filename=" "$BASE/api/validation/adhoc" | grep -i '^Location' | awk '{print $2}' | tr -d '\r')
check "S11 resource without file name -> typed by root element, named resource-1.sch" 'scenario-id="resource-1.sch"' "$(result "$loc" 'scenario-id="[^"]*"')"
loc=$(curl -s -i -F "document=@$T/input/foo.xml" -F "repository=@$OUT/rules.zip" -F "artifact=with-include.sch" "$BASE/api/validation/adhoc" | grep -i '^Location' | awk '{print $2}' | tr -d '\r')
check "S12 ZIP repository: include resolved, rule fires (foo.xml) -> REJECT" 'cvr:decision="REJECT" scenario-id="with-include.sch"' "$(result "$loc" 'cvr:decision="[^"]*"\|scenario-id="[^"]*"')"
loc=$(curl -s -i -F "document=@$T/input/simple.xml" -F "repository=@$OUT/rules.zip" -F "artifact=with-include.sch" "$BASE/api/validation/adhoc" | grep -i '^Location' | awk '{print $2}' | tr -d '\r')
check "S12 ZIP repository: simple.xml -> ACCEPT"          'cvr:decision="ACCEPT"' "$(result "$loc" 'cvr:decision="[^"]*"')"
check "S13 ZIP repository, artifact not in it -> 400"     400 "$(status -F document=@$T/input/simple.xml -F repository=@$OUT/rules.zip -F artifact=missing.sch "$BASE/api/validation/adhoc")"

echo
echo "failures: $fail"
exit $fail
