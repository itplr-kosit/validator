package org.kosit.validator.api;

import java.util.List;

import org.kosit.base.error.SimpleError;
import org.kosit.validator.api.xvrl.compact.AcceptRecommendation;
import org.kosit.xvrl.model.XvrlReports;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.w3c.dom.Document;

import net.sf.saxon.s9api.XdmNode;

/**
 * API result object holding various information of the validation process results.
 *
 * @author Andreas Penski
 */
@Deprecated(since = "2.0.0")
public interface VResult {

    /**
     * Indicates whether the processing by the validator was completed successfully. This function explicitly makes no
     * statement about acceptance.
     *
     * @return true if the processing was completed fully and successfully
     * @see #getAcceptRecommendation()
     */
    @Deprecated(since = "2.0.0")
    boolean isProcessingSuccessful();

    /**
     * Returns a list of processing error messages.
     *
     * @return list of error messages
     */
    @Deprecated(since = "2.0.0")
    List<String> getProcessingErrors();

    /**
     * The generated report.
     */
    @Deprecated(since = "2.0.0")
    XdmNode getReport();

    @Deprecated(since = "2.0.0")
    XvrlReports getReportSummary();

    /**
     * The Recommendation based on the evaluation of this Result.
     *
     * @return AcceptRecommendation
     */
    @Deprecated(since = "2.0.0")
    AcceptRecommendation getAcceptRecommendation();

    /**
     * Returns the report as a W3C {@link Document}.
     *
     * @return the report
     */
    @Deprecated(since = "2.0.0")
    Document getReportDocument();

    /**
     * Quick access to the recommendation for further processing of the document.
     *
     * @return true if {@link AcceptRecommendation#ACCEPTABLE}
     */
    @Deprecated(since = "2.0.0")
    boolean isAcceptable();

    /**
     * Returns a list of schema validation errors found. This list is empty if no errors were found.
     */
    @Deprecated(since = "2.0.0")
    List<SimpleError> getSchemaViolations();

    /**
     * Returns the results of the Schematron validations, in the order of the scenario configuration.
     *
     * @return list of Schematron results
     */
    @Deprecated(since = "2.0.0")
    List<SchematronOutputType> getSchematronResult();

    /**
     * Returns {@link org.oclc.purl.dsdl.svrl.FailedAssert FailedAsserts} of a schematron evaluation.
     *
     * @return list of {@link org.oclc.purl.dsdl.svrl.FailedAssert FailedAsserts}, if any, empty list otherwise
     */
    @Deprecated(since = "2.0.0")
    List<FailedAssert> getFailedAsserts();

    /**
     * Returns true if the test document is a well-formed XML file.
     *
     * @return true if well-formed
     */
    @Deprecated(since = "2.0.0")
    boolean isWellformed();

    /**
     * Returns true if no schema violations are present.
     *
     * @return true if schema-valid
     */
    @Deprecated(since = "2.0.0")
    boolean isSchemaValid();

    /**
     * Returns true, if schematron has been checked and the result does not contain any {@link FailedAssert
     * FailedAsserts}.
     *
     * @return true, if valid
     */
    @Deprecated(since = "2.0.0")
    boolean isSchematronValid();
}
