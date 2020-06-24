# Changelog

All notable changes to the Schematron Rules and this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## 1.2.1
### Fixed
- Validator was creating invalid createReportInput xml in case of no scenrio match

## 1.2.0
### Added
- Provide access to schematron result through [Result.java](https://github.com/itplr-kosit/validator/blob/master/src/main/java/de/kosit/validationtool/api/Result.java)
  - *Result#getFailedAsserts()* returns a list of failed asserts found by schematron
  - *Result#isSchematronValid()* convinience access to evaluate whether schematron was processed without any *FailedAsserts*
### Changed
- *Result#getAcceptRecommendation()* does not _only_ work when _acceptMatch_ is configured in the scenario
  - schema correctness is a precondition, if the checked instance is not valid, this evaluates to _REJECTED_
  - if _acceptMatch_ is configured, the result is based on the boolean result of the xpath expression evaluated against the generated report
  - if *no* _acceptMatch_ is configured, the result is based on evaluation of schema and schematron correctness
  - _UNDEFINED_ is only returned, when processing is stopped somehow
- *Result#isAcceptable()* can now evaluate to true, when no _acceptMatch_ is configured (see above)
 
## 1.1.3
### Fixed
- XXE vulnerability when reading xml documents with Saxon [#44](https://github.com/itplr-kosit/validator/issues/44)
- validator unintentionally stopped when schematron processing has errors. See  [#41](https://github.com/itplr-kosit/validator/issues/41).

## 1.1.2
### Fixed
- NPE in Result.getReportDocument for malformed xml input

## 1.1.1
### Added
- Convenience method for accessing information about well-formedness in Result
- Convenience method for accessing information about schema validation result in Result
### Fixed
- NPE when validating non-XML files

## 1.1.0
### Added
- Enhanced API-Usage e.g. return _Result_ object with processing information
- Support loading scenarios and content from a JAR-File
- Simple Daemon-Mode exposing validation functionality via http
- cli option to serialize the 'report input' xml document to _cwd_ (current working directory)
- Documentation in `docs` folder

### Changed
- Use s9api (e.g. XdmNode) internally for loading and holding xml objects (further memory optimization)
- Builds with java 8 and >= 11
- Packages for java8 and java >= 11 (with jaxb included)
- Translated README.md

## 1.0.2

### Fixed

- Memory issues when validating multiple targets

## 1.0.1

### Changed

- Removed XRechnung configuration from release artifacts and source (moved to [own repository](https://github.com/itplr-kosit/validator-configuration-xrechnung) )

## 1.0.0

- Initial Release
