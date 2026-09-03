# Vergleich Validator 1.6 ↔ kanonische Pipeline 2.0 (Steps 2–8)

Vollständig selbst-enthaltener Vergleichslauf vom 20.08.2026: identische Eingaben, beide
Validatoren, alle Ausgaben gespeichert.

## Verzeichnisstruktur

```
comparison/
├── input/                              ← EXAKTE EINGABEN (für beide Läufe identisch)
│   ├── scenarios-v1.6.xml              ← Original XRechnung 3.0.2 Konfiguration (Framework 1)
│   ├── scenarios-v2.0-framework2.xml   ← Konvertierung für 2.0 (Namespace-Swap, frameworkVersion,
│   │                                      <id> je createReport, noScenarioReport entfernt —
│   │                                      Szenarien/Matches/Artefakte/customLevel unverändert)
│   ├── repository/resources/           ← Artefakt-Repository (XSDs, kompilierte Schematron-XSLs,
│   │                                      Report-XSLs) — Kopie aus validator-configuration-xrechnung/build
│   └── instances/                      ← 86 Testinstanzen der XRechnung-Testsuite
│       ├── business-cases/standard/    (66) · business-cases/extension/ (6)
│       └── technical-cases/cius/ (12)  · technical-cases/cvd/ (2)
│
├── v1_6/reports/                       ← AUSGABEN Validator 1.6.0 (validator-1.6.0-standalone.jar)
│   └── **/<instanz>-report.xml         ← offizielle varl-Reports (rep:accept/reject, rep:message)
│
├── v2_0/                               ← AUSGABEN kanonische Pipeline 2.0 (Steps 2–8)
│   ├── reports/**/<instanz>-report.md  ← je Instanz: Verdikt, Szenario, SHA-512, Conformance je
│   │                                      RuleSet, kompletter Detection-Trace (Steps 2–8)
│   ├── xrechnung-e2e-summary.md        ← Übersichtstabelle
│   └── xrechnung-e2e-details.md        ← Nicht-INFO-Findings je Instanz
│
├── comparison.md                       ← INSTANZ-FÜR-INSTANZ-VERGLEICH (86 Zeilen):
│                                          Verdikt 1.6 | Verdikt 2.0 | Match | Findings beidseitig
└── build-comparison.sh                 ← erzeugt comparison.md neu aus beiden Report-Sätzen
```

## Läufe reproduzieren

```bash
# 1.6 (je Instanz-Ordner):
java -jar .../lib/validator-1.6.0-standalone.jar -s input/scenarios-v1.6.xml \
     -r input/repository -o v1_6/reports/<ordner> input/instances/<ordner>/*.xml

# 2.0:
mvn -f validator/pom.xml -pl core test-compile org.codehaus.mojo:exec-maven-plugin:3.1.0:java \
    -Dexec.mainClass=org.kosit.validator.impl.conformatron.XRechnungE2ERunner -Dexec.classpathScope=test \
    -De2e.scenarios=.../input/scenarios-v2.0-framework2.xml -De2e.repository=.../input/repository \
    -De2e.instances=.../input/instances -De2e.output=.../v2_0 -De2e.reports=.../v2_0/reports

# Vergleich:
bash build-comparison.sh
```

## Ergebnis auf einen Blick

| | 1.6 | 2.0 |
|---|---|---|
| Verdikt | 86× ACCEPT, 0× REJECT | 82× CONFORMANT, 4× NON_CONFORMANT |
| Verdikt-Übereinstimmung | **82 / 86** | |
| Finding-Übereinstimmung (Codes + Anzahl) | **86 / 86 — vollständig deckungsgleich** | |

Die 4 Verdikt-Abweichungen (extension/04.05a, extension/05.01a, beide cvd-Fälle): **beide
Validatoren finden exakt dieselben Regelverletzungen** (BR-CL-10/13/21, BR-CO-16). 1.6 akzeptiert
trotzdem, weil seine Akzeptanz-Entscheidung über den `acceptMatch`-XPath auf dem gerenderten
Report läuft, in den die `customLevel`-Herabstufungen (diese Codes → information/warning)
eingerechnet sind. Die 2.0-Pipeline bewertet detection-basiert und wendet `customLevel` noch
nicht an (offene Frage step-07) bzw. `acceptMatch` noch gar nicht (braucht das Report-Modell,
ADR-004-Follow-up).

**Lesart:** Die Regel-Engine der kanonischen Pipeline ist mit 1.6 identisch bis auf die
Bewertungsschicht; die verbleibende Differenz ist vollständig lokalisiert (customLevel/acceptMatch)
und verschwindet mit deren Implementierung.

## Erweiterung 03.09.2026 — Baseline auf 150 Instanzen

Für das One-Shot-Experiment (`experiments/oneshot-validator-1.6/`) wurde der 1.6-Lauf auf die 64
Konfigurationstest-Instanzen (`config-cases/`) ausgedehnt, die seit dem 2.0-Lauf vom 31.08. dazugekommen
waren. `v1_6/reports/` enthält damit **150** Reports, `comparison.md` 150 Zeilen.

| | 1.6 | 2.0 (mit customLevel, Stand 03.09.) |
|---|---|---|
| Verdikt | 118× ACCEPT, 32× REJECT | 118× CONFORMANT, 29× NON_CONFORMANT, 3× FAILED |
| Verdikt-Übereinstimmung | **150 / 150** | (147 wörtlich; 3 nur im Label: `REJECT` ↔ `FAILED@PARSE_DOCUMENT`/`DETECT_SCENARIOS` für die zwei Processing-Error- und die eine No-Match-Instanz) |

Die 4 Abweichungen vom 20.08. sind mit der customLevel-Implementierung in Step 7 geschlossen.
