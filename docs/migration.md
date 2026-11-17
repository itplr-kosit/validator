# Migrating from 1.6.x to 2.x

Validator 2.0 is a rewrite of the engine, not a continuation of the 1.x line. There is no compatibility layer: 1.x
does not separate its public from its internal API, so there is no boundary such a layer could rest on (see the
decision record `validator-no-compatibility-bridge-to-1x`). This page lists what changed and what to do about it,
for each way the validator is used. The [CHANGELOG](../CHANGELOG.md) has the complete list.

## What changed

**One report, the CVR.** Every run produces a Conformance Validation Report: an XVRL document
(`http://www.xproc.org/ns/xvrl`) with the extension vocabulary of the CVR profile (`urn:conformatron:cvr:draft`). It
records each step of the run - parsing, scenario detection and selection, artifact retrieval, rule preparation and
application, the conformance of every rule set and the decision. A run that stops early, because the document is
not well-formed or no scenario applies, still yields a report, a partial one. The report of 1.x, produced by an
XSLT that the configuration named in `createReport`, is gone, and so is the compact report of the API. The engine
writes XML only; rendering a report for people is left to whoever consumes it.

**The decision is computed, not configured.** The recommendation (`ACCEPT` or `REJECT`) follows from the
conformance statements of the rule sets the scenario applied. `acceptMatch` is still allowed in a configuration but
not evaluated.

**No fallback scenario.** A document that no scenario applies to is rejected: the run stops at the step
`detect-scenarios`, and the report says so.

**Modules.** The code is split into modules with a defined purpose - `api`, `schematron`, `scenario`, `cvr`, `core`,
`cli`, `server`, `client` and supporting ones; see [modules](modules.md). The configuration format and its JAXB
binding live in `validator-scenario`.

**Java 25.** The minimum Java version is 25.

## Migrating a scenario configuration

2.0 does not read configurations written for 1.x (namespace `http://www.xoev.de/de/validator/framework/1/scenarios`).
Converting one by hand is a short list of changes. The repository has the XRechnung configuration in both forms, which
shows all of them: `e2e/comparison/input/scenarios-v1.6.xml` and `e2e/comparison/input/scenarios-v2.0-framework2.xml`.

1. Change the namespace to `http://www.xoev.de/de/validator/framework/2/scenarios` and `frameworkVersion` to `2.0.0`.
2. Remove every `createReport` element, together with the `namespace` declarations only the report XSLT needed (the
   XRechnung configuration declared the prefix `rep` for `http://www.xoev.de/de/validator/varl/1`).
3. Leave `acceptMatch` empty: `<acceptMatch/>`.
4. Name the Schematron processor of every rule set: `<validateWithSchematron compiler="schxslt">`. For a precompiled
   `.xsl` it states which processor produced it; for a `.sch` it is the processor the validator compiles it with.
5. Keep the `customLevel` overrides inside the `validateWithSchematron` of the rule set they belong to, after its
   `resource`.

The format is described in [configurations](configurations.md).

### On to version 3

Version 2 is the format of 2.0; version 3 (`urn:kosit:validator:scenario:3`) is read as well and adds named
repositories and structured coordinates. A configuration of version 2 is converted with

```bash
validator convert -s scenarios.xml -o scenarios-v3.xml
```

Version 3 requires a coordinate (`groupId:artifactId:version[:classifier]`) for every scenario and every resource,
which version 2 may leave off. The report identifies scenarios and artifacts by these coordinates, so `convert`
proposes the missing ones and writes nothing until you confirm them: pass `--group-id` for the group id they are
published under, and `--accept-proposals` to write them in. The same migration is available without the command line,
as `ScenarioMigration` in the module `validator-scenario`.

## Migrating code that uses the API

| 1.x | 2.0 |
|---|---|
| `VCheck`, `DefaultVCheck` | `ValidationEngine<ConformanceValidationResult>`, implemented by `ConformanceValidation` |
| `VResult`, `DefaultResult`, the compact report | `ConformanceValidationResult`: `getDecision()`, `getRationale()`, `isConformant()`, `writeCvr(OutputStream)` |
| `VConfiguration`, `DefaultConfiguration`, `ConfigurationKeys` | `ScenarioSet`, loaded with `ScenarioSet.load(scenarios, repository)` or assembled with `ScenarioSet.create()` |
| the fallback scenario | none - an unmatched document is rejected at `detect-scenarios` |
| `SchematronValidation` | `ConformanceValidation.adHoc(...)`, the same engine over a scenario assembled from artifacts |

The builders keep their shape (`ConfigurationBuilder`, `ScenarioBuilder`, `SchemaBuilder`, `SchematronBuilder`), without
`report(...)` and `fallback()`. Type names treat acronyms as words now: `XmlHelper`, not `XMLHelper`; `Xvrl`, not
`XVRL`. A complete example is in [api](api.md).

The report names the engine that produced it, and the engine knows that about itself. An application that wants the
report to name itself instead says so with `withEngineInformation(...)`.

## Migrating use of the command line

The command line drives the new engine. The result table, the memory statistics and the exit codes are as before;
the report it writes is the CVR. New are the ad hoc validation (`-S`, a set of XML Schemas and Schematrons applied to
every document, without a configuration) and the subcommand `convert`. See [cli](cli.md).

## Migrating use of the server

The daemon mode of 1.x is gone; the server is a separate application built on Quarkus, with a REST API that follows
RFC 9110 §9.3.3. `POST /api/validation` runs the validation of the posted document and answers `201 Created` with the
location of the result; `GET /api/validation/result/{id}` returns it. `POST /api/validation/adhoc` validates against
posted artifacts. The earlier `validate` endpoints, their compact XML and JSON responses and the `X-VALIDATOR-*`
headers are removed. See [server](server.md); the Java client follows the same two steps (`createRun`, `fetchResult`), see
[client](client.md).

## Building

The build needs Java 25 and Maven. The server is a Quarkus application; it does not build a container image unless
asked to (`-Dquarkus.container-image.build=true`).
