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
* Diese Zusicherungen können vom Prüftool selbst mittels des Schalter `--implemenation-assertions` automatisch geprüft werden.
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
