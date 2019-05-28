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



Voraussetzung für die Verwendung ist eine valide Prüfszenarien-Definition (xml-Datei) und das dazugehörige Repository
mit den von den definierten Szenarien benötigten Artefakten. Der folgende Quellcode zeigt die Erzeugung einer neuen
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

Eine einmal initialisierte Prüfinstanz ist *threadsafe* und kann beliebig oft wieder verwendet
werden. XML-Artefakte wie Schema oder XSLT-Executables werden bei Instantiierung des `DefaultCheck` initialisiert und
wiederverwendet. Da diese Objekte relativ aufwändig zu Erzeugen sind, empfielt sich die Wiederverwendung der `Check`-Instanz.

Die Batch-Verarbeitung erfolgt grundsätzlich seriell. Der `DefaultCheck` implementiert *keine Parallelverarbeitung*.

Einziges Eingabeobjekt ist `Input`, welches sich mit den verschiedenen Methoden der `InputFactory` aus div. Eingabe-Resourcen
erzeugen lässt. Die InputFactory erzeugt für jedes Eingabe-Objekt eine Prüfsumme, die im Report mitgeführt wird. Der
verwendete Algorithmus ist über die `read`-Methoden der `InputFactory` definierbar. Standardmäßig wird _SHA-256_ des JDK
verwendet
