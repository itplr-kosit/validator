package org.kosit.validator.impl.conformatron;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.conformance.CTDecision;
import org.conformatron.api.model.detection.CTDetection;
import org.kosit.validator.impl.conformatron.report.CvrlWriter;

/**
 * The result of a full conformance validation: what the run decided, and the run itself.
 * <p>
 * Serialization is a capability <b>beside</b> the result, not its type (ADR-008): the result answers the questions a
 * caller has — accepted or not, why, what was detected — and can write the CVR when one is wanted. No XVRL, SVRL, DOM
 * or Saxon type appears in this contract.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class ConformanceValidationResult {

    private final String documentName;

    private final PipelineResults run;

    private final CvrlWriter writer;

    public ConformanceValidationResult(final String documentName, final PipelineResults run, final CvrlWriter writer) {
        if (run == null || writer == null) {
            throw new IllegalArgumentException("run and writer may not be null");
        }
        this.documentName = documentName;
        this.run = run;
        this.writer = writer;
    }

    /**
     * The verdict of step 9. Always present — also for a cancelled run, which is a {@link CTDecision#REJECT} naming the
     * cancelling step.
     *
     * @return the decision. Never {@code null}.
     */
    public CTDecision getDecision() {
        return this.run.decision().decision();
    }

    /**
     * @return why the run decided the way it did, in one sentence. Never {@code null}.
     */
    public String getRationale() {
        return this.run.decision().rationale();
    }

    /**
     * Whether the run reached a conformance statement and no target was non-conformant. Note the difference to
     * {@link #getDecision()}: a run can be inconclusive, which is neither conformant nor a rejection.
     *
     * @return {@code true} if every conformance target is conformant
     */
    public boolean isConformant() {
        return this.run.isConformant();
    }

    /**
     * @return {@code true} if the pipeline ran to the end; {@code false} if a step cancelled it
     */
    public boolean isCompleted() {
        return this.run.isCompleted();
    }

    /**
     * @return the step that cancelled the run, or {@code null} when it completed
     */
    public CTActionType getCancelledAt() {
        return this.run.cancelledAt();
    }

    /**
     * @return every detection of the run, in pipeline order. Never {@code null}.
     */
    public List<CTDetection> getAllDetections() {
        return this.run.allDetections();
    }

    /**
     * @return the raw step results — for callers that work on the run itself rather than on its verdict. Never
     *         {@code null}.
     */
    public PipelineResults getRun() {
        return this.run;
    }

    /**
     * @return the name the document was validated under. Never {@code null}.
     */
    public String getDocumentName() {
        return this.documentName;
    }

    /**
     * Writes the run as a CVR report. A cancelled run yields a partial report covering the steps it reached (ADR-004).
     *
     * @param out the target stream (UTF-8); not closed by this method
     * @throws IOException if the report can not be written
     */
    public void writeCvr(final OutputStream out) throws IOException {
        this.writer.write(this.documentName, this.run, out);
    }
}
