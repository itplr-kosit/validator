## Inhaltsverzeichnis

- [Über das Prüftool und die Prüftool-Konfiguration XRechnung](#über-das-prueftool-und-die-prüftool-konfiguration-xrechnung)
- [Status der Bestandteile](#status-der-bestandteile)
- [Grundsätzlicher Ablauf der Prüfung](#grundsätzlicher-ablauf-der-prüfung)
- [Verwendung](#verwendung)
- [Build-Anweisungen](#build-anweisungen)
- [Die Konfiguration XRechnung](#die-konfiguration-xrechnung)
- [Qualitätssicherung](#qualitätssicherung)

# Über das Prüftool und die Prüftool-Konfiguration XRechnung
In seiner 23. Sitzung hat der [IT-Planungsrat](https://www.it-planungsrat.de) mit [Beschluss 2017/22 (6a)](https://www.it-planungsrat.de/SharedDocs/Sitzungen/DE/2017/Sitzung_23.html?pos=3) die Koordinierungsstelle für IT-Standards (KoSIT) im Rahmen des Betriebs des Standards XRechnung mit der dauerhaften„…Bereitstellung eines Moduls zur Konformitätsprüfung elektronischer Rechnungen als offene Referenzimplementierung sowie …“ aller zugehöriger Artefakte beauftragt. Im Rahmen dieser Beauftragung wurde die hier bereitgestellte Software "Prüftool" entwickelt und (vor-) konfiguriert.

Das Prüftool ist ein Programm, welches XML-Dateien (Dokumente) in Abhängigkeit von ihren Dokumenttypen gegen verschiedene 
Validierungsregeln (XML Schema und Schematron) prüft und das Ergebnis zu einem Konformitätsbericht (Konformitätsstatus
*valid* oder *invalid*) mit einer Empfehlung zur Weiterverarbeitung (*accept*) oder Ablehnung (*reject*) aggregiert.  Mittels  Konfiguration kann bestimmt werden, welche der Konformitätsregeln durch ein Dokument, das zur Weiterverarbeitung empfohlen (*accept*) wird, verletzt sein dürfen. 

Das Prüftool selbst ist fachunabhängig und kennt keine spezifischen Dokumenttypen. 
Diese werden im Rahmen einer [Prüftool-Konfiguration](#konfiguration-des-prüftools) definiert, welche zur Anwendung des Prüftools
erforderlich ist. 
Mit ausgeliefert wird in diesem Kontext eine Entwurfsversion der [Prüftool-Konfiguration
XRechnung](#die-konfiguration-xrechnung), welche die Prüfartefakte zu der EN16931 (https://github.com/CenPC434/validation) und die Prüfartefakte des Standards
[XRechnung](http://www.xoev.de/de/xrechnung) in ihren aktuellen Entwurfsversionen beinhaltet. 

# Status der Bestandteile
Das Prüftool hat den Status "proposal". Sofern keine Rückmeldungen aus der Pilotierungsphase eingehen, wird diese
Fassung zur finalen Version "1.0.0". Der geregelte Betrieb dieser Referenzimplementierung des Prüftools wird im Rahmen des Betriebs des Standards XRechnung erfolgen.

Die Prüftool-Konfiguration XRechnung hat den Status "draft", da auch die darin enthaltenen Prüfartefakte der EN16931 und
des Standards XRechnung aktuell nur als "draft" vorliegen.

Alle Bestandteile des Pakets werden unter der Apache License Version 2.0, January 2004 (http://www.apache.org/licenses/) bereitgestellt.

# Grundsätzlicher Ablauf der Prüfung
Eine zu prüfende Datei durchläuft die folgenden Schritte   

1. *Grundsätzliche XML-Prüfung*: Es muss sich bei der zu prüfenden Datei um wohlgeformtes XML handeln, andernfalls
   werden keine weiteren Prüfungen durchgeführt und ein [Prüfbericht] mit Status *invalid* und Empfehlung 
    *reject* generiert.
2. *Identifikation des anzuwendenden Prüfszenarios*: Für den Dokumenttyp der zu prüfenden XML-Datei muss in der
    [Konfigurationsdatei](#konfiguration-des-prüftools) ein Prüfszenario definiert sein (die Identifikation des
    Dokumenttyps erfolgt durch einen XPath-Test), andernfalls werden keine weiteren Prüfungen durchgeführt und ein
    [Prüfbericht] mit Status *invalid* und Empfehlung *reject* generiert.
3. *Prüfung gegen das XML-Schema des identifizierten Dokumenttyps*: Das zu prüfende Dokument muss valide bzgl. des
    Schemas sein, andernfalls werden keine weiteren Prüfungen durchgeführt und ein [Prüfbericht] mit Status *invalid*
    und Empfehlung *reject* generiert.
4. *Prüfung gegen die Schematron-Regeln des identifizierten Dokumenttyps*
5. *Aggregation und Bewertung der einzelnen Prüfungen* zu einem [Prüfbericht]: Die Ergebnisse der
    vorherigen Schritte werden in einem einheitlichen Berichtsformat zusammengefasst und bewertet:
    * Sofern mindestens einer der zuvor durchgeführten Prüfschritte einen Fehler (*error*) oder eine Warnung (*warning*)
      geliefert hat, erhält der Prüfbericht den Status *invalid*, andernfalls erhält er den Status *valid*.
    * Sofern einer der Prüfschritte einen Fehler geliefert hat, erhält der Prüfbericht grundsätzlich die Empfehlung
      *reject*, andernfalls erhält er die Empfehlung *accept*. 
    * In der [Konfigurationsdatei](#konfiguration-des-prüftools) kann für einzelne Prüfregeln festgelegt werden, dass
      sie für die Bewertung einer [anderen Meldungsart](#anpassung-der-fehlergrade-für-die-bewertung) zuzuordnen sind
      (z. B. *warning* anstelle von *error*).  
    * Der Prüfbericht ist ein für die maschinelle Auswertung geeignetes XML-Dokument (hier ein
      [Beispiel](configurations/xrechnung/test/reports/ubl002-report.xml)). Darin eingebettet ist auch eine 
      für menschliche Leser bestimmte HTML-Aufbereitung des Prüfergebnisses (hier ein
      [Beispiel](configurations/xrechnung/test/reports/ubl002-report.html)). Die Details dieser HTML-Aufbereitung können
      bei Bedarf [angepasst](#anpassung-der-html-ausgabe) werden.
    
# Verwendung
Das Prüftool steht in zwei Varianten zur Verfügung:
- als [Standalone-Version](#verwendung-als-anwendung), die von der Kommandozeile aus aufgerufen werden kann 
- als [Bibliothek](#verwendung-als-bibliothek), die in eigene Anwendungen integriert werden kann 

# Voraussetzungen
Zur Ausführung und zum Durchführen des Maven-Builds wird Java 8 Update 111 oder höher benötigt.


## Verwendung als Standalone-Anwendung
```java -jar  validationtool-<version>-standalone.jar  -s <scenario-config-file> [OPTIONS] [FILE] [FILE] [FILE] ...```

Eine Liste der möglichen Optionen kann mit den Schalter `--help` angezeigt werden.

Aufruf, um die mitgelieferten Test-Dokumente zu validieren und dabei neben den XML-Prüfberichten auch die eingebetteten
HTML-Dokumente als eingeständige Dateien auszugeben:

```
unzip validationtool-dist-<version>-standalone.zip
unzip xrechnung-<version>.zip
cd xrechnung
java -jar ../validationtool-<version>-standalone.jar -s scenarios.xml -o test/reports -h test/instances/*.xml
```

Der Aufruf erzeugt im Verzeichnis [xrechnung/test/reports](configurations/xrechnung/test/reports/) für jede validierte Eingabedatei
einen gleichnamige [Prüfbericht]-Datei.  
Eine Übersicht über die Eigenschaften der Testdateien in
[xrechnung/test/instances](configurations/xrechnung/test/instances/) findet sich in
[xrechnung/test/assertions.xlsx](configurations/xrechnung/test/assertions.xlsx).  

## Verwendung als Bibliothek
Daneben kann das Prüftool auch in eigene Anwendungen integriert werden. 

Die Bibliothek steht derzeit noch *nicht* im Maven-Central-Repository zur Verfügung. Sie muss manuell im lokalen oder 
unternehmensweiten Maven-Repository bereitgestellt werden (siehe [vgl. Maven Dokumentation](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html)).  

 

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
mit den von den definierten Szenarien benötigten Artefakten. Der folgende Quellcode zeigt die Erzeugung einer neuen
Prüf-Instanz: 

```java
//Vorbereitung der Konfiguration
URI scenarios =  URI.create("scenarios.xml");
CheckConfiguration config = new CheckConfiguration();
config.setScenarioDefinition(scenarios);

//Instanziierung der DefaultCheck-Implementierung
Check check =  new DefaultCheck(config);
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
Document report = pruefer.check(pruefKandidat);

//Batch-Prüfung
List<File> files = Files.list(Paths.get("rechnungen")).map(path -> path.toFile()).collect(Collectors.toList());
List<Input> toCheck = files.stream().map(InputFactory::read).collect(Collectors.toList());
List<Document> reports = pruefer.check(toCheck);

```

Eine einmal initialisierte Prüfinstanz ist *threadsafe* und kann beliebig oft wieder verwendet
werden. XML-Artefakte wie Schema oder XSLT-Executables werden bei Instantiierung des `DefaultCheck` initialisiert und 
wiederverwendet. Da diese Objekte relativ aufwändig zu Erzeugen sind, empfielt sich die Wiederverwendung der `Check`-Instanz.

Die Batch-Verarbeitung erfolgt grundsätzlich seriell. Der `DefaultCheck` implementiert *keine Parallelverarbeitung*.

Einziges Eingabeobjekt ist `Input`, welches sich mit den verschiedenen Methoden der `InputFactory` aus div. Eingabe-Resourcen
erzeugen lässt. Die InputFactory erzeugt für jedes Eingabe-Objekt eine Prüfsumme, die im Report mitgeführt wird. Der
verwendete Algorithmus ist über die `read`-Methoden der `InputFactory` definierbar. Standardmäßig wird _SHA-256_ des JDK 
verwendet


# Build-Anweisungen
Das Projekt wird mit Apache Maven gebaut. 

Mittels `mvn install` werden im Unterverzeichnis `dist` zwei Pakete gebaut:

* die *Standalone-Distribution*  enthält das Uber-Jar mit allen Klassen zur Verarbeitung von Eingaben aus der Kommandozeile, 
sowie für Ausgabeoptionen für Ergebnisse. Sämtliche Abhängigkeiten sind im Jar gebundlet  und das Jar-File ist 'ausführbar'.

* die *Full-Distribution* enthält darüber sämtlichen weiteren Varianten des `validationtools` sowie die benötigten Abhängigkeiten.

# Konfiguration des Prüftools
Die Konfiguration besteht aus einer Konfigurationsdatei (XML-Dokument im Namensraum
`http://www.xoev.de/de/validator/framework/1/scenarios`) sowie Resourcen (XML Schemata und XSLT-Dateien), auf welche die
Konfigurationsdatei verweist.

Der Aufbau der Konfigurationsdatei ist im entsprechenden Schema [scenarios.xsd](validationtool/src/main/model/xsd/scenarios.xsd) erläutert.



# Die Konfiguration XRechnung
Die Konfiguration XRechnung findet sich im Verzeichnis [xrechnung/](configurations/xrechnung/).
Für den produktiven Betrieb benötigt werden die Konfigurationsdatei [xrechnung/scenarios.xml](configurations/xrechnung/scenarios.xml) und das
Ressourcen-Verzeichnis [xrechnung/resources/](configurations/xrechnung/resources/). 

Die Konfiguration beinhaltet Prüfszenarien für die folgenden Dokumenttypen:

* EN16931 CIUS XRechnung (UBL Invoice)
* EN16931 CIUS XRechnung (UBL CreditNote)
* EN16931 CIUS XRechnung (CII)

Jedes Szenario prüft die Konformität zu der zugrunde liegenden XML-Schema-Datei, den Schematron-Regeln der EN16931 und
den Schematron-Regeln der XRechnung CIUS.

## Prüfbericht
Der Aufbau des Prüfberichts ist im entsprechenden Schema [report.xsd](configurations/xrechnung/resources/report.xsd) erläutert.
Die für die maschinelle Auswertung des Prüfberichts wesentlichsten Angaben sind

* der *Konformitätsstatus* (*valid* oder *invalid*, Attribut rep:report/@valid)
* die Empfehlung zur Annahme (*accept* - Element rep:report/rep:assessment/rep:accept) oder Ablehnung
  (*reject* - Element rep:report/rep:assessment/rep:reject) des geprüften
  Dokuments.  


## Anpassung der Fehlergrade für die Bewertung
Grundsätzlich werden für die Verarbeitungen alle Meldungen, welche aus den einzelnen
[Prüfschritten](#grundsätzlicher-ablauf-der-prüfung) resultieren, in die Rollen *error*,
*warning* und *information* übersetzt. Der Prüfbericht erhält den Konformitätstatus *valid* genau dann, wenn in der
Konfiguration ein Prüfszenario für den Dokumenttyp des zu testenden Dokuments gefunden wurde und keine Meldung mit
Status *error* oder *warning* vorliegt. 

Die Erstellung dieser Bewertung ist nicht konfigurierbar.

In der Standardkonfiguration erhält der Prüfbericht genau dann die Empfehlung *accept*, wenn in der Konfiguration ein
Prüfszenario für den Dokumenttyp des zu testenden Dokuments gefunden wurde und keine Meldung mit Status *error* vorliegt.

Die Erstellung dieser Empfehlung kann *je Prüfszenario* konfiguriert werden, in dem im jeweiligen Prüfszenario in
`createReport` ein `customLevel` aufgenommen wird:

```
      <scenario>
        <name>EN16931 CIUS XRechnung (UBL Invoice)</name>
        ...
        <createReport>
            <resource>
                <name>Prüfbericht für XRechnung</name>
                <location>resources/xrechnung/xrechnung-report.xsl</location>
            </resource>
            <customLevel level="warning">BR-15 BR-DE-3</customLevel>
        </createReport>
    </scenario>
```

In diesem Beispiel werden die Fehlercodes `BR-15` (Teil der EN) und `BR-DE-3` (Teil der CIUS XRechnung) für den
Bewertungsschritt von ihrem eigentlicher Rolle *error* auf *warning* geändert. Ein Dokument, welches eine oder
beide dieser Regeln verletzt (und ansonsten keine *error*-Meldungen erzeugt) erhielte damit abweichend vom
Standardverhalten die Bewertung *accept*.   

## Anpassung der HTML-Ausgabe
Die Konfiguration XRechnung erstellt XML-Prüfberichte, welche für jede Bewertung (*accept* oder *reject*-Kindelement im
Prüfbericht) genau eine HTML5-Darstellung enthalten.  

Diese wird durch das XSLT-Skript `xrechnung-report.xsl` erstellt. Dieses Skript kann überschrieben werden um die
HTML-Ausgabe anzupassen. Dazu ist eine neue XSTL-Datei (z. B. `my-xrechnung-report.xsl`) zu erstellen, welche
`xrechnung-report.xsl` per `xsl:import` einbindet. Die neue XSLT-Datei ist anstelle von `xrechnung-report.xsl` in der
Konfigurationsdatei einzutragen. In der neuen XSLT-Datei kann nun das XSLT-Template `html:html` oder eines der von
diesem eingebundenen Unter-Templates `html:*` überschrieben werden.  

Für weiterführende Erläuterungen wird auf die Dokumentation in der XSLT `xrechung\resources\default-report.xsl`
verwiesen.  

# Qualitätssicherung

## Umgesetzte QS-Maßnahmen

### Automatische Unit-Tests (Java-Code)
* Die korrekte Funktionsweise des Prüftools wird durch mehr als 60 Unit-Test überprüft. 
* Die Unit-Tests sind Teil des bereitgestellten Codes und werden durch den Maven-Build automatisch ausgeführt. 
* Die Unit-Tests decken alle grundsätzlichen Funktionen des Prüftools ab. Daneben wird  das korrekte Verhalten der
  Anwendung bei verschiedenen Fehleingaben überprüft und nachgewiesen. 
* Die Testabdeckung (Coverage) liegt derzeit bei ca. 85% des Java-Codes. 
  Diese Abdeckung wird mittels der Bibliothek jacoco automatisch ermittelt und zeigt, dass alle wesentlichen Funktionen
  durch Tests überprüft werden.  
  Die verbleibenden 15% lassen sich fast ausschließlich auf Fehlerbedingungen (Exceptions) zurückführen,  
  die in der Praxis auch bei Fehleingaben nicht auftreten können und entsprechend durch keine Unit-Tests durchlaufen
  werden. 

### Automatische Code-Analyse (Java-Code)
* Der Quellcode wird dauerhaft und automatisch durch das weit verbreitete System [Sonar](https://www.sonarqube.org/) zur
  statischen Code-Analyse geprüft.    
* Das Prüftool wird von Sonar mit aktuell ca 1.800 Zeilen Quellcode als klein (S) eingestuft. 
* Es existieren aktuelle 7 "Code Smells" und 3 "False Positives". 
* Sämtliche „Code Smells“ sind auf nicht abgetestete Bedingungen (siehe oben) zurückzuführen. 
* Ein Beispiel für ein "False Positive" ist "Illegale Ausgabe auf STDout", was jedoch eine konkrete Anforderung an das
  Prüftool ist. 
* In den Aspekten "Reliability", "Security" und "Maintainability" wird der Quellcode jeweils mit dem bestmöglichen
  [Rating](https://docs.sonarqube.org/display/SONAR/Metric+Definitions) "A" bewertet.   

### Berücksichtigung von Best Practices für XML-Security
* Es wurden explizit Best Practices für die sichere XML-Verarbeitung mit Java (XML, XML Schema, XSLT) berücksichtigt, um
  beispielsweise XXE (XML eXternal Entity) Attacken und allgemein externe Referenzierungen (Entities, XIncludes)
  auzuschließen. 
 
### End-to-End-Testsuite für die Prüftool-Konfiguration XRechnung
* Um die korrekte Funktion der Prüftool-Konfiguration XRechnung zu testen, wurde eine Suite aus 10 Testdokumenten und
  insgesamt 310 prüfbaren Aussagen (Assertions) über die resultierenden Prüfberichte erstellt.
* Durch diese Testsuite werden, ausgehend von dem Prüfbericht-Schemas alle möglichen Optionen und Auswahlmöglichkeiten
  mindestens je einmal positiv  und einmal negativ getestet.  
* Diese Zusicherungen können vom Prüftool selbst mittels des Schalter `--check-assertions` automatisch geprüft werden.
* Zudem wird die Integrität aller erstellten Prüfberichte automatisch gegen das Schema (XML Schema und
  Schematron-Regeln) des Prüfberichts getestet. 
* Für weitere Details siehe [xrechnung/test/readme.txt](configurations/xrechnung/test/readme.txt).   


## Noch nicht umgesetzte QS-Maßnahmen

### Internes Security-Audit (Java-Code)
Ein abschließendes Security Audit durch den Dienstleister läuft noch und wird voraussichtlich in der KW40 abgeschlossen sein. 

### Fachlicher Test der Prüftool-Konfiguration XRechnung
Die Korrektheit der in der Prüftool-Konfiguration XRechnung enthaltenen Schematron-Dateien bzw. der aus ihnen erstellten
XSLT-Kompilate wurde noch nicht systematisch geprüft, da weder die Schematron-Dateien der EN16931 noch die
Schematron-Dateien des Standards XRechnung in finalen Fassungen vorlagen.
