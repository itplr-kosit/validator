# Validator API

The Validator offers an API which allows you to integrate Validator in your own applications.

## Dependency Management

Currently, we *do not* deploy to Maven Central or similar. Hence you need to build and optionally deploy the Validator artifacts to your own shared repository  (see for example [Maven Documentation](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html)).

### Maven

Then you can declare the dependency as follows:

```xml
<dependency>
   <groupId>de.kosit</groupId>
   <artifactId>validationtool</artifactId>
   <version>${validator.version}</version>
</dependency>
```

### Gradle

```js
dependencies {
    compile group: 'de.kosit', name: 'validationtool', version: '1.0.0'
}
```

## Usage

Prerequisite for use is a valid [scenario definition](configurations.md) and the a folder with all necessary artifacts for validation (repository).

The following example demonstrates  Der folgende Quellcode zeigt die Erzeugung einer neuen
Prüf-Instanz:

```java
//Vorbereitung der Konfiguration
URI scenarios =  URI.create("scenarios.xml");
CheckConfiguration config = new CheckConfiguration();
config.setScenarioDefinition(scenarios);

//Instanziierung der DefaultCheck-Implementierung
Check implemenation =  new DefaultCheck(config);
```

Weitere Konfigurationsoption ist der Pfad zum Repository. Standardmäßig wird das Repository relativ zur Szenarien-Defintion
unter "repository" gesucht.

Die so erzeugte Prüfinstanz initialisiert sämtliche  Szenarien und deren Prüfartefakte. Ein etwaiger Konfigurationsfehler
wird frühzeitig mitgeteilt.

Die eigentlich Prüfung erfolgt mit den beiden Methoden des `Check`-Interfaces:

```java
...
Check pruefer =  new DefaultCheck(config);

//einzelne Datei prüfen
Input pruefKandidat = InputFactory.read(new File("rechnung.xml"));
Document report = pruefer.implemenation(pruefKandidat);

//Batch-Prüfung
List<File> files = Files.list(Paths.get("rechnungen")).map(path -> path.toFile()).collect(Collectors.toList());
List<Input> toCheck = files.stream().map(InputFactory::read).collect(Collectors.toList());
List<Document> reports = pruefer.implemenation(toCheck);

```

Initializing all XML artifacts and XSLT-executables is expensive. The `Check` instance is *threadsafe* and keeps all artifacts. Therefore, we recommend the re-use of an `Check` instance.

* Batch use is serial and *not parallel*

The only input `de.kosit.validationtool.api.Input` which can be created by various methods of `de.kosit.validationtool.api.InputFactory`.
The `InputFactory` calculates a hash sum for each Input which is also written to the Report. _SHA-256_ from the JDK is the default algorithm. It can be changed using the `read`-methods of `InputFactory`.
