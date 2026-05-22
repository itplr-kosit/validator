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

package de.kosit.validationtool.api;

import java.util.List;

import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.w3c.dom.Document;

import de.kosit.validationtool.impl.model.CustomFailedAssert;
import net.sf.saxon.s9api.XdmNode;

/**
 * API result object holding various information of the validation process results.
 *
 * @author Andreas Penski
 */
public interface Result {

    /**
     * Indicates whether processing by the validator completed successfully. This function explicitly makes no statement
     * about acceptance.
     *
     * @return true if processing completed in full and successfully
     * @see #getAcceptRecommendation()
     */
    boolean isProcessingSuccessful();

    /**
     * Returns a list of processing error messages.
     *
     * @return list of error messages
     */
    List<String> getProcessingErrors();

    /**
     * The generated report.
     */
    XdmNode getReport();

    /**
     * The Recommendation based on the evaluation of this Result.
     *
     * @return AcceptRecommendation
     */
    AcceptRecommendation getAcceptRecommendation();

    /**
     * Returns the report as a W3C {@link Document}.
     *
     * @return the report
     */
    Document getReportDocument();

    /**
     * Shortcut to the recommendation for further processing of the document.
     *
     * @return <code>true</code> if {@link AcceptRecommendation#ACCEPTABLE}
     */
    boolean isAcceptable();

    /**
     * Returns a list of schema validation errors that were found. This list is empty if no errors were found.
     *
     * @return List of schema validation errors.
     */
    List<XmlError> getSchemaViolations();

    /**
     * Returns the results of the Schematron checks, in the order of the scenario configuration.
     *
     * @return List with Schematron results
     */
    List<SchematronOutput> getSchematronResult();

    /**
     * @return List of custom failed asserts per Schematron level. Only failed assertions with a custom level are
     *         contained. Never <code>null</code> but maybe empty.
     */
    List<CustomFailedAssert> getCustomFailedAsserts();

    /**
     * Returns {@link org.oclc.purl.dsdl.svrl.FailedAssert FailedAsserts} of a schematron evaluation.
     *
     * @return list of {@link org.oclc.purl.dsdl.svrl.FailedAssert FailedAsserts}, if any, empty list otherwise
     */
    List<FailedAssert> getFailedAsserts();

    /**
     * Returns true if no schema violations are present.
     *
     * @return <code>true</code> if XML Schema compliant
     */
    boolean isSchemaValid();

    /**
     * Returns true if the test object is a well-formed XML file.
     *
     * @return <code>true</code> if wellformed
     */
    boolean isWellformed();

    /**
     * Returns true, if schematron has been checked and the result does not contain any {@link FailedAssert
     * FailedAsserts}.
     *
     * @return <code>true</code>, if valid
     */
    boolean isSchematronValid();
}
