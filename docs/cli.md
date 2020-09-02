# Validator CLI

The validator comes with a commandline interface (CLI) which allows validating any number of input xml files.

The general way using the CLI is:

```shell
java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> [OPTIONS] [FILE] [FILE] [FILE] ...
```

The validator can also read the xml file from the standard input

```shell script
# via redirection
java -jar validationtool-<version>-standalone.jar  -s <scenario-config-file> [OPTIONS] < my-input.xml

# read from pipe
cat my-input.xml |  validationtool-<version>-standalone.jar  -s <scenario-config-file> [OPTIONS]
```

The help option displays further CLI options to customize the process:

```shell
java -jar  validationtool-<version>-standalone.jar --help
```

## Special features
Besides the obvious functionality of validating, the cli provides additional functionality to customize the processing:

|name | option | description |
| - | - | - |
| [Daemon mode](daemon.md) | `-D` | Starts the validator in daemon mode as an HTTP service | 
| print mode | `-p` | Print the report to stdout |
| extract html | `-h` | Extracts any html blocks within the report and saves the content to the filesystem. Note: the file name is derived from the node name the html appears in |
| print memory stats | `-m` | Prints some memory usage information. Mainly for debugging purposes on processing huge xml files |
| check assertions | `-c <file>` | Check assertions on the generated reports. This is mainly useful for scenario developers. Ask KoSIT for documentation, if you want to use this feauture |


## Return codes

| code | description |
|-|-|
| 0  | All validated xml files are acceptable according to the scenario configurations |
| positive integer | Number of rejected (e.g. not acceptable) xml files according to the scenario configurations| 
| -1 | Parsing error. The commandline arguments specified are incorrect  |
| -2 |  Configuration error. There is an error loading the configuration and/or validation targets |
