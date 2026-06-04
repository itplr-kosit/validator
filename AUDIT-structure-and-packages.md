# Validator — Code Structure & Package Audit

**Date:** 2025-04-07  
**Scope:** `validator/` module — full structural review including package design, module boundaries, layering violations, naming conventions, and forward-compatibility with the `conformatron-api` target architecture.

---

## Table of Contents

1. [Current Module & Package Topology](#1-current-module--package-topology)
2. [Package-Level Findings](#2-package-level-findings)
3. [Module Boundary & Dependency Findings](#3-module-boundary--dependency-findings)
4. [Layering & Architectural Violations](#4-layering--architectural-violations)
5. [Naming Convention Issues](#5-naming-convention-issues)
6. [Gap Analysis: validator vs. conformatron-api](#6-gap-analysis-validator-vs-conformatron-api)
7. [Structural Migration Roadmap](#7-structural-migration-roadmap)
8. [Summary Matrix](#8-summary-matrix)

---

## 1. Current Module & Package Topology

### Multi-module layout

```
validator/                          (parent POM, packaging=pom)
├── api/                            (validator-api)
│   └── src/main/java/org/kosit/validator/
│       ├── api/                    ← public API types (AcceptRecommendation, XmlError)
│       │   ├── compact/            ← CompactXVRLReport, CompactXVRLReportSummary, ValidatorEngineInformation
│       │   ├── xsd/                ← ValidatorSchemas (XSD path constants)
│       │   └── xvrl/               ← BaseDetection, BaseMessage, BaseReportSummary (interfaces)
│       └── impl/                   ← LEAKED IMPL TYPES IN API MODULE
│           ├── model/              ← BaseOutput, BaseXMLSyntaxError
│           ├── xml/                ← LongAdapter, StringTrimAdapter
│           └── xvrl/               ← AbstractXVRLReportSummary, BaseReport
├── core/                           (validator-core, not in this repo — submodule)
├── cli/                            (validator-cli)
│   └── src/main/java/org/kosit/validator/cmd/
│       ├── (root)                  ← CommandLineApplication, CommandLineOptions, Validator,
│       │                              InternalCheck, Printer, ReturnValue, TypeConverter, etc.
│       └── report/                 ← Grid, Line, Text, Format, Justify (ANSI output)
├── server/                         (submodule)
└── client/                         (submodule)
```

### Observation: flat, monolithic packages

The `cli` module puts **15 classes** into a single package `org.kosit.validator.cmd`. There is no separation between:
- CLI framework integration (picocli/Quarkus wiring)
- Business logic (validation orchestration)
- I/O actions (serialize, extract, print)
- Output formatting

The `api` module mixes public API interfaces with internal base classes under `impl/` — defeating the purpose of a separate API artifact.

---

## 2. Package-Level Findings

### 2.1 🔴 `impl` package in `api` module — broken encapsulation

**Files affected:**
- `api/…/impl/model/BaseOutput.java`
- `api/…/impl/model/BaseXMLSyntaxError.java`
- `api/…/impl/xml/LongAdapter.java`
- `api/…/impl/xml/StringTrimAdapter.java`
- `api/…/impl/xvrl/AbstractXVRLReportSummary.java`
- `api/…/impl/xvrl/BaseReport.java`

**Problem:** The `validator-api` artifact is supposed to be the **public contract** that consumers depend on. Having `org.kosit.validator.impl.*` inside this artifact means:
- Consumers see implementation details on the classpath
- Refactoring `impl` classes forces a new API release
- There is no clean dependency direction: `api` should have **zero** `impl` packages

**Recommendation:** Move these to `validator-core` or a new `validator-model` module. The api module should contain only interfaces, enums, DTOs, and XSD path constants.

### 2.2 🟠 `api/compact/` — ad-hoc convenience layer without interface abstraction

`CompactXVRLReport` and `CompactXVRLReportSummary` are concrete classes with business logic (null handling, QName lookups, XVRL manipulation). They wrap JAXB-generated model objects (`XVRLReport`, `XVRLReportSummary`) and add convenience access.

**Problems:**
- No interface — consumers are coupled to the concrete class
- Mixes two concerns: XVRL model navigation + CVRL-namespace attribute management
- The `conformatron-api` equivalent (`ICTValidationReport`, `ICTValidationRootReport`) is fully interface-based with clean contracts
- This class cannot evolve into a Conformatron implementation without a breaking rewrite

### 2.3 🟠 `cli/cmd/` — god-package anti-pattern

All 15 CLI classes share `org.kosit.validator.cmd`. A domain-driven split would be:

| Concern | Current location | Suggested package |
|---------|-----------------|-------------------|
| CLI framework (picocli, Quarkus) | `cmd/` | `cmd.framework` or `cmd` (slim) |
| Validation orchestration | `Validator.java`, `InternalCheck.java` | `cmd.execution` |
| Post-processing actions | `SerializeReportAction`, `ExtractReportContentAction`, `PrintReportAction`, `PrintMemoryStats` | `cmd.action` |
| Report formatting (ANSI grid) | `report/` (already separated) | ✅ OK |
| Type conversion / naming | `TypeConverter`, `DefaultNamingStrategy`, `NamingStrategy` | `cmd.config` or `cmd.conversion` |
| DTO / value types | `ReturnValue` | `cmd.model` |
| General utilities | `Printer`, `Util` | `cmd.util` |

### 2.4 🟡 `api/xvrl/` — interfaces without implementations nearby

`BaseDetection`, `BaseMessage`, `BaseReportSummary` are pure interfaces that serve as JAXB superclass injection targets. They are the right idea (abstracting XVRL model behavior) but:
- They live in `api/xvrl/` while their implementations live in JAXB-generated classes (not in this module)
- The naming (`BaseDetection`) suggests an abstract class, not an interface
- The `conformatron-api` uses `ICT`-prefixed interfaces (`ICTDetection`, `ICTDetectionList`) — a much clearer convention

### 2.5 🟡 `api/xsd/ValidatorSchemas` — path constants without schema files

This class declares paths like `/xsd/xvrl-1.0.xsd` but the actual XSD files live in `src/main/model/xsd/`. If the XSD path constants are part of the public API, they should be validated at build time. Currently it's a string-only reference with no compile-time guarantee.

---

## 3. Module Boundary & Dependency Findings

### 3.1 🔴 `cli` → `core` → `api` — missing `api` module self-sufficiency

The `api` module depends on JAXB-generated classes (`org.kosit.validator.model.xvrl.*`, `org.oclc.purl.dsdl.svrl.*`). These model classes are generated from XSDs during the `api` build but live in generated-sources. This means:
- The API artifact contains generated JAXB classes (schema-derived code)
- Consumers of `validator-api` transitively pull in JAXB runtime
- The public API and the serialization format are **coupled**

**conformatron-api contrast:** The Conformatron API uses **zero** generated code. It is pure interfaces with `jspecify` annotations. The JAXB/XVRL mapping is an **implementation concern** of the runtime, not the API.

### 3.2 🟠 Parent POM declares all dependencies globally

```xml
<!-- parent pom.xml -->
<dependencies>
    <dependency>commons-io</dependency>
    <dependency>commons-lang3</dependency>
    <dependency>Saxon-HE</dependency>
    <dependency>jaxb-runtime</dependency>
    <dependency>junit</dependency>
    <dependency>mockito-core</dependency>
    <dependency>rest-assured</dependency>
    ...
</dependencies>
```

Every child module inherits **all** these dependencies. The `api` module, which should be lightweight, inherits Saxon-HE, commons-io, rest-assured (test scope but still resolved), etc. Dependencies should be declared per-module, not globally.

### 3.3 🟠 `cli` module takes hard dependency on `impl` internals

The CLI module imports from:
- `org.kosit.validator.impl.DefaultCheck`
- `org.kosit.validator.impl.ConversionService`
- `org.kosit.validator.impl.ScenarioRepository`
- `org.kosit.validator.impl.tasks.CheckAction`
- `org.kosit.validator.impl.tasks.CreateReportsAction`
- `org.kosit.validator.impl.xml.ProcessorProvider`
- `org.kosit.validator.impl.xvrl.XVRLReportBuilder`

The `cli` module is tightly coupled to `core` implementation details. There is no service abstraction layer. The Conformatron architecture defines `ICTAction`, `ICTActionExecution`, and `ICTActionReport` as the contract between pipeline steps — the validator has no equivalent.

---

## 4. Layering & Architectural Violations

### 4.1 🔴 No pipeline abstraction — imperative orchestration

`Validator.processActions()` is a 40-line imperative method that:
1. Creates a `Processor` (Saxon)
2. Loads configuration
3. Manually assembles a `List<CheckAction>` based on CLI flags
4. Iterates inputs and calls `check.checkInput(input)` in a loop
5. Manually manages timing, progress output, and results

**conformatron-api contrast:** The Conformatron defines:
- `ECTCanonicalAction` — well-known pipeline steps (PARSE_DOCUMENT → DETECT_SCENARIO → RETRIEVE_ARTIFACTS → TRANSPILE_RULES → VALIDATE_RULES → DECISION_RECOMMENDATION)
- `ICTAction` + `ICTActionExecution` — each step is a first-class object with start/end timestamps, input/output descriptions, and a report
- `ICTValidationReport` per action, nested sub-reports

The validator has **none** of this. The pipeline is hardcoded in `Validator.java` and `DefaultCheck` (in core). There is no way to introspect, reorder, or extend the pipeline without modifying source code.

### 4.2 🔴 No structured result model — stringly-typed reporting

The validator's `Result` interface (in `api`) returns:
- `boolean isAcceptable()`
- `boolean isSchemaValid()`
- `boolean isSchematronValid()`
- `List<String> getProcessingErrors()` ← strings, not typed objects
- `List<XmlError> getSchemaViolations()`
- `List<SchematronOutput> getSchematronResult()` ← JAXB types leaked into API

**conformatron-api contrast:** Clean domain model:
- `ICTDetection` with `ICTSeverity`, `ICTDetectionLocation`, `ICTDetectionText` (i18n), `code`, `field`
- `ICTDetectionList` with filter/count methods
- `ICTReportDigest` with `isValid()`, `getErrorCount()`, `getWarningCount()`, `getErrorCodes()`
- `ICTValidationReport` with metadata, digest, detections, sub-reports

### 4.3 🟠 `CompactXVRLReport` conflates format and domain logic

`CompactXVRLReport` does three things:
1. Wraps XVRL JAXB objects (format concern)
2. Manages CVRL-namespace custom attributes via `QName` (serialization concern)
3. Provides domain queries like `isSchemaValid()`, `getAcceptance()` (business logic)

These should be separated into:
- A domain interface (aligned with `ICTValidationReport`)
- A JAXB/XVRL mapping adapter (implementation)
- A CVRL attribute helper (utility)

### 4.4 🟠 No severity model — fragmented across types

The validator uses:
- `XmlError.Severity` (3 values: WARNING, ERROR, FATAL_ERROR)
- `XMLSyntaxErrorSeverity` (JAXB-generated enum, maps to XSD)
- `XVRLDetection.Severity` (JAXB-generated, string-based `fromValue()`)
- `AcceptRecommendation` (UNDEFINED, ACCEPTABLE, REJECT) — not a severity but conflated

**conformatron-api contrast:** Single severity hierarchy:
- `ICTSeverity` interface with `getNumericLevel()` (ordered), `isError()`, `isWarning()`, `getWorst()`
- `ECTSeverity` enum implementing it: NONE(0) < INFO(50) < WARNING(100) < ERROR(200) < FATAL_ERROR(300)

### 4.5 🟡 No validation type model

The validator has no concept equivalent to `ICTValidationType` / `ECTValidationType`. Validation types (XSD, Schematron, partial XSD, etc.) are implicit in the code, not modeled as first-class objects. This makes it impossible to:
- Query what validation types are configured
- Define pipeline behavior per validation type (`isStopValidationOnError()`)
- Support non-XML formats (EDIFACT, PDF, JSON)

---

## 5. Naming Convention Issues

### 5.1 🟡 No systematic naming convention

| conformatron-api convention | validator equivalent | Issue |
|---------------------------|---------------------|-------|
| `ICT*` = interface | `Base*` = sometimes interface, sometimes abstract class | `BaseDetection` is an interface; `BaseReport` is an abstract class |
| `ECT*` = enum | No convention | Enums use various patterns (`AcceptRecommendation`, `Severity`) |
| `*List` = typed collection | None | Lists are `List<XmlError>` inline |
| `*Digest` = summary | None | No equivalent concept |
| `*Location` = position in document | Row/column spread across `XmlError`, `Location` | No unified type |
| `*Text` = i18n text | Plain `String` everywhere | No i18n support |

### 5.2 🟡 Mixed language in identifiers

- `assertFileExistance` (English typo — should be `Existence`)
- `sayGoodby` (should be `sayGoodbye`)
- German comments mixed with English method names
- `Hauptprogramm`, `Prüflinge`, `Extrahiert Erstellten Dokumentne` in Javadoc

### 5.3 🟡 `report/` package is CLI-specific output formatting, not domain reports

The package `cmd.report` contains ANSI terminal formatting utilities (`Grid`, `Line`, `Text`, `Format`, `Justify`). The name `report` collides conceptually with the domain concept of validation reports. Should be renamed to `cmd.output` or `cmd.terminal`.

---

## 6. Gap Analysis: validator vs. conformatron-api

This section maps every `conformatron-api` concept to its validator equivalent (or lack thereof).

### 6.1 Report Model

| conformatron-api | validator | Gap |
|------------------|-----------|-----|
| `ICTValidationRootReport` | `XVRLReportSummary` (JAXB) + `CompactXVRLReportSummary` | ⚠️ No clean interface; JAXB leaked |
| `ICTValidationReport` | `XVRLReport` (JAXB) + `CompactXVRLReport` | ⚠️ No interface; business logic in wrapper |
| `ICTReportMetadata` | Spread across `XVRLMetadata` fields + CVRL QName attributes | 🔴 No unified metadata type |
| `ICTReportDigest` | `XVRLDigest` (JAXB) | ⚠️ Exists but no clean interface |
| `isCompact()` mode flag | Separate classes (`CompactXVRLReport` vs. full `XVRLReport`) | ⚠️ Different classes instead of mode flag |
| `getOverallWorstSeverity()` | Not implemented | 🔴 Missing |
| `isOverallValid()` | `isSuccessful()` in `DefaultCheck` (impl-only) | ⚠️ Not in API |
| Sub-reports (`getSubReports()`) | Not supported | 🔴 Missing — flat list only |
| Report ID (UUID) | Not implemented | 🔴 Missing |
| Language / i18n | Not implemented | 🔴 Missing |

### 6.2 Detection Model

| conformatron-api | validator | Gap |
|------------------|-----------|-----|
| `ICTDetection` | `XVRLDetection` (JAXB) + `BaseDetection` interface | ⚠️ Interface exists but is JAXB-bound |
| `ICTDetectionList` | `List<XVRLDetection>` raw list | 🔴 No typed collection with filter/count |
| `ICTDetectionLocation` | `Location` (JAXB) | ⚠️ No clean interface |
| `ICTDetectionText` (i18n) | Plain `String` | 🔴 No i18n support |
| `ICTSeverity` interface | `XVRLDetection.Severity` (JAXB enum) | 🔴 No ordered, extensible severity |
| `ECTSeverity` enum | `XmlError.Severity` (3 values, no NONE/INFO) | 🔴 Missing NONE, different ordering |
| `getCode()` classification | `XVRLDetection.getCode()` | ✅ Exists |
| `getField()` / XPath | Not standardized | ⚠️ Partial via `Location` |
| `getSummary()` | Not implemented | 🔴 Missing |
| `getLinkedException()` | Not implemented | 🔴 Missing |

### 6.3 Action / Pipeline Model

| conformatron-api | validator | Gap |
|------------------|-----------|-----|
| `ICTAction` | `CheckAction` interface | ⚠️ Different contract |
| `ECTCanonicalAction` (7 steps) | Implicit in `DefaultCheck` task chain | 🔴 No canonical action vocabulary |
| `ECTActionType` (VALIDATOR/TRANSFORMATOR/OTHER) | Not modeled | 🔴 Missing |
| `ICTActionExecution` (start/end time, I/O) | Not modeled | 🔴 Missing |
| `ICTActionReport` (per-action) | `ProcessStepResult` (internal) | ⚠️ Exists but not in API |
| Pipeline ordering (`ECTCanonicalAction` sequence) | Hardcoded in `Validator.processActions()` | 🔴 Not configurable |

### 6.4 Validation Type Model

| conformatron-api | validator | Gap |
|------------------|-----------|-----|
| `ICTValidationType` | Not modeled | 🔴 Missing |
| `ECTValidationType` (9 types incl. Schematron variants) | Implicit | 🔴 Missing |
| `ECTValidationBaseType` (XML, XSD, SCH, EDIFACT, PDF) | Hardcoded XML-only | 🔴 No extensibility |
| `isStopValidationOnError()` | Not configurable | 🔴 Missing |
| `isContextRequired()` | Not modeled | 🔴 Missing |

### 6.5 Cross-Cutting

| conformatron-api | validator | Gap |
|------------------|-----------|-----|
| `@NonNull` / `@Nullable` (jspecify) | None | 🔴 No null-safety annotations |
| `@Nonnegative` / `@Nonempty` | None | 🔴 No contract annotations |
| `@CheckForSigned` | None | 🔴 No value-range annotations |
| Zero external dependencies (only jspecify) | Depends on Saxon, JAXB, commons-*, SVRL | ⚠️ Heavy API surface |
| Interface-only API | Mixes interfaces + classes + JAXB | 🔴 Not interface-first |

---

## 7. Structural Migration Roadmap

To align the validator with the `conformatron-api` target architecture, the following structural changes are recommended, ordered by priority and dependency.

### Phase 1: Module Hygiene (no API changes)

1. **Move `impl/` packages out of `api` module** into `core` or a new `validator-model` module
2. **Move per-module dependencies** out of parent POM `<dependencies>` into individual child POMs
3. **Fix Lombok version mismatch** — use `${version.lombok}` everywhere
4. **Remove duplicate plugin declarations** in `jee-standard` profile
5. **Rename `cmd.report`** to `cmd.output` or `cmd.terminal`

### Phase 2: Domain Model Introduction

6. **Introduce `ICTSeverity` / `ECTSeverity`** adapter or bridge in `api`
   - Implement the Conformatron severity interface
   - Map `XVRLDetection.Severity` and `XmlError.Severity` to it
7. **Introduce `ICTDetection`** bridge wrapping `XVRLDetection`
8. **Introduce `ICTDetectionList`** wrapping `List<XVRLDetection>` with filter/count methods
9. **Introduce `ICTReportDigest`** adapter wrapping `XVRLDigest`
10. **Introduce `ICTReportMetadata`** adapter composing XVRL metadata fields

### Phase 3: Report Structure Alignment

11. **Introduce `ICTValidationReport`** implementation wrapping `CompactXVRLReport`
    - Expose `getAction()`, `getMetadata()`, `getDigest()`, `getDetections()`
    - Support sub-reports
12. **Introduce `ICTValidationRootReport`** implementation wrapping `CompactXVRLReportSummary`
    - Expose `getReports()`, `isCompact()`, `getOverallWorstSeverity()`, `isOverallValid()`
13. **Add report IDs** (UUID generation for root + sequential for sub-reports)
14. **Add i18n support** (`ICTDetectionText`) — at minimum single-locale bridge

### Phase 4: Pipeline Formalization

15. **Model canonical actions** as `ECTCanonicalAction` equivalents
16. **Introduce `ICTAction`** interface for each pipeline step
17. **Refactor `DefaultCheck`** to produce per-action `ICTActionReport` instances
18. **Make pipeline configurable** — action ordering, stop-on-error, custom actions
19. **Add `ICTActionExecution`** with timing, input/output descriptions

### Phase 5: API Cleanup

20. **Remove JAXB types from public API** — wrap them behind interfaces
21. **Add `@NonNull` / `@Nullable`** annotations (jspecify or jakarta.annotation)
22. **Define `ICTValidationType`** for the validator's supported types (XML, XSD, Schematron)
23. **Deprecate `CompactXVRLReport`** / `CompactXVRLReportSummary` in favor of Conformatron interfaces
24. **Extract `validator-spi`** (Service Provider Interface) module for third-party integrations

---

## 8. Summary Matrix

| Category | Issues Found | Critical | High | Medium | Low |
|----------|:----------:|:--------:|:----:|:------:|:---:|
| Package encapsulation | 5 | 1 | 2 | 1 | 1 |
| Module boundaries | 3 | 1 | 2 | 0 | 0 |
| Layering / architecture | 5 | 2 | 2 | 1 | 0 |
| Naming conventions | 3 | 0 | 0 | 3 | 0 |
| Conformatron gap (report) | 10+ | 5 | 3 | 2 | 0 |
| Conformatron gap (detection) | 8+ | 4 | 2 | 2 | 0 |
| Conformatron gap (action/pipeline) | 6+ | 4 | 1 | 1 | 0 |
| Conformatron gap (validation type) | 4+ | 3 | 1 | 0 | 0 |
| Conformatron gap (cross-cutting) | 5+ | 3 | 1 | 1 | 0 |
| **Total** | **~50** | **23** | **14** | **11** | **1** |

### Key Takeaway

The validator's current structure is a **monolithic, JAXB-coupled, imperative codebase** with no clean API surface. The `conformatron-api` defines a fundamentally different architecture: **interface-first, pipeline-oriented, severity-ordered, i18n-capable, and format-independent**.

Bridging this gap requires a phased migration that starts with module hygiene (Phase 1 — low risk, high value) and progressively introduces the Conformatron domain model as adapter/bridge implementations wrapping the existing XVRL infrastructure. A full rewrite is not necessary — but the API module needs a clean separation from JAXB internals, and the pipeline needs to become a first-class, inspectable concept.
