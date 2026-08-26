package org.kosit.validator.api.compact;

import static org.kosit.validator.api.compact.CompactXVRLReportSummary.CVRL_NS;
import static org.kosit.validator.api.compact.CompactXVRLReportSummary.CVRL_PREFIX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.api.XmlError;
import org.kosit.xvrl.model.Creator;
import org.kosit.xvrl.model.Document;
import org.kosit.xvrl.model.Location;
import org.kosit.xvrl.model.ObjectFactory;
import org.kosit.xvrl.model.Provenance;
import org.kosit.xvrl.model.Schema;
import org.kosit.xvrl.model.XVRLDetection;
import org.kosit.xvrl.model.XVRLMessage;
import org.kosit.xvrl.model.XVRLMetadata;
import org.kosit.xvrl.model.XVRLReport;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compact XVRL report with convenience access to additive attributes.
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CompactXVRLReport.class);

    private final XVRLReport original;

    public CompactXVRLReport(final XVRLReport original) {
        this.original = original;
    }

    /**
     * Creates a new instance with an empty underlying XVRLReport.
     *
     * @return new instance of {@link CompactXVRLReport}
     */
    public static CompactXVRLReport create() {
        return new CompactXVRLReport(new ObjectFactory().createXVRLReport());
    }

    /**
     * Sets the file name/path in the report.
     *
     * @param href the file name or path
     */
    public void setFilename(final String href) {
        if (original.getMetadata() == null) {
            original.setMetadata(new ObjectFactory().createXVRLMetadata());
        }
        final Document doc = new ObjectFactory().createDocument();
        doc.setHref(href);
        original.getMetadata().getDocuments().clear();
        original.getMetadata().getDocuments().add(doc);
    }

    public String getFilename() {
        return Optional.ofNullable(original.getMetadata()).map(XVRLMetadata::getDocuments).stream().flatMap(Collection::stream)
                .map(Document::getHref).findFirst().orElse(null);
    }

    /**
     * Sets the creator of the report.
     *
     * @param name name of the creator
     */
    public void setCreator(final String name) {
        if (original.getMetadata() == null) {
            original.setMetadata(new ObjectFactory().createXVRLMetadata());
        }
        final Creator creator = new ObjectFactory().createCreator();
        creator.setName(name);
        original.getMetadata().getCreators().clear();
        original.getMetadata().getCreators().add(creator);
    }

    /**
     * Sets the selected scenario.
     *
     * @param scenario name of the scenario
     */
    public void setScenario(final String scenario) {
        final XVRLDetection d = new ObjectFactory().createXVRLDetection();
        d.setId(ID_SCENARIO);
        d.setCode(scenario);
        original.getDetection().removeIf(det -> ID_SCENARIO.equals(det.getId()));
        original.getDetection().add(d);
    }

    /**
     * Returns the selected scenario. According to the target XML this is represented via a detection with the xml:id
     * 'scenario' and the attribute 'code'.
     *
     * @return name of the scenario or null
     */
    public String getScenario() {
        return original.getDetection().stream().filter(d -> ID_SCENARIO.equals(d.getId())).map(XVRLDetection::getCode).findFirst()
                .orElse(null);
    }

    /**
     * Sets the acceptance status in the CVRL namespace.
     *
     * @param recommendation acceptance recommendation
     */
    public void setAcceptance(final AcceptRecommendation recommendation) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_ACCEPTANCE, CVRL_PREFIX), recommendation.name());
    }

    /**
     * Returns the acceptance status from the CVRL namespace. It is located directly on the report element.
     *
     * @return acceptance recommendation or null
     */
    public AcceptRecommendation getAcceptance() {
        final String val = original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_ACCEPTANCE));
        if (val == null) {
            return AcceptRecommendation.UNDEFINED;
        }
        final String ucVal = val.toUpperCase(Locale.ROOT);
        try {
            return AcceptRecommendation.valueOf(ucVal);
        } catch (final IllegalArgumentException e) {
            LOGGER.error("Failed to convert '" + ucVal + "' to an acceptance result");
            return AcceptRecommendation.UNDEFINED;
        }
    }

    /**
     * Sets the error summary in the CVRL namespace.
     *
     * @param summary error summary
     */
    public void setErrorSummary(final String summary) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_ERROR_SUMMARY, CVRL_PREFIX), summary);
    }

    /**
     * Returns the error summary from the CVRL namespace.
     *
     * @return error summary or null
     */
    public String getErrorSummary() {
        return original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_ERROR_SUMMARY));
    }

    /**
     * Sets the checksum in the CVRL namespace on the document element.
     *
     * @param checksum checksum
     */
    public void setChecksum(final String checksum) {
        if (original.getMetadata() == null || original.getMetadata().getDocuments().isEmpty()) {
            setFilename(VAL_UNKNOWN); // Ensure that a document exists
        }
        original.getMetadata().getDocuments().get(0).getOtherAttributes().put(new QName(CVRL_NS, ATTR_CHECKSUM, CVRL_PREFIX), checksum);
    }

    /**
     * Returns the checksum from the CVRL namespace. In the target XML this is located on the document element within
     * the metadata.
     *
     * @return checksum or null
     */
    public String getChecksum() {
        return Optional.ofNullable(original.getMetadata()).map(XVRLMetadata::getDocuments).stream().flatMap(Collection::stream)
                .map(d -> d.getOtherAttributes().get(new QName(CVRL_NS, ATTR_CHECKSUM))).filter(c -> c != null).findFirst().orElse(null);
    }

    /**
     * Adds a schema violation.
     *
     * @param error the schema error object
     */
    public void addSchemaViolation(final XmlError error) {
        final ObjectFactory of = new ObjectFactory();
        final XVRLDetection d = of.createXVRLDetection();
        d.setCode(CODE_XSD_VIOLATION);
        d.setSeverity(mapSeverity(error.getSeverity()));

        // Message
        final XVRLMessage msg = of.createXVRLMessage();
        msg.getContent().add(error.getMessage());
        d.getMessages().add(msg);

        // Location/Provenance
        if (error.getRowNumber() != null || error.getColumnNumber() != null) {
            final Location loc = of.createLocation();
            if (error.getRowNumber() != null) {
                loc.setLine(error.getRowNumber().longValue());
            }
            if (error.getColumnNumber() != null) {
                loc.setColumn(error.getColumnNumber().longValue());
            }
            final Provenance prov = of.createProvenance();
            prov.getLocation().add(loc);
            d.getProvenances().add(prov);
        }

        original.getDetection().add(d);
    }

    public XVRLDetection getSchemaViolation() {
        return original.getDetection().stream().filter(d -> CODE_XSD_VIOLATION.equals(d.getCode())).findFirst().orElse(null);
    }

    /**
     * Adds a Schematron violation.
     *
     * @param failedAssert the Schematron failure message
     * @param schemaHref the reference to the schema used (e.g. href or title)
     */
    public void addSchematronViolation(final FailedAssert failedAssert, final String schemaHref) {
        final ObjectFactory of = new ObjectFactory();
        final XVRLDetection d = of.createXVRLDetection();
        d.setCode(CODE_SCHEMATRON_VIOLATION);
        // In the target XML severity is often info, but we take the role value if present
        if (failedAssert.getRole() != null) {
            try {
                d.setSeverity(XVRLDetection.Severity.fromValue(failedAssert.getRole().toLowerCase()));
            } catch (final IllegalArgumentException e) {
                d.setSeverity(XVRLDetection.Severity.INFO);
            }
        } else {
            d.setSeverity(XVRLDetection.Severity.INFO);
        }

        // Message
        final XVRLMessage msg = of.createXVRLMessage();
        if (failedAssert.getText() != null) {
            msg.getContent().addAll(failedAssert.getText().getContent());
        }
        d.getMessages().add(msg);

        // Schema reference via provenance/location
        if (schemaHref != null) {
            final Location loc = of.createLocation();
            loc.setHref(schemaHref);
            final Provenance prov = of.createProvenance();
            prov.getLocation().add(loc);
            d.getProvenances().add(prov);
        }

        original.getDetection().add(d);
    }

    public List<XVRLDetection> getSchematronViolations() {
        return original.getDetection().stream().filter(d -> CODE_SCHEMATRON_VIOLATION.equals(d.getCode())).toList();
    }

    public ValidationResult getSchemaValidationResult() {
        final Schema validationRef = getSchemaReferences(CODE_XSD_VALIDATION).stream().findFirst().orElse(null);
        final String type = validationRef.getOtherAttributes().get(new QName(CVRL_NS, ATTR_LANGUAGE));
        final XVRLDetection det = getSchemaViolation();
        final List<Violation> violations = new ArrayList<>();
        if (det != null) {
            final long line = det.getProvenances().stream().flatMap(p -> p.getLocation().stream()).findFirst()
                    .map(l -> l.getLine().intValue()).orElse(0);
            final long col = det.getProvenances().stream().flatMap(p -> p.getLocation().stream()).findFirst()
                    .map(l -> l.getColumn().intValue()).orElse(0);
            violations.add(new Violation(det.getSeverity().value(), line, col, null,
                    det.getMessages().stream().map(m -> String.join(";", m.getMessageStrings())).collect(Collectors.joining(";"))));
        }
        return new ValidationResult(type, validationRef.getHref(), violations);
    }

    public List<ValidationResult> getSchematronValidationResult() {
        final List<Schema> validationRef = getSchemaReferences(CODE_SCHEMATRON_VALIDATION);
        final List<XVRLDetection> dets = getSchematronViolations();
        return validationRef.stream().map(s -> {
            final String type = s.getOtherAttributes().get(new QName(CVRL_NS, ATTR_LANGUAGE));
            final XVRLDetection det = dets.stream().filter(
                    d -> d.getProvenances().stream().anyMatch(p -> p.getLocation().stream().anyMatch(l -> l.getHref().equals(s.getHref()))))
                    .findFirst().orElse(null);
            final List<Violation> violations = new ArrayList<>();
            if (det != null) {
                violations.add(new Violation(det.getSeverity().value(), null, null, det.getId(),
                        det.getMessages().stream().map(m -> String.join(";", m.getMessageStrings())).collect(Collectors.joining(";"))));
            }
            return new ValidationResult(type, s.getHref(), violations);
        }).toList();
    }

    private XVRLDetection.Severity mapSeverity(final XmlError.Severity severity) {
        if (severity == null)
            return XVRLDetection.Severity.INFO;
        return switch (severity) {
            case SEVERITY_WARNING -> XVRLDetection.Severity.WARNING;
            case SEVERITY_ERROR -> XVRLDetection.Severity.ERROR;
            case SEVERITY_FATAL_ERROR -> XVRLDetection.Severity.FATAL_ERROR;
        };
    }

    /**
     * Adds a schema reference to the metadata.
     *
     * @param href the URL or path to the schema
     * @param language the language of the schema (e.g. "XSD" or "Schematron")
     */
    public void addSchemaReference(final String href, final String language) {
        if (original.getMetadata() == null) {
            original.setMetadata(new ObjectFactory().createXVRLMetadata());
        }
        final Schema s = new ObjectFactory().createSchema();
        s.setHref(href);
        s.getOtherAttributes().put(new QName(CVRL_NS, ATTR_LANGUAGE, CVRL_PREFIX), language);
        original.getMetadata().getSchemas().add(s);
    }

    public List<Schema> getSchemaReferences(final String language) {
        return original.getMetadata().getSchemas().stream()
                .filter(s -> language.equals(s.getOtherAttributes().get(new QName(CVRL_NS, ATTR_LANGUAGE)))).toList();
    }

    public void addSchemaValidationResult(final List<XmlError> violations) {
        addSchemaReference(CODE_XSD_VALIDATION, CODE_XSD_VALIDATION);
        Optional.ofNullable(violations).ifPresent(v -> v.forEach(this::addSchemaViolation));
    }

    public void addSchematronValidationResults(final List<SchematronOutputType> schematronOutputs) {
        Optional.ofNullable(schematronOutputs).ifPresent(so -> so.forEach(this::addSchematronValidationResult));
    }

    private void addSchematronValidationResult(final SchematronOutputType schematronOutput) {
        final String title = schematronOutput.getTitle() != null ? schematronOutput.getTitle() : "Schematron";
        addSchemaReference(title, CODE_SCHEMATRON_VALIDATION);
        schematronOutput.getFailedAsserts().forEach(fa -> addSchematronViolation(fa, title));
    }

    /**
     * Checks whether the report contains schema violations.
     *
     * @return true if no schema violations are present
     */
    public boolean isSchemaValid() {
        return original.getDetection().stream().noneMatch(d -> CODE_XSD_VIOLATION.equals(d.getCode()));
    }

    /**
     * Checks whether the report contains Schematron violations.
     *
     * @return true if no Schematron violations are present
     */
    public boolean isSchematronValid() {
        return original.getDetection().stream().noneMatch(d -> CODE_SCHEMATRON_VIOLATION.equals(d.getCode()));
    }

    public XVRLReport getOriginal() {
        return original;
    }
}
