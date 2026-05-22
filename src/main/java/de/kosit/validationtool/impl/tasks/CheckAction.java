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
package de.kosit.validationtool.impl.tasks;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;

import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.reportInput.ProcessingError;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;
import net.sf.saxon.s9api.XdmNode;

/**
 * Interface implemented by all check steps. The parameter of type {@link Bag} serves both as a source for input
 * parameters and for collecting results that should be forwarded to further steps.
 *
 * @author Andreas Penski
 */
@FunctionalInterface
public interface CheckAction {

    /**
     * Transport class for input and output objects for the individual check steps.
     */
    class Bag {

        private Result<Scenario, String> scenarioSelectionResult;

        private final CreateReportInput reportInput;

        /**
         * The final result
         */
        private XdmNode report;

        private boolean finished;

        private boolean stopped;

        private AcceptRecommendation acceptStatus = AcceptRecommendation.UNDEFINED;

        /**
         * The document to validate
         */
        private Input input;

        private Result<XdmNode, XMLSyntaxError> parserResult;

        private Result<Integer, String> assertionResult;

        private Result<Boolean, XMLSyntaxError> schemaValidationResult;

        public Bag(final Input input) {
            this(input, new CreateReportInput());
        }

        public Bag(final Input input, final CreateReportInput reportInput) {
            this.input = input;
            this.reportInput = reportInput;
        }

        /**
         * Indicates an early stop in processing.
         * 
         * @param error Error text
         */
        public void stopProcessing(final String error) {
            stopProcessing(Collections.singleton(error));
        }

        public void stopProcessing(final Collection<String> errors) {
            this.stopped = true;
            if (this.reportInput.getProcessingError() == null) {
                this.reportInput.setProcessingError(new ProcessingError());
            }
            this.reportInput.getProcessingError().getError().addAll(errors);
        }

        public void addProcessingError(final String msg) {
            stopProcessing(msg);
        }

        /**
         * Returns the name of the test object; any path information is stripped off.
         *
         * @return the name of the test object
         */
        public String getName() {
            final String fileName = getInput().getName().replaceAll(".*/|.*\\\\", "");
            return FilenameUtils.getBaseName(fileName);
        }

        public Result<Scenario, String> getScenarioSelectionResult() {
            return this.scenarioSelectionResult;
        }

        public CreateReportInput getReportInput() {
            return this.reportInput;
        }

        /**
         * The final result
         */
        public XdmNode getReport() {
            return this.report;
        }

        public boolean isFinished() {
            return this.finished;
        }

        public boolean isStopped() {
            return this.stopped;
        }

        public AcceptRecommendation getAcceptStatus() {
            return this.acceptStatus;
        }

        /**
         * The document to validate
         */
        public Input getInput() {
            return this.input;
        }

        public Result<XdmNode, XMLSyntaxError> getParserResult() {
            return this.parserResult;
        }

        public Result<Integer, String> getAssertionResult() {
            return this.assertionResult;
        }

        public Result<Boolean, XMLSyntaxError> getSchemaValidationResult() {
            return this.schemaValidationResult;
        }

        public void setScenarioSelectionResult(final Result<Scenario, String> scenarioSelectionResult) {
            this.scenarioSelectionResult = scenarioSelectionResult;
        }

        /**
         * The final result
         */
        public void setReport(final XdmNode report) {
            this.report = report;
        }

        public void setFinished(final boolean finished) {
            this.finished = finished;
        }

        public void setStopped(final boolean stopped) {
            this.stopped = stopped;
        }

        public void setAcceptStatus(final AcceptRecommendation acceptStatus) {
            this.acceptStatus = acceptStatus;
        }

        /**
         * The document to validate
         */
        public void setInput(final Input input) {
            this.input = input;
        }

        public void setParserResult(final Result<XdmNode, XMLSyntaxError> parserResult) {
            this.parserResult = parserResult;
        }

        public void setAssertionResult(final Result<Integer, String> assertionResult) {
            this.assertionResult = assertionResult;
        }

        public void setSchemaValidationResult(final Result<Boolean, XMLSyntaxError> schemaValidationResult) {
            this.schemaValidationResult = schemaValidationResult;
        }
    }

    /**
     * Executes the check step and extends the gathered information.
     *
     * @param results the information collection
     */
    void check(Bag results);

    /**
     * Determines whether a step can possibly be skipped. The function is called before the actual check action and can
     * therefore prevent execution of the check step. Developers can override this function to execute the check step
     * conditionally.
     *
     * @param results the information gathered so far
     * @return <code>true</code> if the step should be skipped
     */
    default boolean isSkipped(final Bag results) {
        return false;
    }
}
