package org.kosit.validator.api.xvrl.compact;

import static org.kosit.validator.api.xvrl.compact.CompactXvrlReportSummary.CVRL_NS;
import static org.kosit.validator.api.xvrl.compact.CompactXvrlReportSummary.CVRL_PREFIX;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.validator.api.xmlerror.XmlError;
import org.kosit.xvrl.model.XvrlCreatorType;
import org.kosit.xvrl.model.XvrlDetectionType;
import org.kosit.xvrl.model.XvrlDocumentType;
import org.kosit.xvrl.model.XvrlLocationType;
import org.kosit.xvrl.model.XvrlMessageType;
import org.kosit.xvrl.model.XvrlMetadataType;
import org.kosit.xvrl.model.XvrlProvenanceType;
import org.kosit.xvrl.model.XvrlReportType;
import org.kosit.xvrl.model.XvrlSchemaType;
import org.kosit.xvrl.model.XvrlSeverityType;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compact Xvrl report with convenience access to additive attributes.
 */
public class CompactXvrlReport {

    public record ValidationResult(String type, String name, List<Violation> violations) {

        @Override
        @ReturnsImmutableObject
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CompactXvrlReport.class);

    private final XvrlReportType original;

    /**
     * Creates a new instance with an empty underlying XvrlReport.
     *
     * @return new instance of {@link CompactXvrlReport}
     */
    public static CompactXvrlReport create() {
        return new CompactXvrlReport(new XvrlReportType());
    }

    public CompactXvrlReport(final XvrlReportType original) {
        this.original = original;
    }

    /**
     * Sets the file name/path in the report.
     *
     * @param href the file name or path
     */
    public void setFilename(final String href) {
        if (original.getMetadata() == null) {
            original.setMetadata(new XvrlMetadataType());
        }
        final XvrlDocumentType doc = new XvrlDocumentType();
        doc.setHref(href);
        original.getMetadata().getDocuments().clear();
        original.getMetadata().getDocuments().add(doc);
    }

    public String getFilename() {
        return Optional.ofNullable(original.getMetadata()).map(XvrlMetadataType::getDocuments).stream().flatMap(Collection::stream)
                .map(XvrlDocumentType::getHref).findFirst().orElse(null);
    }

    /**
     * Sets the creator of the report.
     *
     * @param name name of the creator
     */
    public void setCreator(final String name) {
        if (original.getMetadata() == null) {
            original.setMetadata(new XvrlMetadataType());
        }
        final XvrlCreatorType creator = new XvrlCreatorType();
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
        final XvrlDetectionType d = new XvrlDetectionType();
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
        return original.getDetection().stream().filter(d -> ID_SCENARIO.equals(d.getId())).map(XvrlDetectionType::getCode).findFirst()
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
        return Optional.ofNullable(original.getMetadata()).map(XvrlMetadataType::getDocuments).stream().flatMap(Collection::stream)
                .map(d -> d.getOtherAttributes().get(new QName(CVRL_NS, ATTR_CHECKSUM))).filter(c -> c != null).findFirst().orElse(null);
    }

    /**
     * Adds a schema violation.
     *
     * @param error the schema error object
     */
    public void addSchemaViolation(final XmlError error) {
        final XvrlDetectionType d = new XvrlDetectionType();
        d.setCode(CODE_XSD_VIOLATION);
        d.setSeverity(mapSeverity(error.getSeverity()));

        // Message
        final XvrlMessageType msg = new XvrlMessageType();
        msg.getContent().add(error.getMessage());
        d.getMessages().add(msg);

        // Location/Provenance
        if (error.getRowNumber() != null || error.getColumnNumber() != null) {
            final XvrlLocationType loc = new XvrlLocationType();
            if (error.getRowNumber() != null) {
                loc.setLine(Long.valueOf(error.getRowNumber().longValue()));
            }
            if (error.getColumnNumber() != null) {
                loc.setColumn(Long.valueOf(error.getColumnNumber().longValue()));
            }
            final XvrlProvenanceType prov = new XvrlProvenanceType();
            prov.getLocation().add(loc);
            d.getProvenances().add(prov);
        }

        original.getDetection().add(d);
    }

