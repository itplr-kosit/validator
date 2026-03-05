package org.kosit.validator.api.compact;

import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.api.XmlError;
import org.kosit.validator.model.xvrl.*;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;

import javax.xml.namespace.QName;
import java.util.*;
import java.util.stream.Collectors;

import static org.kosit.validator.api.compact.CompactXVRLReportSummary.CVRL_NS;
import static org.kosit.validator.api.compact.CompactXVRLReportSummary.CVRL_PREFIX;

/**
 * Kompakter XVRL-Report mit Komfort-Zugriff auf additive Attribute.
 */
public class CompactXVRLReport {

    public record ValidationResult(String type, String name, List<Violation> violations) {

        @Override
        public List<Violation> violations() {
            return Optional.ofNullable(violations).orElse(List.of());
        }
    }

    public record Violation(String severity, Long line, Long row, String id, String message) {
    }

    private static final String ID_SCENARIO = "scenario";

    private static final String ATTR_ACCEPTANCE = "acceptance";

    private static final String ATTR_ERROR_SUMMARY = "error-summary";

    private static final String ATTR_CHECKSUM = "checksum";

    private static final String VAL_UNKNOWN = "unknown";

    private static final String CODE_XSD_VALIDATION = "xsd";

    private static final String CODE_SCHEMATRON_VALIDATION = "schematron";

    private static final String CODE_XSD_VIOLATION = "xsd-violation";

    private static final String CODE_SCHEMATRON_VIOLATION = "schematron-violation";

    private static final String ATTR_LANGUAGE = "language";

    private final XVRLReport original;

    public CompactXVRLReport(XVRLReport original) {
        this.original = original;
    }

    /**
     * Erstellt eine neue Instanz mit einem leeren zugrunde liegenden XVRLReport.
     *
     * @return neue Instanz von {@link CompactXVRLReport}
     */
    public static CompactXVRLReport create() {
        return new CompactXVRLReport(new ObjectFactory().createXVRLReport());
    }

    /**
     * Setzt den Dateinamen/Pfad im Report.
     *
     * @param href der Dateiname oder Pfad
     */
    public void setFilename(String href) {
        if (original.getMetadata() == null) {
            original.setMetadata(new ObjectFactory().createXVRLMetadata());
        }
        Document doc = new ObjectFactory().createDocument();
        doc.setHref(href);
        original.getMetadata().getDocuments().clear();
        original.getMetadata().getDocuments().add(doc);
    }

    public String getFilename() {
        return Optional.ofNullable(original.getMetadata()).map(m -> m.getDocuments()).stream().flatMap(Collection::stream)
                .map(Document::getHref).findFirst().orElse(null);
    }

    /**
     * Setzt den Ersteller des Reports.
     *
     * @param name Name des Erstellers
     */
    public void setCreator(String name) {
        if (original.getMetadata() == null) {
            original.setMetadata(new ObjectFactory().createXVRLMetadata());
        }
        Creator creator = new ObjectFactory().createCreator();
        creator.setName(name);
        original.getMetadata().getCreators().clear();
        original.getMetadata().getCreators().add(creator);
    }

    /**
     * Setzt das ausgewählte Szenario.
     *
     * @param scenario Name des Szenarios
     */
    public void setScenario(String scenario) {
        XVRLDetection d = new ObjectFactory().createXVRLDetection();
        d.setId(ID_SCENARIO);
        d.setCode(scenario);
        original.getDetection().removeIf(det -> ID_SCENARIO.equals(det.getId()));
        original.getDetection().add(d);
    }

    /**
     * Gibt das ausgewählte Szenario zurück. Laut Ziel-XML wird dies über eine Detection mit der xml:id 'scenario' und
     * dem Attribut 'code' abgebildet.
     *
     * @return Name des Szenarios oder null
     */
    public String getScenario() {
        return original.getDetection().stream().filter(d -> ID_SCENARIO.equals(d.getId())).map(XVRLDetection::getCode).findFirst()
                .orElse(null);
    }

