# Validator Command Line Interface (CLI)

The `validator` comes with a command line interface (CLI) which validates any number of XML documents and writes a
Conformance Validation Report (CVR) for each of them.

**Important hint**: since v2.0.0 the filename has been changed from `validator-*` to `validator-cli-*` and the jar is a
Quarkus-based uber-jar.

## Synopsis

There are two ways to say what the documents are validated against — a scenario configuration, or a set of validation
artifacts given directly:

```shell
# along the scenarios of a configuration
java -jar validator-cli-<version>.jar -s <scenarios.xml> [-r <repository>] [OPTIONS] [FILE] [FILE] ...

# ad hoc, against one or more artifacts (.xsd, .sch, .xsl)
java -jar validator-cli-<version>.jar -S <artifact> [-S <artifact> ...] [-r <repository>] [OPTIONS] [FILE] [FILE] ...
```

`FILE` is an XML document or a directory (every `.xml` in it). The validator can also read one document from the standard
input:

```shell
# via redirection
java -jar validator-cli-<version>.jar -s <scenarios.xml> [OPTIONS] < my-input.xml

# read from pipe
cat my-input.xml | java -jar validator-cli-<version>.jar -s <scenarios.xml> [OPTIONS]
```

The help option displays all CLI options:

```shell
java -jar validator-cli-<version>.jar --help
```

## Options

| Option | Effect |
|---|---|
| `-s`, `--scenarios <scenarios.xml>` | The scenario configuration. May be given several times; every configuration brings its own repository. Required unless `-S` is given. |
| `-r`, `--repository <dir>` | The artifact repository of a configuration. Without it the directory of the `scenarios.xml` is used. May be given several times, see below. With `-S` it is the repository root of the artifacts (one `-r`, the artifacts must lie within it); without it the common directory of the artifacts. |
| `-S`, `--artifact <file>` (alias `--schematron`) | Ad hoc validation: a validation artifact applied to every document — an XML Schema (`.xsd`), a Schematron (`.sch`, compiled at start) or a precompiled Schematron XSLT (`.xsl`, run as is). May be given several times; all of them form one scenario, applied in this order. Excludes `-s`. |
| `-o`, `--output-directory <dir>` | Where the reports are written. Default: the working directory. |
| `-p`, `--print` | Also print each report to standard output. |
| `--report-prefix`, `--report-postfix` | Parts of the report file name, see [Output](#output). |
| `-m`, `--memory-stats` | Log memory usage after every document; for huge documents. |
| `-d`, `--debug` | More output on errors (stack traces). |
| `-X`, `--debug-logging` / `-l <level>` | Log level; `-X` is `-l debug`. Default: `OFF`. |
| `-?`, `--help` | Show the usage. |
| `-e`, `--extract-reports` | **No effect** since 2.0: the report transformations of a 1.x configuration (`createReport`) are not part of 2.0, there is nothing to extract. A warning is logged. |
| `--serialize-report-input` | **No effect**, deprecated. |

### Several configurations

Several scenario configurations and their repositories can be given, either in order or by name. The scenarios of all
configurations are used for detection; the artifacts of the selected scenario come from the repository of *its*
configuration. Valid usages are:

```shell
# several scenarios, implicit repositories (the directory of each scenarios.xml)
java -jar validator-cli-<version>.jar -s <scenarios1.xml> -s <scenarios2.xml> [OPTIONS] [FILE]

# several scenarios, one repository for the first, implicit for the second
java -jar validator-cli-<version>.jar -s <scenarios1.xml> -s <scenarios2.xml> -r <repo1> [OPTIONS] [FILE]

# several scenarios, several repositories, paired in order
java -jar validator-cli-<version>.jar -s <scenarios1.xml> -r <repo1> -s <scenarios2.xml> -r <repo2> [OPTIONS] [FILE]
java -jar validator-cli-<version>.jar -s <scenarios1.xml> -s <scenarios2.xml> -r <repo1> -r <repo2> [OPTIONS] [FILE]

# several scenarios, several repositories, paired by name
java -jar validator-cli-<version>.jar -s "NAME1=<scenarios1.xml>" -s "NAME2=<scenarios2.xml>" -r "NAME1=<repo1>" -r "NAME2=<repo2>" [OPTIONS] [FILE]
```

### Ad hoc validation against a set of artifacts

`-S` is for rule authors and the CI of a Schematron repository: "run these rules against these files", without a
`scenarios.xml`. The engine builds one scenario from the artifacts — it applies to every document, the `.xsd` files are
its XML Schema, every `.sch` or `.xsl` one of its rule sets, in the order given — and runs the same pipeline as for a
configured scenario, so the report is the same CVR. The scenario is named after the artifacts (`simple.xsd, simple.sch`).
The kind of an artifact is decided by its extension; anything else is a configuration error.

```shell
# one Schematron
java -jar validator-cli-<version>.jar -S rules/XRechnung-UBL-validation.sch -o /tmp/cvr rechnung.xml

# schema and rules together
java -jar validator-cli-<version>.jar -S xsd/UBL-Invoice-2.1.xsd -S rules/XRechnung-UBL-validation.xsl -o /tmp/cvr rechnung.xml
```

The repository of the ad hoc scenario is the directory given with `-r`, or without it the deepest directory that contains
all artifacts. Relative `xs:import`, `sch:include` and `xsl:import` are resolved within that repository and nowhere else,
so `-r` is the way to reach a module that lies above the artifacts. An artifact outside the repository is a configuration
error. RELAX NG (`.rnc`) is not supported yet.

## Output

For each document the CLI writes the CVR to `<output-directory>/[<prefix>-]<name>[-<postfix>]-report.xml`, where `<name>`
is the document name without its `.xml` extension — `rechnung.xml` becomes `rechnung-report.xml`. With `-p` the report is
printed to standard output as well.

After all documents the CLI prints the result table:

```
Results:
File                            Schema   Schematron   Acceptance   Error/Description
rechnung.xml                      Y          Y        ACCEPTABLE
kaputt.xml                        Y          N          REJECT     [BR-DE-01] An invoice must ...
Acceptable: 1 Rejected: 1
```

| Column | Meaning |
|---|---|
| `Schema`, `Schematron` | `Y` — rule sets of that kind ran and none reported an error; `N` — one reported an error, or the run was cancelled before the rules ran; `-` — the run completed but applied no rule set of that kind (an ad hoc validation has no XML Schema). |
| `Acceptance` | The decision of the pipeline in the words of 1.6: `ACCEPTABLE` (accept), `REJECT`, `UNDEFINED` (the decision could not be made — `EVALUATE_FURTHER` in the CVR). |
| `Error/Description` | The findings of the rule sets, or — for a cancelled run — what the cancelling step reported. |

A status line sums up acceptable and rejected documents and, if any, the number of runs that were cancelled
(`Processing errors`). The table, its columns and the status line are unchanged from 1.6.

## Return codes

| code | description |
|-|-|
| 0  | All validated documents are acceptable, or the usage was requested |
| positive integer | Number of documents that are not acceptable (rejected, or undecidable) |
| -1 | Parsing error. The command line arguments are incorrect |
| -2 | Configuration error: neither `-s` nor `-S`, both of them, a missing file or repository, an artifact of unknown kind or outside the repository, no document to validate |
