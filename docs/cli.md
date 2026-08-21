# Validator Command Line Interface (CLI)

The `validator` comes with a command line interface (CLI) which allows validating any number of input XML files.

**Important hint**: since v2.0.0 the filename has been changed from `validator-*` to `validator-cli-*` and the jar is now a Quarkus-based uber-jar.

The general way using the CLI is:

```shell
java -jar  validator-cli-<version>.jar  -s <scenario-config-file> [OPTIONS] [FILE] [FILE] [FILE] ...
```

The validator can also read the XML file from the standard input

```shell
# via redirection
java -jar validator-cli-<version>.jar  -s <scenario-config-file> [OPTIONS] < my-input.xml

# read from pipe
cat my-input.xml | java -jar validator-cli-<version>.jar  -s <scenario-config-file> [OPTIONS]
```

The help option displays further CLI options:

```shell
java -jar  validator-cli-<version>.jar --help
```

You can also use multiple scenario configurations and multiple repositories with resources for these. The validator either supports
supplying the parameters in order or using named configuration. Valid usages are

```shell
# multiple scenarios, implicit repository
java -jar  validator-cli-<version>.jar -s <scenario-config-file1> -s <scenario-config-file2> [OPTIONS] [FILE]

# multiple scenarios, single defined repository
java -jar  validator-cli-<version>.jar -s <scenario-config-file1> -s <scenario-config-file2> -r <path-to-repo> [OPTIONS] [FILE]

# multiple scenarios, multiple repositories ordered
java -jar  validator-cli-<version>.jar -s <scenario-config-file1> -r <path-to-repo1> -s <scenario-config-file2> -r <path-to-repo2> [OPTIONS] [FILE]
java -jar  validator-cli-<version>.jar -s <scenario-config-file1> -s <scenario-config-file2> -r <path-to-repo1>  -r <path-to-repo2> [OPTIONS] [FILE]

# multiple scenarios, multiple repositories (named)
java -jar  validator-cli-<version>.jar -s "NAME1=<scenario-config-file1>" -s "NAME2=<scenario-config-file2>" -r "NAME1=<path-to-repo1>"  -r "NAME2=<path-to-repo2>" [OPTIONS] [FILE]
```

## Special features

Besides the obvious functionality of validating, the cli provides additional functionality to customize the processing:

| name | option | description | 
| - | - | - | 
| output directory | `-o` | Defines the out directory for results. |
| print mode | `-p` | Print the report to stdout | 
| extract reports | `-e` | Extracts any reports within the result and saves the content to the filesystem. | 
| print memory stats | `-m` | Prints some memory usage information. Mainly for debugging purposes on processing huge xml files | 

## Return codes

| code | description |
|-|-|
| 0  | All validated xml files are acceptable according to the scenario configurations or application usage was requested |
| positive integer | Number of rejected (e.g. not acceptable) xml files according to the scenario configurations| 
| -1 | Parsing error. The commandline arguments specified are incorrect  |
| -2 | Configuration error. There is an error loading the configuration and/or validation targets |
