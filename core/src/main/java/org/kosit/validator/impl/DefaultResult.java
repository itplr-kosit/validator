package org.kosit.validator.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.api.Result;
import org.kosit.validator.api.XmlError;
import org.kosit.validator.impl.tasks.ReaderWrapper;
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

public class DefaultResult implements Result {

    /** The internal report 'preliminary stage' produced by the validator */
    private XVRLReportSummary reportSummary;

    /** The evaluated result. */
    private final AcceptRecommendation acceptRecommendation;

    private List<XmlError> schemaViolations;

    private List<SchematronOutputType> schematronResult;

    private boolean processingSuccessful;

    private boolean wellformed;

    public DefaultResult(final AcceptRecommendation recommendation) {
        this.acceptRecommendation = recommendation;
    }

    @Override
    public XdmNode getReport() {
        final Marshaller marshaller;
        try {
            marshaller = new XvrlConversionService().getJaxbContext().createMarshaller();
            final JAXBSource source = new JAXBSource(marshaller, getReportSummary());
            // wrap to circumvent inconsistency between sax and saxon
            source.setXMLReader(new ReaderWrapper(source.getXMLReader()));
            return new Processor(false).newDocumentBuilder().build(source);
        } catch (JAXBException | SaxonApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> getProcessingErrors() {
        return reportSummary.getAllErrors();
    }

    /**
     * Returns the report as a W3C {@link Document}.
     *
     * @return the report
     */
    @Override
    public Document getReportDocument() {
        return (Document) NodeOverNodeInfo.wrap(getReport().getUnderlyingNode());
    }

    /**
     * Quick access to the recommendation for further processing of the document.
     *
     * @return true if {@link AcceptRecommendation#ACCEPTABLE}
     */
    @Override
    public boolean isAcceptable() {
        return isProcessingSuccessful() && AcceptRecommendation.ACCEPTABLE.equals(this.acceptRecommendation);
    }

    @Override
    public boolean isSchemaValid() {
        return getSchemaViolations() != null && getSchemaViolations().isEmpty();
    }

    /**
     * Returns all Schematron results of type {@link FailedAssert}.
     *
     * @return the {@link FailedAssert}
     */
    @Override
    public List<FailedAssert> getFailedAsserts() {
        return getSchematronResult() != null
                ? getSchematronResult().stream().flatMap(e -> e.getActivePatternOrActiveGroupAndFiredRule().stream())
                        .filter(FailedAssert.class::isInstance).map(FailedAssert.class::cast).collect(Collectors.toList())
                : Collections.emptyList();
    }

    private boolean isSchematronEvaluated() {
        return getSchematronResult() != null
                && getSchematronResult().stream().noneMatch(e -> e.getActivePatternOrActiveGroupAndFiredRule().isEmpty());
    }

    @Override
    public boolean isSchematronValid() {
        return isSchematronEvaluated() && getFailedAsserts().isEmpty();
    }

    /**
     * Die vom Validator erstelle interne Berichts-'Vorstufe'
     */
    public XVRLReportSummary getReportSummary() {
        return this.reportSummary;
    }

    /**
     * Die vom Validator erstelle interne Berichts-'Vorstufe'
     */
    void setReportSummary(final XVRLReportSummary reportSummary) {
        this.reportSummary = reportSummary;
    }

    /**
     * Das evaluierte Ergebnis.
     */
    public AcceptRecommendation getAcceptRecommendation() {
        return this.acceptRecommendation;
    }

    void setSchemaViolations(final List<XmlError> schemaViolations) {
        this.schemaViolations = schemaViolations;
    }

    public List<XmlError> getSchemaViolations() {
        return this.schemaViolations;
    }

    public List<SchematronOutputType> getSchematronResult() {
        return this.schematronResult;
    }

    void setSchematronResult(final List<SchematronOutputType> schematronResult) {
        this.schematronResult = schematronResult;
    }

    public boolean isProcessingSuccessful() {
        return this.processingSuccessful;
    }

    public void setProcessingSuccessful(final boolean processingSuccessful) {
        this.processingSuccessful = processingSuccessful;
    }

    public boolean isWellformed() {
        return this.wellformed;
    }

    public void setWellformed(final boolean wellformed) {
        this.wellformed = wellformed;
    }
}
