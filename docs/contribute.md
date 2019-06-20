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

The configuration can be found in `.settings`-directory. For IntelliJ this is all set up. Additionally this should work in Eclipse out of the box.
Another potential usage scenario would be to integrate the formatter via git hooks into the commit-pipeline (e.g  [Example Hook](https://gist.github.com/ktoso/708972)  ).
For other IDEs you are on your own.
