# Setup development environment

## Prerequisites

We use IntelliJ >= 2018.3  and OpenJDK 11 for development. If you like to contribute please use the following plugins:

1. Checkstyle Plugin
1. Lombok Plugin
1. Eclipse Formatter Plugin (see below)

Any other IDE should be sufficient too. Just use suitable tools, plugins and setup comparable to our IntelliJ stuff.

## Import into IDE

For IntelliJ we provide a suitable configuration within the code. Just clone the repository locally and open it in IntelliJ
and you are up and running.

For other IDEs the correct setup is up to you.

## Code Formatting

We use an automatic formatting of the source code in our environment. This is based on the Eclipse code formatter functionality
due to historical reasons. This not only works in Eclipse but also in IntelliJ (via plugin) and can be used standalone.

The configuration can be found in `.settings`-directory. For IntelliJ this is all set up. The correct formatting is validated as part of the build 
process. If your IDE does not support the eclipse formatter, your can run the maven build locally prior commit like this:

```shell script
mvn package -Pformat
```

This will format all changed files according to our rules. Afterwards your can commit and push your changes.

## Build

### Requirements

* Maven > 3.0.0
* Java > 8 update 111

### Procedure

 `mvn install` generates two different packages in the `dist` directory: