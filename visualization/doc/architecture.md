# Architecture

Der Standard XRechnung basiert auf der Europäischen Norm EN16931. Diese Norm besteht 
aus einem semantischen Datenmodell und sogenannten Syntax-Bindings zu den 
vorgegebenen Syntaxen.

Das semantische Datenmodell spezifiziert in nicht-technischer Form mögliche Bestandteile
(Rechnungsnummer, Rechnungsdatum, Rechnungsbetrag, Käufer etc.) elektronischer
Rechnungen in Form von Business Terms (BT), Business Groups (BG) und Business Rules 
(BR). Mit den Syntax-Bindings wird spezifiziert, wie diese Bestandteile in technischer Form 
abgebildet werden müssen. Grundlage dieser Spezifikation sind die beiden durch die Norm 
vorgegebenen Syntaxen UBL und CII und die diesen Syntaxen zugrundeliegenden XML 
Schema-Dateien.

Im Rahmen der Umsetzung der elektronischen Rechnungsbe- und -verarbeitung wurde in 
unterschiedlichen Zusammenhängen die Anforderung formuliert, die durch die Norm 
spezifizierte technische Abbildung einer konkreten Rechnung (= XML-Instanz) in 
strukturierter Form für menschliche Leser\*innen optimiert lesbar anzuzeigen. Bestandteile
dieser Anzeige (Visualisierung) sind zum einen die konkreten Inhalte der elektronischen 
Rechnung und deren Bezug zu den BTs und BGs der Norm. Zum anderen muss das 
Konzept der Visualisierung anwendungsspezifische Anforderungen hinsichtlich Position, 
Reihenfolge und Bezeichnung der Bestandteile einer Rechnung unterstützen.

*Abbildung 1* zeigt einen konzeptionellen Ansatz zur Erzeugung der Visualisierung von 
XRechnungen unter Berücksichtigung der genannten Anforderungen.

![Grundkonzept der Visualisierung von XRechnung](../img/visualization-concept.png)
<figcaption align="center" style="width:80%;"><em>Abbildung 1: Grundkonzept der Visualisierung von XRechnung</em></figcaption>

*Schritt 1* beinhaltet die Rechnungen im Format der durch die Norm geforderten technischen 
Syntaxen (XML-Instanzen in UBL bzw. CII).

*Schritt 2* zeigt die mittels einer bereitgestellten Transformation (XSL-Datei aus den 
gegebenen XML-Instanzen) in eine „syntaxneutrale“ Abbildung der Rechnung, die um die 
Information zu den in der Rechnung genutzten BTs und BGs angereichert ist.

Diese Abbildung ist die Basis für eine zweite Transformation der Rechnung in eine domänen- bzw. anwendungsspezifische Visualisierung im HTML-Format (*Schritt 3*). Die letztendliche 
Visualisierung dieser HTML-Instanzen erfolgt mittels CSS-Dateien, in denen individuelle 
Bezeichner, Positionen und Reihenfolgen der BTs/BGs und der zugehörigen 
Rechnungsinhalte spezifiziert sind. Transformation (XSL-Datei) und zugehörige CSS-Dateien 
werden durch die jeweiligen Anwendungsbereiche bereitgestellt.