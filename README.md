## Inhaltsverzeichnis

- [Über das Prütool](#über-das-prüftool)
- [Status](#status)
- [Verwendung](#verwendung)
- [Build-Anweisungen](#build-anweisungen)
- [Konfiguration xRechnung](#konfiguration-xrechnung)


## Über das Prüftool
 * macht die KoSIT
## Status

## Verwendung
Das Prüftool steht in zwei Varianten zur Verfügung:
- als [**Standalone-Version**](#verwendung-als-anwendung), die von der Kommandozeile aus aufgerufen werden kann 
- als [**Bibliothek**](#verwendung-als-bibliothek), die in eigene Anwendungen integriert werden kann 

### Verwendung als Standalone-Anwendung
```java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> [OPTIONS] [FILE]...```

Eine Liste der möglichen Optionen kann der Hilfe entnommen werden. Diese steht mit folgendem Projektaufruf zur Verfügung

```java -jar  validationtool-<version>-standalone.jar --help```

### Verwendung als Bibliothek
Daneben kann das Prüftool auch in eigene Anwendungen integriert werden. 

Die Bibliothek steht im Maven-Central-Repository zur Verfügung und kann mit kompatiblen Build-Tools genutzt werden. 

* Maven 
```
<dependency>
   <groupId>de.kosit</groupId>
   <artifactId>validationtool</artifactId>
   <version>1.0.0</version>
</dependency>
```
* Gradle
```
dependencies {
    compile group: 'de.kosit', name: 'validationtool', version: '1.0.0'
}
```

Voraussetzung für die Verwendung ist eine valide Prüfszenarien-Definition (xml-Datei) und das dazugehörige Repository 
mit den von den definierten Szenarien benötigten Artefakten. Der folgende Quellcode zeigt die Erzeugung einer neuen Prüf-Instanz:

```java
//Vorbereitung der Konfiguration
URI scenarios =  URI.create("scenarios.xml");
CheckConfiguration config = new CheckConfiguration();
config.setScenarioDefinition(scenarios);

//Instantiierung der DefaultCheck-Implementierung
Check check =  new DefaultCheck(config);
```

Weitere Konfigurationsoption ist der Pfad zum Repository. Standardmäßig wird das Repository relativ zur Szenarien-Defintion
aufgelöst.

Die so erzeugte Prüfinstanz initialisiert sämtliche  Szenarien und deren Prüfartefakte. Ein etwaiger Konfigurationsfehler 
wird frühzeitig mitgeteilt. 

Die eigentlich Prüfung erfolgt mit den beiden Methoden des `Check`-Interfaces:

```java
...
Check pruefer =  new DefaultCheck(config);

//einzelne Datei prüfen
Input pruefKandidat = InputFactory.read(new File("rechnung.xml"));
Document report = pruefer.check(pruefKandidat);

//Batch-Prüfung
List<File> files = Files.list(Paths.get("rechnungen")).map(path -> path.toFile()).collect(Collectors.toList());
List<Input> toCheck = files.stream().map(InputFactory::read).collect(Collectors.toList());
List<Document> reports = pruefer.check(toCheck);

```

Eine einmal initialisierte Prüfinstanz ist **threadsafe** und kann beliebig oft wieder verwendet
werden. XML-Artefakte wie Schema oder XSLT-Executables werden bei Instantiierung des `DefaultCheck` initialisiert und 
wiederverwendet. Da diese Objekte relativ aufwändig zu Erzeugen sind, empfielt sich die Wiederverwendung der `Check`-Instanz.

Die Batch-Verarbeitung erfolgt grundsätzlich seriell. Der `DefaultCheck` implementiert **keine Parallelverarbeitung**.

Einziges Eingabeobjekt ist `Input`, welches sich mit den verschiedenen Methoden der `InputFactory` aus div. Eingabe-Resourcen
erzeugen lässt. Die InputFactory erzeugt für jedes Eingabe-Objekt eine Prüfsumme, die im Report mitgeführt wird. Der
verwendete Algorithmus ist über die `read`-Methoden der `InputFactory` definierbar. Standardmäßig wird _SHA-256_ des JDK 
verwendet

### Build-Anweisungen
Das Projekt wird mit Apache Maven gebaut. 

Mittels `mvn install` wird standardmäßig die Bibliotheks-Variante gebaut. Diese enthält nur die Klassen und
Komponenten für die Prüfung. Abhängigkeiten müssen durch die einbindende Anwendung aufgelöst werden (maven).

Ein `mvn install -Pstandalone` baut die Standalone-Variante. Diese Variante enthält zusätzlich Klassen zur Verarbeitung
von Eingaben aus der Kommandozeile, sowie für Ausgabeoptionen für Ergebnisse. Darüberhinaus ist diese als sog. 
Uber-Jar gebaut, sodass sämtliche Abhängigkeiten im Jar gebundlet sind und das Jar-File somit 'lauffähig' ist.

### Konfiguration xRechnung 
 