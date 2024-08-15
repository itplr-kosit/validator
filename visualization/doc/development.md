# Development of XRechnung Visualization


## Project Structure

* `src` contains the source files.
* `src/test` contains example instances of invoice files.
* `src/xsd` contains the schema of the intermediate xml.
* `src/xsl` contains the transformation files.

## Dependencies Overview

### Compile Time

That is for creating visualizations.

* Apache FOP
* Saxon HE
* XRechnung-Testsuite

### Testing

* validator-configuration-xrechnung
* VNU HTML Validator

## The build environment

This repository contains an ANT `build.xml` for development and test.

We recommend `Apache Ant` version 1.10.x or newer (but should work with > 1.8.x).

The main `ant` targets for development are:

* `clean` deletes all generated folders i.e. foremost the `build` directory.
* `transform-to-visualization` generates all visualizations from xrechnung-testsuite and test instances in `src/test`
* `test` validates source UBL or CII XML against XRechnung, transforms to XR Sem Model and schema validates results and transforms and test HTML and PDF visualization
* and `dist` (creating the distribution artefact)

However, because of the complex dependencies, you may only expect `transform-to-visualization` target to work without any customizations.

## Test dependencies on the fly

If you build own local custom versions of dependencies such as XRechnung Testsuite or Validator Configuration XRechnung, you can customize the ant build at runtime.

### Test with local Validator Configuration XRechnung

If you want to test with a local validator configuration xrechnung installation set the ant property `validator.repository.dir` to the directory (full path) like e.g. `validator.repository.dir=/mnt/c/data/git-repos/validator-configuration-xrechnung/build` (Linux). 
To execute the `test` target, for example, call

```shell
ant -Dvalidator.repository.dir=/home/renzo/projects/validator-configuration-xrechnung/build test
```

For Windows users:

```shell
ant "-Dvalidator.repository.dir=/c:/dev/git/validator-configuration-xrechnung/build" test
```

### Development properties file

In order to configure more complex adoption to the local development needs, you have to load a set of different properties from a file.

We provide the `development.build.properties.example` file for the most common properties to be set different than default. It contains some documentation.

You have to copy the file to e.g. `development.build.properties` and you have to explicitly provide the property file location at CLI for your development (otherwise tests will always fail or not be executed at all).


## Distribution

The `ant` target `dist` creates the distribution zip Archive for releases.

## Release

### Checklist

* Are all issues scheduled for the release solved?
* Is everything merged to master branch?
* Make sure that CHANGELOG.md is up to date
* Make sure all external contributors are mentioned


### Prepare

* Make sure you committed and pushed everything 
* Create the distribution 
 
   * Use the `clean` target to build and test all from scratch

```
ant clean dist
```

* Tag the last commit according to the following naming rule: `v${xr-visu.version.full}` e.g.
  `git tag v2024-06-20 && git push origin v2024-06-20`

### Publish

* Draft a new release at https://github.com/itplr-kosit/xrechnung-visualization/releases/new
  * Choose the git tag you just created
* Add release title of the following scheme: `XRechnung Visualization ${xr-visu.version.full} compatible with XRechnung ${xrechnung.version}`
* Copy & paste the high quality changelog entries for this release from CHANGELOG.md.
* Upload distribution zip and tick mark this release as a `pre-release`.
* If **all** released components are checked to be okay, then uncheck pre-release.

### Post-Release

* Change the version of this component in `build.xml` to the next release and commit
* bump version
* update CHANGELOG.md

You are done :smile:
