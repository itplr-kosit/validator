package org.kosit.validator.api.xvrl.compact;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.xvrl.model.XvrlMetadataType;
import org.kosit.xvrl.model.XvrlReportsType;
import org.kosit.xvrl.model.XvrlValidatorType;

/**
 * Compact summary of the validation results using the existing Xvrl. Provides convenience access to additive attributes
 * via the CVRL namespace.
 */
public class CompactXvrlReportSummary {

    public static final String CVRL_NS = "http://www.xoev.de/de/validator/framework/2/compact-format";

    public static final String CVRL_PREFIX = "compactvrl";

    private static final String ATTR_ACCEPTABLE = "acceptable";

    private static final String ATTR_REJECTED = "rejected";

    private static final String ATTR_PROCESSING_ERRORS = "processing-errors";

    private final XvrlReportsType original;

    /**
     * Creates a new instance with an empty underlying XvrlReportSummary.
     *
     * @return new instance of {@link CompactXvrlReportSummary}
     */
    public static CompactXvrlReportSummary create() {
        return new CompactXvrlReportSummary(new XvrlReportsType());
    }

    public CompactXvrlReportSummary(final XvrlReportsType original) {
        this.original = original;
    }

    public XvrlReportsType getOriginal() {
        return original;
    }

    /**
     * Returns the list of compact reports.
     *
     * @return list of {@link CompactXvrlReport}
     */
    @ReturnsImmutableObject
    public List<CompactXvrlReport> getReports() {
        return original.getReports().stream().map(CompactXvrlReport::new).toList();
    }

    /**
     * Adds a compact report.
     *
     * @param report the compact report
     */
    public void addReport(final CompactXvrlReport report) {
        original.getReportOrReportsOrDigest().add(report.getOriginal());
    }

    /**
     * Sets the value of the 'acceptable' attribute in the CVRL namespace.
     *
     * @param count number of acceptable results
     */
    public void setAcceptable(final long count) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_ACCEPTABLE, CVRL_PREFIX), Long.toString(count));
    }

    /**
     * Returns the value of the 'acceptable' attribute from the CVRL namespace.
     *
     * @return number of acceptable results or null
     */
    public Long getAcceptable() {
        final String val = original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_ACCEPTABLE));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Sets the value of the 'rejected' attribute in the CVRL namespace.
     *
     * @param count number of rejected results
     */
    public void setRejected(final long count) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_REJECTED, CVRL_PREFIX), Long.toString(count));
    }

    /**
     * Returns the value of the 'rejected' attribute from the CVRL namespace.
     *
     * @return number of rejected results or null
     */
    public Long getRejected() {
        final String val = original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_REJECTED));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Sets the value of the 'processing-errors' attribute in the CVRL namespace.
     *
     * @param count number of processing errors
     */
    public void setProcessingErrors(final long count) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_PROCESSING_ERRORS, CVRL_PREFIX), Long.toString(count));
    }

    /**
     * Returns the value of the 'processing-errors' attribute from the CVRL namespace.
     *
     * @return number of processing errors or null
     */
    public Long getProcessingErrors() {
        final String val = original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_PROCESSING_ERRORS));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Sets the information about the validator used.
     *
     * @param info {@link ValidatorEngineInformation}
     */
    public void setValidatorInformation(final ValidatorEngineInformation info) {
        if (original.getMetadata() == null) {
            original.setMetadata(new XvrlMetadataType());
        }

        final XvrlValidatorType v = new XvrlValidatorType();
        v.setName(info.name());
        v.setVersion(info.version());
        original.getMetadata().getValidators().clear();
        original.getMetadata().getValidators().add(v);
    }

    /**
     * Returns the information about the validator used.
     *
     * @return {@link ValidatorEngineInformation} or null
     */
    @Nullable
    public ValidatorEngineInformation getValidatorInformation() {
        return Optional.ofNullable(original.getMetadata()).map(XvrlMetadataType::getValidators).stream().flatMap(Collection::stream)
                .findFirst().map(v -> new ValidatorEngineInformation(v.getName(), v.getVersion())).orElse(null);
    }
}
