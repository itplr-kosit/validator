# Quality Management

Some information on how we aim to ensure certain level of quality.

## Measures

* We perform unit tests (see [source code](src/test/java/de/kosit/validationtool) )
* We perform static code analysis using [Sonar](https://docs.sonarqube.org/display/SONAR/Metric+Definitions)


## XML-Security Best Practices

* We follow the [OWASP recommendations](https://github.com/OWASP/CheatSheetSeries/blob/master/cheatsheets/XML_Security_Cheat_Sheet.md)
  on best practices for JAVA XML to mitigate XML eXternal Entity (XXE) attacks and we do not allow external references on Entities and XIncludes per default.
