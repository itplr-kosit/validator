/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.kosit.validationtool.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.api.XmlError;
import de.kosit.validationtool.impl.model.CustomFailedAssert;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.XdmNode;

/**
 * The default {@link Result} for returning in the API
 *
 * @author Andreas Penski
 */
public class DefaultResult implements Result {

    /**
     * The generated report.
     */
    private final XdmNode report;

    /**
     * The internal report 'preliminary stage' created by the validator.
     */
    private CreateReportInput reportInput;

    /**
     * The evaluated result.
     */
    private final AcceptRecommendation acceptRecommendation;

    private final HtmlExtractor htmlExtraction;

    private List<XmlError> schemaViolations;

    private List<SchematronOutput> schematronResult;

    /**
     * List of custom failed asserts per Schematron level. Only failed assertions with a custom level are contained.
     */
    private List<CustomFailedAssert> customFailedAsserts;

    private boolean processingSuccessful;

    private boolean wellformed;

    public DefaultResult(final XdmNode report, final AcceptRecommendation recommendation, final HtmlExtractor htmlExtractor) {
        this.report = report;
        this.acceptRecommendation = recommendation;
        this.htmlExtraction = htmlExtractor;
    }

    @Override
    public List<String> getProcessingErrors() {
        return getReportInput().getProcessingError() != null ? getReportInput().getProcessingError().getError() : Collections.emptyList();
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
     * Extracts any HTML fragments present in the report as String.
     *
     * @return list of HTML strings.
     */
    public List<String> extractHtmlAsString() {
        return this.htmlExtraction.extractAsString(getReport());
    }

    /**
     * Extracts any HTML fragments present in the report.
     *
     * @return list of HTML nodes.
     */
    public List<XdmNode> extractHtml() {
        return this.htmlExtraction.extract(getReport());
    }

    /**
     * Extracts any HTML fragments present in the report as {@link Element}.
     *
     * @return list of HTML elements.
     */
    public List<Element> extractHtmlAsElement() {
        return this.htmlExtraction.extractAsElement(getReport());
    }

    /**
     * Returns all Schematron results of type {@link FailedAssert}.
     *
     * @return the {@link FailedAssert}
     */
    @Override
    public List<FailedAssert> getFailedAsserts() {
        return filterSchematronResult(FailedAssert.class);
    }

    private <T> List<T> filterSchematronResult(final Class<T> type) {
        return this.schematronResult != null
                ? this.schematronResult.stream().flatMap(e -> e.getActivePatternAndFiredRuleAndFailedAssert().stream())
                        .filter(type::isInstance).map(type::cast).collect(Collectors.toList())
                : Collections.emptyList();
    }

    private boolean isSchematronEvaluated() {
        return this.schematronResult != null
                && this.schematronResult.stream().noneMatch(e -> e.getActivePatternAndFiredRuleAndFailedAssert().isEmpty());
    }

    @Override
    public boolean isSchematronValid() {
        return isSchematronEvaluated() && getFailedAsserts().isEmpty();
    }

    @Override
    public List<CustomFailedAssert> getCustomFailedAsserts() {
        return this.customFailedAsserts;
    }

    public void setCustomFailedAsserts(List<CustomFailedAssert> customFailedAsserts) {
        this.customFailedAsserts = customFailedAsserts;
    }

    /**
     * The generated report.
     */
    public XdmNode getReport() {
        return this.report;
    }

    /**
     * The internal report 'preliminary stage' created by the validator.
     */
    public CreateReportInput getReportInput() {
        return this.reportInput;
    }

    /**
     * The internal report 'preliminary stage' created by the validator.
     */
    void setReportInput(final CreateReportInput reportInput) {
        this.reportInput = reportInput;
    }

    /**
     * The evaluated result.
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

    public List<SchematronOutput> getSchematronResult() {
        return this.schematronResult;
    }

    void setSchematronResult(final List<SchematronOutput> schematronResult) {
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
