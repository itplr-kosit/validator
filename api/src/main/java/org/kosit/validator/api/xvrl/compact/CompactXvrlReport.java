package org.kosit.validator.api.xvrl.compact;

import static org.kosit.validator.api.xvrl.compact.CompactXvrlReportSummary.CVRL_NS;
import static org.kosit.validator.api.xvrl.compact.CompactXvrlReportSummary.CVRL_PREFIX;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.base.error.SimpleError;
import org.kosit.xvrl.api.XvrlHelper;
import org.kosit.xvrl.model.XvrlCreator;
import org.kosit.xvrl.model.XvrlDetection;
import org.kosit.xvrl.model.XvrlDocument;
import org.kosit.xvrl.model.XvrlLocation;
import org.kosit.xvrl.model.XvrlMessage;
import org.kosit.xvrl.model.XvrlMetadata;
import org.kosit.xvrl.model.XvrlProvenance;
import org.kosit.xvrl.model.XvrlReport;
import org.kosit.xvrl.model.XvrlSchema;
import org.kosit.xvrl.model.XvrlSeverity;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compact Xvrl report with convenience access to additive attributes.
 * <p>
 * The class is a mutable facade over the immutable {@link XvrlReport} data model: it collects the modifications in an
 * {@link XvrlReport.Builder} and materializes them on every call to {@link #getOriginal()}.
 */
public class CompactXvrlReport {

    // TODO change severity type
    public record Violation(String severity, Long line, Long row, String id, String message) {
    }

    public record ValidationResult(String type, String name, List<Violation> violations) {

        @Override
        @ReturnsImmutableObject
        public List<Violation> violations() {
            return Optional.ofNullable(violations).orElse(List.of());
        }
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

    private final XvrlReport.Builder report;

    private XvrlMetadata.@Nullable Builder metadata;

    /**
     * Creates a new instance with an empty underlying XvrlReport.
     *
     * @return new instance of {@link CompactXvrlReport}
     */
    public static CompactXvrlReport create() {
        return new CompactXvrlReport(XvrlReport.builder().build());
    }

    public CompactXvrlReport(final XvrlReport original) {
        this.report = original.toBuilder();
        this.metadata = original.getMetadata() == null ? null : original.getMetadata().toBuilder();
    }

    private XvrlMetadata.Builder metadata() {
        if (this.metadata == null) {
            this.metadata = XvrlMetadata.builder();
        }
        return this.metadata;
    }

    private @Nullable XvrlMetadata buildMetadata() {
        return this.metadata == null ? null : this.metadata.build();
    }

    /**
     * Sets the file name/path in the report.
     *
     * @param href the file name or path
     */
    public void setFilename(final String href) {
        metadata().document(XvrlDocument.builder(href));
    }

    public String getFilename() {
        return Optional.ofNullable(buildMetadata()).map(XvrlMetadata::getFirstDocument).map(XvrlDocument::getHref).orElse(null);
    }

    /**
     * Sets the creator of the report.
     *
     * @param name name of the creator
     */
    public void setCreator(final String name) {
        metadata().creator(XvrlCreator.builder(name));
    }

    /**
     * Sets the selected scenario.
     *
     * @param scenario name of the scenario
     */
    public void setScenario(final String scenario) {
        this.report.removeDetectionsIf(det -> ID_SCENARIO.equals(det.getID()));
        this.report.addDetection(XvrlDetection.builder().id(ID_SCENARIO).code(scenario));
    }

    /**
     * Returns the selected scenario. According to the target XML this is represented via a detection with the xml:id
     * 'scenario' and the attribute 'code'.
     *
     * @return name of the scenario or null
     */
    public String getScenario() {
        return this.report.getDetections().stream().filter(d -> ID_SCENARIO.equals(d.getID())).map(XvrlDetection::getCode).findFirst()
                .orElse(null);
    }

    /**
     * Sets the acceptance status in the CVRL namespace.
     *
     * @param recommendation acceptance recommendation
     */
    public void setAcceptance(final AcceptRecommendation recommendation) {
        this.report.otherAttribute(new QName(CVRL_NS, ATTR_ACCEPTANCE, CVRL_PREFIX), recommendation.getID());
    }

    /**
     * Returns the acceptance status from the CVRL namespace. It is located directly on the report element.
     *
     * @return acceptance recommendation or null
     */
    public AcceptRecommendation getAcceptance() {
        final String val = this.report.getOtherAttribute(new QName(CVRL_NS, ATTR_ACCEPTANCE));
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
        this.report.otherAttribute(new QName(CVRL_NS, ATTR_ERROR_SUMMARY, CVRL_PREFIX), summary);
    }

    /**
     * Returns the error summary from the CVRL namespace.
     *
     * @return error summary or null
     */
    public String getErrorSummary() {
        return this.report.getOtherAttribute(new QName(CVRL_NS, ATTR_ERROR_SUMMARY));
    }

    /**
     * Sets the checksum in the CVRL namespace on the document element.
     *
     * @param checksum checksum
     */
    public void setChecksum(final String checksum) {
        final QName attr = new QName(CVRL_NS, ATTR_CHECKSUM, CVRL_PREFIX);
        final XvrlMetadata md = buildMetadata();
        final List<XvrlDocument> documents = md == null ? List.of() : md.getDocuments();
        if (documents.isEmpty()) {
            // Ensure that a document exists
            metadata().document(XvrlDocument.builder(VAL_UNKNOWN).otherAttribute(attr, checksum));
        } else {
            final List<XvrlDocument> newDocuments = new ArrayList<>(documents);
            newDocuments.set(0, documents.getFirst().toBuilder().otherAttribute(attr, checksum).build());
            metadata().removeAllDocuments().addDocuments(newDocuments);
        }
    }

    /**
     * Returns the checksum from the CVRL namespace. In the target XML this is located on the document element within
     * the metadata.
     *
     * @return checksum or null
     */
    public String getChecksum() {
        final XvrlMetadata md = buildMetadata();
        if (md == null) {
            return null;
        }
        return md.getDocuments().stream().map(d -> d.getOtherAttribute(new QName(CVRL_NS, ATTR_CHECKSUM))).filter(c -> c != null)
                .findFirst().orElse(null);
    }

    /**
     * Adds a schema violation.
     *
     * @param error the schema error object
     */
    public void addSchemaViolation(final SimpleError error) {
        final XvrlDetection.Builder d = XvrlDetection.builder().code(CODE_XSD_VIOLATION)
                .severity(XvrlHelper.translate(error.getSeverity()));

        // Message
        d.addMessage(XvrlMessage.builder(error.getMessage()));

        // Location/Provenance
        if (error.hasLineOrColumnNumber()) {
            d.addProvenance(XvrlProvenance.builder().addLocation(XvrlHelper.createLocation(error)));
        }

        this.report.addDetection(d);
    }

    public XvrlDetection getSchemaViolation() {
        return this.report.getDetections().stream().filter(d -> CODE_XSD_VIOLATION.equals(d.getCode())).findFirst().orElse(null);
    }

    /**
     * Adds a Schematron violation.
     *
     * @param failedAssert the Schematron failure message
     * @param schemaHref the reference to the schema used (e.g. href or title)
     */
    public void addSchematronViolation(final FailedAssert failedAssert, final String schemaHref) {
        final XvrlDetection.Builder d = XvrlDetection.builder().code(CODE_SCHEMATRON_VIOLATION);
        // In the target XML severity is often info, but we take the role value if present
        if (failedAssert.getRole() != null) {
            d.severity(XvrlSeverity.getFromIDOrDefault(failedAssert.getRole().toLowerCase(Locale.ROOT), XvrlSeverity.INFO));
        } else {
            d.severity(XvrlSeverity.INFO);
        }

        // Message
        final XvrlMessage.Builder msg = XvrlMessage.builder();
        if (failedAssert.getText() != null) {
            for (final Object item : failedAssert.getText().getContent()) {
                msg.addContent(item == null ? null : item.toString());
            }
        }
        d.addMessage(msg);

        // Schema reference via provenance/location
        if (schemaHref != null) {
            d.addProvenance(XvrlProvenance.builder().addLocation(XvrlLocation.builder().href(schemaHref)));
        }

        this.report.addDetection(d);
    }

    @ReturnsImmutableObject
    public List<XvrlDetection> getSchematronViolations() {
        return this.report.getDetections().stream().filter(d -> CODE_SCHEMATRON_VIOLATION.equals(d.getCode())).toList();
    }

    public ValidationResult getSchemaValidationResult() {
        final XvrlSchema validationRef = getSchemaReferences(CODE_XSD_VALIDATION).stream().findFirst().orElse(null);
        final String type = validationRef.getOtherAttribute(new QName(CVRL_NS, ATTR_LANGUAGE));
        final XvrlDetection det = getSchemaViolation();
        final List<Violation> violations = new ArrayList<>();
        if (det != null) {
            final Long line = det.getProvenances().stream().flatMap(p -> p.getLocations().stream()).findFirst().map(XvrlLocation::getLine)
                    .orElse(null);
            final Long col = det.getProvenances().stream().flatMap(p -> p.getLocations().stream()).findFirst().map(XvrlLocation::getColumn)
                    .orElse(null);
            violations.add(new Violation(det.getSeverity().getID(), line, col, null,
                    det.getMessages().stream().map(m -> String.join(";", m.getContentStrings())).collect(Collectors.joining(";"))));
        }
        return new ValidationResult(type, validationRef.getHref(), violations);
    }

    public List<ValidationResult> getSchematronValidationResult() {
        final List<XvrlSchema> validationRef = getSchemaReferences(CODE_SCHEMATRON_VALIDATION);
        final List<XvrlDetection> dets = getSchematronViolations();
        return validationRef.stream().map(s -> {
            final String type = s.getOtherAttribute(new QName(CVRL_NS, ATTR_LANGUAGE));
            final XvrlDetection det = dets.stream()
                    .filter(d -> d.getProvenances().stream()
                            .anyMatch(p -> p.getLocations().stream().anyMatch(l -> l.getHref().equals(s.getHref()))))
                    .findFirst().orElse(null);
            final List<Violation> violations = new ArrayList<>();
            if (det != null) {
                violations.add(new Violation(det.getSeverity().getID(), null, null, det.getID(),
                        det.getMessages().stream().map(m -> String.join(";", m.getContentStrings())).collect(Collectors.joining(";"))));
            }
            return new ValidationResult(type, s.getHref(), violations);
        }).toList();
    }

    /**
     * Adds a schema reference to the metadata.
     *
     * @param href the URL or path to the schema
     * @param language the language of the schema (e.g. "XSD" or "Schematron")
     */
    public void addSchemaReference(final String href, final String language) {
        metadata().addSchema(XvrlSchema.builder().href(href).otherAttribute(new QName(CVRL_NS, ATTR_LANGUAGE, CVRL_PREFIX), language));
    }

    @ReturnsImmutableObject
    public List<XvrlSchema> getSchemaReferences(final String language) {
        final XvrlMetadata md = buildMetadata();
        if (md == null) {
            return List.of();
        }
        return md.getSchemas().stream().filter(s -> language.equals(s.getOtherAttribute(new QName(CVRL_NS, ATTR_LANGUAGE)))).toList();
    }

    public void addSchemaValidationResult(final List<SimpleError> violations) {
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
        return this.report.getDetections().stream().noneMatch(d -> CODE_XSD_VIOLATION.equals(d.getCode()));
    }

    /**
     * Checks whether the report contains Schematron violations.
     *
     * @return true if no Schematron violations are present
     */
    public boolean isSchematronValid() {
        return this.report.getDetections().stream().noneMatch(d -> CODE_SCHEMATRON_VIOLATION.equals(d.getCode()));
    }

    /**
     * @return the underlying data model object, materialized from the current state of this facade. Never
     *         <code>null</code>.
     */
    public XvrlReport getOriginal() {
        return this.report.metadata(buildMetadata()).build();
    }
}
