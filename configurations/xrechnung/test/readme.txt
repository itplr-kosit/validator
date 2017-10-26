Dieses Verzeichnis enthält Tests für die Prüftool-Konfiguration XRechnung.
Die Tests können über das ANT-Skript build.xml ausgeführt werden.

Inhalt des Verzeichnisses:
 - instances/*.xml      - Test-Dokumente (Rechnungen)
 - reports/*.xml|*.html - Die Prüfberichte für die Test-Dokumente
 - scenarios.xml        - Ein für Testzwecke angepasste Variante der 
                          XRechnung-Konfigurationsdatei (customLevels ergänzt)
 - assertions.xlsx      - Eine tabellarische Darstellung der Eigenschaften
                          der Prüfberichte für die einzelnen Dokumente
 - assertions.xml       - maschinenlesbare Form für den Schalter --check-assertions.
 - build.xml            - Vollständiger Testlauf
 - saxon9he.jar         - Saxon-XSLT, wird nur für die Testauswertung benötigt.

Der vollständige Testlauf umfasst:
 - Prüftool auf alle Testdateien anwenden
 - Zusicherungen aus assertions.xml testen
 - Testen, dass alle XML-Berichte konform zu report.xsd sind.
 - Testen, dass alle XML-Berichte konform zu report.sch sind.
                   						 