    /**
     * Setzt den Akzeptanzstatus im CVRL-Namespace.
     *
     * @param recommendation Akzeptanzempfehlung
     */
    public void setAcceptance(AcceptRecommendation recommendation) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_ACCEPTANCE, CVRL_PREFIX), recommendation.name());
    }

    /**
     * Gibt den Akzeptanzstatus aus dem CVRL-Namespace zurück. Dieser befindet sich direkt am Report-Element.
     *
     * @return Akzeptanzempfehlung oder null
     */
    public AcceptRecommendation getAcceptance() {
        String val = original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_ACCEPTANCE));
        if (val == null) {
            return AcceptRecommendation.UNDEFINED;
        }
        try {
            return AcceptRecommendation.valueOf(val.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AcceptRecommendation.UNDEFINED;
        }
    }

    /**
     * Setzt die Fehlerszusammenfassung im CVRL-Namespace.
     *
     * @param summary Fehlerszusammenfassung
     */
    public void setErrorSummary(String summary) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_ERROR_SUMMARY, CVRL_PREFIX), summary);
    }

    /**
     * Gibt die Fehlerszusammenfassung aus dem CVRL-Namespace zurück.
     *
     * @return Fehlerszusammenfassung oder null
     */
    public String getErrorSummary() {
        return original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_ERROR_SUMMARY));
    }

    /**
     * Setzt die Checksumme im CVRL-Namespace am Dokument-Element.
     *
     * @param checksum Checksumme
     */
    public void setChecksum(String checksum) {
        if (original.getMetadata() == null || original.getMetadata().getDocuments().isEmpty()) {
            setFilename(VAL_UNKNOWN); // Sicherstellen, dass ein Dokument existiert
        }
        original.getMetadata().getDocuments().get(0).getOtherAttributes().put(new QName(CVRL_NS, ATTR_CHECKSUM, CVRL_PREFIX), checksum);
    }

    /**
     * Gibt die Checksumme aus dem CVRL-Namespace zurück. Diese befindet sich im Ziel-XML am document-Element innerhalb
     * der Metadata.
     *
     * @return Checksumme oder null
     */
    public String getChecksum() {
        return Optional.ofNullable(original.getMetadata()).map(m -> m.getDocuments()).stream().flatMap(Collection::stream)
                .map(d -> d.getOtherAttributes().get(new QName(CVRL_NS, ATTR_CHECKSUM))).filter(c -> c != null).findFirst().orElse(null);
    }

    /**
     * Fügt eine Schema-Violation hinzu.
     *
     * @param error das Schema-Fehlerobjekt
     */
    public void addSchemaViolation(XmlError error) {
        ObjectFactory of = new ObjectFactory();
        XVRLDetection d = of.createXVRLDetection();
        d.setCode(CODE_XSD_VIOLATION);
        d.setSeverity(mapSeverity(error.getSeverity()));

        // Nachricht
        XVRLMessage msg = of.createXVRLMessage();
        msg.getContent().add(error.getMessage());
        d.getMessages().add(msg);

        // Location/Provenance
        if (error.getRowNumber() != null || error.getColumnNumber() != null) {
            Location loc = of.createLocation();
            if (error.getRowNumber() != null) {
                loc.setLine(error.getRowNumber().longValue());
            }
            if (error.getColumnNumber() != null) {
                loc.setColumn(error.getColumnNumber().longValue());
            }
            Provenance prov = of.createProvenance();
            prov.getLocation().add(loc);
            d.getProvenances().add(prov);
        }

        original.getDetection().add(d);
    }

    public XVRLDetection getSchemaViolation() {
        return original.getDetection().stream().filter(d -> CODE_XSD_VIOLATION.equals(d.getCode())).findFirst().orElse(null);
    }

    /**
     * Fügt eine Schematron-Violation hinzu.
     *
     * @param failedAssert die Schematron-Fehlermeldung
     * @param schemaHref die Referenz auf das verwendete Schema (z.B. href oder Titel)
     */
    public void addSchematronViolation(FailedAssert failedAssert, String schemaHref) {
        ObjectFactory of = new ObjectFactory();
        XVRLDetection d = of.createXVRLDetection();
        d.setCode(CODE_SCHEMATRON_VIOLATION);
        // Im Ziel-XML ist severity oft info, wir nehmen hier aber den Role-Wert falls vorhanden
        if (failedAssert.getRole() != null) {
            try {
                d.setSeverity(XVRLDetection.Severity.fromValue(failedAssert.getRole().toLowerCase()));
            } catch (IllegalArgumentException e) {
                d.setSeverity(XVRLDetection.Severity.INFO);
            }
        } else {
            d.setSeverity(XVRLDetection.Severity.INFO);
        }

        // Nachricht
        XVRLMessage msg = of.createXVRLMessage();
        if (failedAssert.getText() != null) {
            msg.getContent().addAll(failedAssert.getText().getContent());
        }
        d.getMessages().add(msg);

        // Schema-Referenz via Provenance/Location
        if (schemaHref != null) {
            Location loc = of.createLocation();
            loc.setHref(schemaHref);
            Provenance prov = of.createProvenance();
            prov.getLocation().add(loc);
            d.getProvenances().add(prov);
        }

        original.getDetection().add(d);
    }

    public List<XVRLDetection> getSchematronViolations() {
        return original.getDetection().stream().filter(d -> CODE_SCHEMATRON_VIOLATION.equals(d.getCode())).toList();
    }

    public ValidationResult getSchemaValidationResult() {
        Schema validationRef = getSchemaReferences(CODE_XSD_VALIDATION).stream().findFirst().orElse(null);
        String type = validationRef.getOtherAttributes().get(new QName(CVRL_NS, ATTR_LANGUAGE));
        XVRLDetection det = getSchemaViolation();
        List<Violation> violations = new ArrayList<>();
        if (det != null) {
            long line = det.getProvenances().stream().flatMap(p -> p.getLocation().stream()).findFirst().map(l -> l.getLine().intValue())
                    .orElse(0);
            long col = det.getProvenances().stream().flatMap(p -> p.getLocation().stream()).findFirst().map(l -> l.getColumn().intValue())
                    .orElse(0);
            violations.add(new Violation(det.getSeverity().value(), line, col, null,
                    det.getMessages().stream().map(m -> String.join(";", m.getMessageStrings())).collect(Collectors.joining(";"))));
        }
        return new ValidationResult(type, validationRef.getHref(), violations);
    }

    public List<ValidationResult> getSchematronValidationResult() {
        List<Schema> validationRef = getSchemaReferences(CODE_SCHEMATRON_VALIDATION);
        List<XVRLDetection> dets = getSchematronViolations();
        return validationRef.stream().map(s -> {
            String type = s.getOtherAttributes().get(new QName(CVRL_NS, ATTR_LANGUAGE));
            XVRLDetection det = dets.stream().filter(
                    d -> d.getProvenances().stream().anyMatch(p -> p.getLocation().stream().anyMatch(l -> l.getHref().equals(s.getHref()))))
                    .findFirst().orElse(null);
            List<Violation> violations = new ArrayList<>();
            if (det != null) {
                violations.add(new Violation(det.getSeverity().value(), null, null, det.getId(),
                        det.getMessages().stream().map(m -> String.join(";", m.getMessageStrings())).collect(Collectors.joining(";"))));
            }
            return new ValidationResult(type, s.getHref(), violations);
        }).toList();
    }

    private XVRLDetection.Severity mapSeverity(XmlError.Severity severity) {
        if (severity == null)
            return XVRLDetection.Severity.INFO;
        return switch (severity) {
            case SEVERITY_WARNING -> XVRLDetection.Severity.WARNING;
            case SEVERITY_ERROR -> XVRLDetection.Severity.ERROR;
            case SEVERITY_FATAL_ERROR -> XVRLDetection.Severity.FATAL_ERROR;
        };
    }

    /**
     * Fügt eine Schema-Referenz zu den Metadaten hinzu.
     *
     * @param href die URL oder der Pfad zum Schema
     * @param language die Sprache des Schemas (z.B. "XSD" oder "Schematron")
     */
    public void addSchemaReference(String href, String language) {
        if (original.getMetadata() == null) {
            original.setMetadata(new ObjectFactory().createXVRLMetadata());
        }
        Schema s = new ObjectFactory().createSchema();
        s.setHref(href);
        s.getOtherAttributes().put(new QName(CVRL_NS, ATTR_LANGUAGE, CVRL_PREFIX), language);
        original.getMetadata().getSchemas().add(s);
    }

    public List<Schema> getSchemaReferences(String language) {
        return original.getMetadata().getSchemas().stream()
                .filter(s -> language.equals(s.getOtherAttributes().get(new QName(CVRL_NS, ATTR_LANGUAGE)))).collect(Collectors.toList());
    }

    public void addSchemaValidationResult(List<XmlError> violations) {
        addSchemaReference(CODE_XSD_VALIDATION, CODE_XSD_VALIDATION);
        Optional.ofNullable(violations).ifPresent(v -> v.forEach(this::addSchemaViolation));
    }

    public void addSchematronValidationResults(List<SchematronOutput> schematronOutputs) {
        Optional.ofNullable(schematronOutputs).ifPresent(so -> so.forEach(this::addSchematronValidationResult));
    }

    private void addSchematronValidationResult(SchematronOutput schematronOutput) {
        String title = schematronOutput.getTitle() != null ? schematronOutput.getTitle() : "Schematron";
        addSchemaReference(title, CODE_SCHEMATRON_VALIDATION);
        schematronOutput.getFailedAsserts().forEach(fa -> addSchematronViolation(fa, title));
    }

    /**
     * Prüft, ob der Report Schema-Verletzungen enthält.
     *
     * @return true, wenn keine Schema-Verletzungen vorliegen
     */
    public boolean isSchemaValid() {
        return original.getDetection().stream().noneMatch(d -> CODE_XSD_VIOLATION.equals(d.getCode()));
    }

    /**
     * Prüft, ob der Report Schematron-Verletzungen enthält.
     *
     * @return true, wenn keine Schematron-Verletzungen vorliegen
     */
    public boolean isSchematronValid() {
        return original.getDetection().stream().noneMatch(d -> CODE_SCHEMATRON_VIOLATION.equals(d.getCode()));
    }

    public XVRLReport getOriginal() {
        return original;
    }
}
