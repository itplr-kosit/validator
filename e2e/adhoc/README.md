# Ad hoc validation — local test cases

Manual and scripted checks of the ad hoc validation: a document against a set of validation artifacts — XML Schemas
(`.xsd`), Schematrons (`.sch`), precompiled Schematron XSLTs (`.xsl`) — without a `scenarios.xml`. Two entry points,
one engine (`ConformanceValidation.adHoc`), so the cases come in pairs — what the CLI does with `-S`, the server does
with `POST /api/validation/adhoc`.

```
bash e2e/adhoc/cases.sh                      # from the validator root, after mvn install
(cd server && java -jar target/validator-server-2.0.0-SNAPSHOT-runner.jar)   # for the S-cases, in a second shell
```

The script prints `PASS`/`FAIL` per case and exits with the number of failures. Reports of the CLI cases land in
`/tmp/adhoc-out` (override with `OUT=…`), the server base URL is `BASE` (default `http://localhost:8080`). The S-cases
zip `e2e/adhoc/rules/` into `$OUT/rules.zip` (needs `zip` or `jar` on the path).

## Fixtures

| File | What it is |
|---|---|
| `test-data/…/simple/repository/simple.xsd` | the XML Schema of the `simple` documents |
| `test-data/…/simple/repository/simple.sch` | a Schematron that is compiled at start (`schxslt`) |
| `test-data/…/simple/repository/simple.xsl` | the same rules, precompiled — run as is |
| `test-data/…/simple/repository/simple-runtime-error.sch` | compiles, but calls `error()` while running |
| `test-data/…/simple/repository/does-not-compile.sch` | does not compile |
| `test-data/…/simple/repository/some.txt` | not XML at all |
| `e2e/adhoc/rules/with-include.sch` + `abstracts.sch` | a modular Schematron: the only rule comes from an included file |
| `e2e/comparison/input/repository/…/XRechnung-UBL-validation.xsl` | the real XRechnung rule set, precompiled |
| `test-data/…/simple/input/simple.xml`, `simple-schematron-invalid.xml`, `simple-schema-invalid.xml`, `simple-not-wellformed.xml`, `foo.xml` | documents: conformant, with rule findings, schema-invalid, not well-formed, wrong root |

## CLI cases (`-S`)

| # | Command (from the validator root, `JAR=cli/target/validator-cli-2.0.0-SNAPSHOT-standalone.jar`) | Expected |
|---|---|---|
| A1 | `java -jar $JAR -S $T/repository/simple.sch -o /tmp/adhoc-out $T/input/simple.xml` | exit 0; table `Schema -`, `Schematron Y`, `ACCEPTABLE`; `simple-report.xml` with `scenario-id="simple.sch"`, `cvr:decision="ACCEPT"` |
| A2 | … `simple-schematron-invalid.xml` | exit 1; `Schematron N`, `REJECT`; finding `content-1` in the table and the report |
| A3 | `-S $T/repository/simple.xsl` … `simple.xml` | exit 0 — a precompiled XSL is run as is, the report's `prepare-rules` step has no `rule-compiled` detection |
| A4 | `-S $T/repository/simple-runtime-error.sch` … `simple.xml` | exit 1; `Processing errors: 1`; report `cvr:status="CANCELLED"`, cancelled at `apply-rules` (`rule-engine-error`) |
| A5 | `-S $T/repository/does-not-compile.sch` … | exit 1; cancelled at `prepare-rules` (`rule-prepare-error`) |
| A6 | `-S … simple.sch` … `simple-not-wellformed.xml` | exit 1; cancelled at `parse-document` |
| A7 | `-s $T/scenarios.xml -S $T/repository/simple.sch …` | configuration error (exit −2): `Specify either --scenarios or --artifact, not both` |
| A8 | `-S $T/repository/simple.sch -r $T/repository …` | exit 0 — `-r` is the repository root of the ad hoc scenario; `-S $T/repository/simple.sch -r e2e/adhoc/rules …` is a configuration error: the artifact `lies outside the repository` |
| A9 | `-S /nowhere/x.sch …` | configuration error: `Not a valid path for artifact definition specified` |
| A10 | `-S e2e/comparison/input/repository/resources/xrechnung/3.0.2/xsl/XRechnung-UBL-validation.xsl -o /tmp/adhoc-out e2e/comparison/input/instances/business-cases/standard/01.01a-INVOICE_ubl.xml` | exit 0 — the XRechnung rules alone, without the CEN rules and the UBL schema the scenario would add; `<validator name="…"/>` lists Saxon only, no transpiler (the XSL was not compiled by the validator) |
| A11 | `-S e2e/adhoc/rules/with-include.sch -o /tmp/adhoc-out $T/input/foo.xml` and `… simple.xml` | `foo.xml` exit 1 (the included rule fires: root is not `simple`), `simple.xml` exit 0 — the include resolved in the directory of the `.sch` |
| A12 | `-S $T/repository/simple.xsd -S $T/repository/simple.sch -o /tmp/adhoc-out $T/input/simple-schema-invalid.xml` and `… simple.xml` | the set is one scenario `scenario-id="simple.xsd, simple.sch"`; the schema-invalid document exits 1 with `code="schema-violation"` (`Schema N`), `simple.xml` exits 0 with `Schema Y`, `Schematron Y` |
| A13 | `-S $T/repository/some.txt …` | configuration error: `Unsupported artifact` — the kind is decided by the extension |

