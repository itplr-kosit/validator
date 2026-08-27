package org.kosit.validator.impl;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.api.VResult;
import org.kosit.validator.api.xvrl.compact.AcceptRecommendation;
import org.kosit.validator.xvrl.XvrlSerializer;
import org.kosit.xvrl.model.XvrlReportsType;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import jakarta.xml.bind.JAXBException;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

@Deprecated(since = "2.0.0", forRemoval = true)
public class DefaultResult implements VResult {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultResult.class);

    /** The internal report 'preliminary stage' produced by the validator */
    private XvrlReportsType reportSummary;

    /** The evaluated result. */
    private final AcceptRecommendation acceptRecommendation;

    private List<SimpleError> schemaViolations;

    private List<SchematronOutputType> schematronResult;

    private boolean processingSuccessful;

    private boolean wellformed;

    @Deprecated(since = "2.0.0", forRemoval = true)
    public DefaultResult(final AcceptRecommendation recommendation) {
        this.acceptRecommendation = recommendation;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    @Nullable
    public XdmNode getReport() {
        try {
            return new XvrlSerializer(null).marshalToXdmNode(getReportSummary());
        } catch (JAXBException | SaxonApiException e) {
            LOGGER.error("Error serializing Xvrl Report", e);
            return null;
        }
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public List<String> getProcessingErrors() {
        return reportSummary.getAllErrors();
    }

    /**
     * Returns the report as a W3C {@link Document}.
     *
     * @return the report
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public Document getReportDocument() {
        final var report = getReport();
        return report == null ? null : (Document) NodeOverNodeInfo.wrap(report.getUnderlyingNode());
    }

    /**
     * Quick access to the recommendation for further processing of the document.
     *
     * @return true if {@link AcceptRecommendation#ACCEPTABLE}
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public boolean isAcceptable() {
        return isProcessingSuccessful() && AcceptRecommendation.ACCEPTABLE.equals(this.acceptRecommendation);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public boolean isSchemaValid() {
        return getSchemaViolations() != null && getSchemaViolations().isEmpty();
    }

    /**
     * Returns all Schematron results of type {@link FailedAssert}.
     *
     * @return the {@link FailedAssert}
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public List<FailedAssert> getFailedAsserts() {
        return getSchematronResult() != null ? getSchematronResult().stream().flatMap(e -> e.getFailedAsserts().stream()).toList()
                : Collections.emptyList();
    }

    private boolean isSchematronEvaluated() {
        return getSchematronResult() != null
                && getSchematronResult().stream().noneMatch(e -> e.getActivePatternOrActiveGroupAndFiredRule().isEmpty());
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public boolean isSchematronValid() {
        return isSchematronEvaluated() && getFailedAsserts().isEmpty();
    }

    /**
     * The reporting source
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public XvrlReportsType getReportSummary() {
        return this.reportSummary;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * @param reportSummary Report summary
     */
    void setReportSummary(final XvrlReportsType reportSummary) {
        this.reportSummary = reportSummary;
    }

    /**
     * The evaluated accept recommendation
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    public AcceptRecommendation getAcceptRecommendation() {
        return this.acceptRecommendation;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    void setSchemaViolations(final List<SimpleError> schemaViolations) {
        this.schemaViolations = schemaViolations;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public List<SimpleError> getSchemaViolations() {
        return this.schemaViolations;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public List<SchematronOutputType> getSchematronResult() {
        return this.schematronResult;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    void setSchematronResult(final List<SchematronOutputType> schematronResult) {
        this.schematronResult = schematronResult;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public boolean isProcessingSuccessful() {
        return this.processingSuccessful;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setProcessingSuccessful(final boolean processingSuccessful) {
        this.processingSuccessful = processingSuccessful;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public boolean isWellformed() {
        return this.wellformed;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public void setWellformed(final boolean wellformed) {
        this.wellformed = wellformed;
    }
}
