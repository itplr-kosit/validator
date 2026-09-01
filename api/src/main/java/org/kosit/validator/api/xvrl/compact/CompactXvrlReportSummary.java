package org.kosit.validator.api.xvrl.compact;

import java.util.List;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.xvrl.model.XvrlMetadata;
import org.kosit.xvrl.model.XvrlReport;
import org.kosit.xvrl.model.XvrlReports;
import org.kosit.xvrl.model.XvrlValidator;

/**
 * Compact summary of the validation results using the existing Xvrl. Provides convenience access to additive attributes
 * via the CVRL namespace.
 * <p>
 * The class is a mutable facade over the immutable {@link XvrlReports} data model: it collects the modifications in an
 * {@link XvrlReports.Builder} and materializes them on every call to {@link #getOriginal()}.
 */
public class CompactXvrlReportSummary {

    public static final String CVRL_NS = "http://www.xoev.de/de/validator/framework/2/compact-format";

    public static final String CVRL_PREFIX = "compactvrl";

    private static final String ATTR_ACCEPTABLE = "acceptable";

    private static final String ATTR_REJECTED = "rejected";

    private static final String ATTR_PROCESSING_ERRORS = "processing-errors";

    private final XvrlReports.Builder reports;

    private XvrlMetadata.@Nullable Builder metadata;

    /**
     * Creates a new instance with an empty underlying XvrlReportSummary.
     *
     * @return new instance of {@link CompactXvrlReportSummary}
     */
    public static CompactXvrlReportSummary create() {
        return new CompactXvrlReportSummary(XvrlReports.builder().build());
    }

    public CompactXvrlReportSummary(final XvrlReports original) {
        this.reports = original.toBuilder();
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
     * @return the underlying data model object, materialized from the current state of this facade. Never
     *         <code>null</code>.
     */
    public XvrlReports getOriginal() {
        return this.reports.metadata(buildMetadata()).build();
    }

    /**
     * Returns the list of compact reports.
     *
     * @return list of {@link CompactXvrlReport}
     */
    @ReturnsImmutableObject
    public List<CompactXvrlReport> getReports() {
        return this.reports.getAllItems().stream().filter(XvrlReport.class::isInstance).map(XvrlReport.class::cast)
                .map(CompactXvrlReport::new).toList();
    }

    /**
     * Adds a compact report.
     *
     * @param report the compact report
     */
    public void addReport(final CompactXvrlReport report) {
        this.reports.addReport(report.getOriginal());
    }

    /**
     * Sets the value of the 'acceptable' attribute in the CVRL namespace.
     *
     * @param count number of acceptable results
     */
    public void setAcceptable(final long count) {
        this.reports.otherAttribute(new QName(CVRL_NS, ATTR_ACCEPTABLE, CVRL_PREFIX), Long.toString(count));
    }

    /**
     * Returns the value of the 'acceptable' attribute from the CVRL namespace.
     *
     * @return number of acceptable results or null
     */
    public Long getAcceptable() {
        final String val = this.reports.getOtherAttribute(new QName(CVRL_NS, ATTR_ACCEPTABLE));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Sets the value of the 'rejected' attribute in the CVRL namespace.
     *
     * @param count number of rejected results
     */
    public void setRejected(final long count) {
        this.reports.otherAttribute(new QName(CVRL_NS, ATTR_REJECTED, CVRL_PREFIX), Long.toString(count));
    }

    /**
     * Returns the value of the 'rejected' attribute from the CVRL namespace.
     *
     * @return number of rejected results or null
     */
    public Long getRejected() {
        final String val = this.reports.getOtherAttribute(new QName(CVRL_NS, ATTR_REJECTED));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Sets the value of the 'processing-errors' attribute in the CVRL namespace.
     *
     * @param count number of processing errors
     */
    public void setProcessingErrors(final long count) {
        this.reports.otherAttribute(new QName(CVRL_NS, ATTR_PROCESSING_ERRORS, CVRL_PREFIX), Long.toString(count));
    }

    /**
     * Returns the value of the 'processing-errors' attribute from the CVRL namespace.
     *
     * @return number of processing errors or null
     */
    public Long getProcessingErrors() {
        final String val = this.reports.getOtherAttribute(new QName(CVRL_NS, ATTR_PROCESSING_ERRORS));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Sets the information about the validator used.
     *
     * @param info {@link ValidatorEngineInformation}
     */
    public void setValidatorInformation(final ValidatorEngineInformation info) {
        metadata().validator(XvrlValidator.builder(info.name()).version(info.version()));
    }

    /**
     * Returns the information about the validator used.
     *
     * @return {@link ValidatorEngineInformation} or null
     */
    @Nullable
    public ValidatorEngineInformation getValidatorInformation() {
        return Optional.ofNullable(buildMetadata()).map(XvrlMetadata::getFirstValidator)
                .map(v -> new ValidatorEngineInformation(v.getName(), v.getVersion())).orElse(null);
    }
}