`$T` is `test-data/src/main/resources/examples/simple`. Without `-s`/`-S` the CLI prints `Missing required option:
--scenarios=<scenario.xml> or --artifact=<artifact>` and the usage.

## Server cases (`POST /api/validation/adhoc`)

Multipart: `document` plus the artifacts as `resource` parts (one per file), or as a ZIP `repository` with `artifact`
parts naming the entries to apply. The answer is the same as for `POST /api/validation`.

| # | Command | Expected |
|---|---|---|
| S1 | `curl -s -i -F document=@$T/input/simple.xml -F resource=@$T/repository/simple.sch $BASE/api/validation/adhoc` | `201 Created`, `Location: …/api/validation/result/{id}`, body `{"id":…,"status":"completed","result":…}` |
| S2 | same with `simple-schematron-invalid.xml`, then `curl -s <Location>` | CVR with `cvr:decision="REJECT"`, `scenario-id="simple.sch"` — the file name of the part names the scenario |
| S3 | `-F resource=@$T/repository/simple.xsl` | `scenario-id="simple.xsl"`, `cvr:decision="ACCEPT"` — a precompiled XSL is run as is |
| S4 | only `-F document=@…` | `400`, `"An ad hoc validation needs at least one artifact: a 'resource' part, or a 'repository' with 'artifact' entries"`; only `-F resource=@…` | `400`, `"… needs the part 'document'"` |
| S5 | `-F resource=@$T/repository/does-not-compile.sch` | `201`; result `cvr:status="CANCELLED"` with `rule-prepare-error` — a broken rule set is a run, not a bad request |
| S6 | `-F resource=@$T/repository/some.txt` | `400`, `"Unsupported artifact: not well-formed XML …"` — neither the name nor the content say what it is |
| S7 | `-F document=@…/01.01a-INVOICE_ubl.xml -F resource=@…/XRechnung-UBL-validation.xsl` | `cvr:decision="ACCEPT"` |
| S8 | `-F resource=@e2e/adhoc/rules/with-include.sch` | `rule-prepare-error`: only one file arrives, `abstracts.sch` is not there — use the repository (S12) |
| S9 | `curl -s -o /dev/null -w '%{http_code}' -H 'Content-Type: application/xml' --data-binary @$T/input/simple.xml $BASE/api/validation` | `201` — the regular endpoint is untouched |
| S10 | `-F document=@$T/input/simple-schema-invalid.xml -F resource=@$T/repository/simple.xsd -F resource=@$T/repository/simple.sch` | one scenario `scenario-id="simple.xsd, simple.sch"`, `code="schema-violation"`, `cvr:decision="REJECT"` |
| S11 | `-F "resource=@$T/repository/simple.sch;filename="` (no file name) | `scenario-id="resource-1.sch"` — the kind was read from the root element, the name generated |
| S12 | `-F document=@$T/input/foo.xml -F repository=@rules.zip -F artifact=with-include.sch` (ZIP of `e2e/adhoc/rules/`) and the same with `simple.xml` | `foo.xml`: `cvr:decision="REJECT"`, `scenario-id="with-include.sch"` — the include resolved inside the ZIP; `simple.xml`: `ACCEPT` |
| S13 | `-F repository=@rules.zip -F artifact=missing.sch` | `400`, `"The repository has no entry 'missing.sch'"` |

The part `schematron` of the first version is still accepted as one `resource`. In Bruno: request `5 Ad hoc validation`
in `server/bruno/` (file parts point at the test data; replace them with your own files, the disabled parts show the
repository form). Several files under one key are written as `resource: @file(a|b)` in Bruno — two lines with the same
key are folded into one, and only the last file is sent.

## What the pairs show

- **Same engine, same report.** A1/S1, A2/S2 and A12/S10 produce the same CVR up to the run metadata; the scenario is
  named after the artifacts in both (`simple.sch`, `simple.xsd, simple.sch`).
- **How the kind is decided.** The CLI decides by extension only (A13); the server by the file name of the part, and
  without one by the root element (S11) — a `.txt` is a `400` (S6). Both end in the same pipeline path: `.sch` is
  compiled by `schxslt` and the report names the transpiler, a precompiled XSL is run as is, an `.xsd` is the schema step.
- **Failures are runs.** A rule set that does not compile, a rule that raises an error, a document that is not XML: each
  is a cancelled run with a partial CVR and `REJECT` — exit 1 with `Processing errors` in the CLI, `201` plus a
  `CANCELLED` report on the server. `400` is for a request that cannot make a run: no document, no artifact, an artifact
  of unknown kind, a ZIP entry that is not there.
- **Includes.** The CLI resolves `sch:include`/`xsl:import` within the repository — the common directory of the
  artifacts, or `-r` (A8, A11). On the server a single `resource` cannot (S8); the ZIP `repository` is the answer (S12):
  its entries keep their paths, `artifact` names what to apply. The Java client posts a list of files that way.