    public XvrlDetectionType getSchemaViolation() {
        return original.getDetection().stream().filter(d -> CODE_XSD_VIOLATION.equals(d.getCode())).findFirst().orElse(null);
    }

    /**
     * Adds a Schematron violation.
     *
     * @param failedAssert the Schematron failure message
     * @param schemaHref the reference to the schema used (e.g. href or title)
     */
    public void addSchematronViolation(final FailedAssert failedAssert, final String schemaHref) {
        final XvrlDetectionType d = new XvrlDetectionType();
        d.setCode(CODE_SCHEMATRON_VIOLATION);
        // In the target XML severity is often info, but we take the role value if present
        if (failedAssert.getRole() != null) {
            try {
                d.setSeverity(XvrlSeverityType.fromValue(failedAssert.getRole().toLowerCase()));
            } catch (final IllegalArgumentException e) {
                d.setSeverity(XvrlSeverityType.INFO);
            }
        } else {
            d.setSeverity(XvrlSeverityType.INFO);
        }

        // Message
        final XvrlMessageType msg = new XvrlMessageType();
        if (failedAssert.getText() != null) {
            msg.getContent().addAll(failedAssert.getText().getContent());
        }
        d.getMessages().add(msg);

        // Schema reference via provenance/location
        if (schemaHref != null) {
            final XvrlLocationType loc = new XvrlLocationType();
            loc.setHref(schemaHref);
            final XvrlProvenanceType prov = new XvrlProvenanceType();
            prov.getLocation().add(loc);
            d.getProvenances().add(prov);
        }

        original.getDetection().add(d);
    }

    public List<XvrlDetectionType> getSchematronViolations() {
        return original.getDetection().stream().filter(d -> CODE_SCHEMATRON_VIOLATION.equals(d.getCode())).toList();
    }

    public ValidationResult getSchemaValidationResult() {
        final XvrlSchemaType validationRef = getSchemaReferences(CODE_XSD_VALIDATION).stream().findFirst().orElse(null);
        final String type = validationRef.getOtherAttributes().get(new QName(CVRL_NS, ATTR_LANGUAGE));
        final XvrlDetectionType det = getSchemaViolation();
        final List<Violation> violations = new ArrayList<>();
        if (det != null) {
            final Long line = det.getProvenances().stream().flatMap(p -> p.getLocation().stream()).findFirst()
                    .map(XvrlLocationType::getLine).orElse(null);
            final Long col = det.getProvenances().stream().flatMap(p -> p.getLocation().stream()).findFirst()
                    .map(XvrlLocationType::getColumn).orElse(null);
            violations.add(new Violation(det.getSeverity().value(), line, col, null,
                    det.getMessages().stream().map(m -> String.join(";", m.getMessageStrings())).collect(Collectors.joining(";"))));
        }
        return new ValidationResult(type, validationRef.getHref(), violations);
    }

    public List<ValidationResult> getSchematronValidationResult() {
        final List<XvrlSchemaType> validationRef = getSchemaReferences(CODE_SCHEMATRON_VALIDATION);
        final List<XvrlDetectionType> dets = getSchematronViolations();
        return validationRef.stream().map(s -> {
            final String type = s.getOtherAttributes().get(new QName(CVRL_NS, ATTR_LANGUAGE));
            final XvrlDetectionType det = dets.stream().filter(
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

    private XvrlSeverityType mapSeverity(final XmlError.Severity severity) {
        if (severity == null)
            return XvrlSeverityType.INFO;
        return switch (severity) {
            case SEVERITY_WARNING -> XvrlSeverityType.WARNING;
            case SEVERITY_ERROR -> XvrlSeverityType.ERROR;
            case SEVERITY_FATAL_ERROR -> XvrlSeverityType.FATAL_ERROR;
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
            original.setMetadata(new XvrlMetadataType());
        }
        final XvrlSchemaType s = new XvrlSchemaType();
        s.setHref(href);
        s.getOtherAttributes().put(new QName(CVRL_NS, ATTR_LANGUAGE, CVRL_PREFIX), language);
        original.getMetadata().getSchemas().add(s);
    }

    public List<XvrlSchemaType> getSchemaReferences(final String language) {
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

    public XvrlReportType getOriginal() {
        return original;
    }
}
