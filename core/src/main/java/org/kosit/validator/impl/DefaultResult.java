package org.kosit.validator.impl;

import java.util.Collections;
import java.util.List;

import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.api.VResult;
import org.kosit.validator.api.XmlError;
import org.kosit.validator.impl.xml.XMLReaderWrapper;
import org.kosit.xvrl.impl.XvrlConversionService;
import org.kosit.xvrl.model.XVRLReportSummary;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.w3c.dom.Document;

import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.util.JAXBSource;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

@Deprecated(since = "2.0.0", forRemoval = true)
public class DefaultResult implements VResult {

    /** The internal report 'preliminary stage' produced by the validator */
    private XVRLReportSummary reportSummary;

    /** The evaluated result. */
    private final AcceptRecommendation acceptRecommendation;

    private List<XmlError> schemaViolations;

    private List<SchematronOutputType> schematronResult;

    private boolean processingSuccessful;

    private boolean wellformed;

    @Deprecated(since = "2.0.0", forRemoval = true)
    public DefaultResult(final AcceptRecommendation recommendation) {
        this.acceptRecommendation = recommendation;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public XdmNode getReport() {
        final Marshaller marshaller;
        try {
            marshaller = new XvrlConversionService().getJaxbContext().createMarshaller();
            final JAXBSource source = new JAXBSource(marshaller, getReportSummary());
            // wrap to circumvent inconsistency between sax and saxon
            source.setXMLReader(new XMLReaderWrapper(source.getXMLReader()));
            return new Processor(false).newDocumentBuilder().build(source);
        } catch (JAXBException | SaxonApiException e) {
            e.printStackTrace();
        }
        return null;
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
        return (Document) NodeOverNodeInfo.wrap(getReport().getUnderlyingNode());
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
    public XVRLReportSummary getReportSummary() {
        return this.reportSummary;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * @param reportSummary Report summary
     */
    void setReportSummary(final XVRLReportSummary reportSummary) {
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
    void setSchemaViolations(final List<XmlError> schemaViolations) {
        this.schemaViolations = schemaViolations;
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public List<XmlError> getSchemaViolations() {
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
