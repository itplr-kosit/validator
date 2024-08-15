# About

Die zur Umsetzung des beschriebenen Konzepts benötigten Komponenten beinhalten 
verschiedene Bestandteile.

Elektronische Rechnungen, die visualisiert werden sollen, sind XRechnungskonforme XML-Instanzen in den vorgegebenen Syntaxen UBL und CII. Diese können mittels von der KoSIT 
bereitgestellten Transformationsskripten in das syntaxneutrale Format überführt werden. 

Eine beispielhafte Visualisierung des Kompetenzzentrums für das Kassen- und 
Rechnungswesen des Bundes (KKR) ist mittels eines weiteren Transformationsskriptes in 
(X)HTML-Format möglich und kann anhand von CSS-Datei(en) zur Umsetzung von Layout 
und Labeln gestaltet werden. Diese Visualisierung dient der Veranschaulichung und kann 
anwendungsspezifisch angepasst werden.

Die Komponenten zur Visualisierung bestehen aus den folgenden Teilen:

| # | Komponente - Beschreibung | Verantwortung |
| :--- | :---: | :---: |
| 1. | Transformationsskripte (XSLTs) zur Überführung von Rechnungen im UBL-Format und CII-Format (konform zu XRechnung) in das syntaxneutrale Format | KoSIT |
| 2. | XML Schema-Definition (XSD) zur Validierung der erzeugten Rechnungen im syntaxneutralen Format | KoSIT |
| 3. | Transformationsskript (XSLT) zur Überführung von syntaxneutralen Rechnungen in das (X)HTML-Format | KKR |
| 4. | CSS-Datei(en) zur Umsetzung von Layout und Labeln | KKR |
<caption style="align:center;"><em>Abbildung 2: Bestandteile und Verantwortlichkeiten</em></caption>

Als Betreiberin des Standards XRechnung stellt die KoSIT die *Bestandteile 1 und 2* bereit, das KKR liefert die *Bestandteile 3 und 4* zur beispielhaften Visualisierung.