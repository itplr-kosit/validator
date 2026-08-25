package org.kosit.validator.impl.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.conformatron.api.model.scenario.CTScenarioMatch;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.kosit.validator.api.VInput;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.xvrl.model.XVRLMetadata;
import org.kosit.xvrl.model.XVRLReport;
import org.kosit.xvrl.model.XVRLReportSummary;

/**
 * Interface that is implemented by all check steps. The parameter of type {@link Process} serves both as a source for
 * input parameters and as a container for results to be forwarded to further steps.
 *
 * @author Andreas Penski
 */
@FunctionalInterface
public interface CheckTask {

    /**
     * Executes the check step and extends the collected information.
     *
     * @param results the information collection
     */
    ProcessStepResult<?, ?> check(Process results);

    /**
     * Determines whether a step may be skipped. This function is called before the actual check action and can
     * therefore prevent the execution of the check step. Developers can override this function to conditionally execute
     * the check step.
     *
     * @param results the previously collected information
     * @return <code>true</code> if the step should be skipped
     */
    default boolean isSkipped(final Process results) {
        return false;
    }

    /**
     * Transport class for input and output objects for the individual check steps.
     */
    class Process {

        private XVRLMetadata metadata;

        private List<ProcessStepResult<?, ?>> processStepResults = new ArrayList<>();

        private boolean finished;

        private boolean stopped;

        /** The document to be checked */
        private VInput input;

        /**
         * Conformatron handshake object carrying the parsed document (immutable byte array, SHA-512 hash, parsed
         * representation). Set by the parse step; {@code null} until parsing succeeded or when the input was already
         * parsed ({@code XdmNodeInput} shortcut). Successor of {@link #getInput()} — downstream steps migrate to this
         * step by step.
         */
        private CTParsedValidationSource parsedSource;

        /**
         * Conformatron handshake object of step 3 ({@code DETECT_SCENARIOS}): all detected scenario matches.
         * {@code null} until detection ran successfully; contains more than one entry on ambiguous configurations.
         */
        private List<CTScenarioMatch> scenarioMatches;

        /**
         * Conformatron handshake object of step 4 ({@code SELECT_SCENARIO}): the selected scenario match. {@code null}
         * until scenario selection succeeded, when the fallback scenario applies (not representable as a match), on
         * ambiguity or when no parsed source is available.
         */
        private CTScenarioMatch scenarioMatch;

        public Process(final VInput input) {
            this(input, new XVRLMetadata());
        }

        public Process(final VInput input, final XVRLMetadata xvrlMetadata) {
            this.input = input;
            this.metadata = xvrlMetadata;
        }

        public void addStepResult(final ProcessStepResult<?, ?> result) {
            this.processStepResults.add(result);
        }

        public XVRLReportSummary getXvrlReportSummary() {
            final XVRLReportSummary summary = new XVRLReportSummary();
            summary.setMetadata(this.metadata);
            summary.getReports()
                    .addAll(this.processStepResults.stream().flatMap(processStepResult -> processStepResult.getReport().stream()).toList());
            return summary;
        }

        public <T, E> List<XVRLReport> getReports(final Key<T, E> key) {
            return getActionResult(key).map(ProcessStepResult::getReport).orElse(null);
        }

        public <T, E> Optional<ProcessStepResult<T, E>> getActionResult(final Key<T, E> key) {
            final ProcessStepResult<T, E> result = (ProcessStepResult<T, E>) this.processStepResults.stream().filter(b -> b.getKey() == key)
                    .findFirst().orElse(null);
            return Optional.ofNullable(result);
        }

        public <T, E> Result<T, E> getResult(final Key<T, E> type) {
            return getActionResult(type).map(ProcessStepResult::getResult).orElse(null);
        }

        /**
         * Returns the name of the test document, with any path information stripped.
         *
         * @return the name of the test document
         */
        public String getName() {
            final String fileName = getInput().getName().replaceAll(".*/|.*\\\\", "");
            return FilenameUtils.getBaseName(fileName);
        }

        public static class Key<T, E> {

            private final Class<T> type;

            private final Class<E> other;

            public Class<T> getType() {
                return this.type;
            }

            public Class<E> getOther() {
                return this.other;
            }

            public Key(final Class<T> type, final Class<E> other) {
                this.type = type;
                this.other = other;
            }
        }

        public XVRLMetadata getMetadata() {
            return this.metadata;
        }

        public List<ProcessStepResult<?, ?>> getProcessStepResults() {
            return this.processStepResults;
        }

        public boolean isFinished() {
            return this.finished;
        }

        public boolean isStopped() {
            return this.stopped;
        }

        /**
         * Das zu prüfende Dokument
         */
        public VInput getInput() {
            return this.input;
        }

        public void setMetadata(final XVRLMetadata metadata) {
            this.metadata = metadata;
        }

        public void setProcessStepResults(final List<ProcessStepResult<?, ?>> processStepResults) {
            this.processStepResults = processStepResults;
        }

        public void setFinished(final boolean finished) {
            this.finished = finished;
        }

        public void setStopped(final boolean stopped) {
            this.stopped = stopped;
        }

        /**
         * Das zu prüfende Dokument
         */
        public void setInput(final VInput input) {
            this.input = input;
        }

        /**
         * The parsed document as conformatron handshake object; {@code null} until the parse step succeeded.
         */
        public CTParsedValidationSource getParsedSource() {
            return this.parsedSource;
        }

        public void setParsedSource(final CTParsedValidationSource parsedSource) {
            this.parsedSource = parsedSource;
        }

        /**
         * All detected scenarios as conformatron handshake objects (step 3); {@code null} until detection succeeded.
         */
        public List<CTScenarioMatch> getScenarioMatches() {
            return this.scenarioMatches;
        }

        public void setScenarioMatches(final List<CTScenarioMatch> scenarioMatches) {
            this.scenarioMatches = scenarioMatches;
        }

        /**
         * The selected scenario as conformatron handshake object (step 4); {@code null} until scenario selection
         * succeeded or when the fallback scenario applies.
         */
        public CTScenarioMatch getScenarioMatch() {
            return this.scenarioMatch;
        }

        public void setScenarioMatch(final CTScenarioMatch scenarioMatch) {
            this.scenarioMatch = scenarioMatch;
        }
    }
}
