package org.kosit.validator.impl.conformatron.report;

import org.kosit.validator.impl.conformatron.action.PrepareRulesAction;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLResult;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.action.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction;

import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.conformatron.api.model.validation.CTValidationSyntax;

/**
 * <b>DRAFT intermediate format</b>: serializes a canonical pipeline run (steps 2–8) to a CVRL report — an XVRL profile
 * per {@code conformance-validation-report-spec.md}. Every opinionated default is tagged {@code D<n>} and listed as an
 * explicit decision point in the CVRL workshop document; nothing here is final until the team confirms.
 *
 * <ul>
 * <li><b>D1</b> extension namespace: {@code urn:conformatron:cvrl:draft} (spec placeholder was
 * {@code xmlns:cvrl="my"})</li>
 * <li><b>D2</b> canonical creator names come from {@code ECTCanonicalAction} (the spec's {@code document-loader} etc.
 * are treated as outdated)</li>
 * <li><b>D3</b> one {@code <report>} per step execution; APPLY_RULES emits one report <b>per rule set</b> (sequential,
 * not nested)</li>
 * <li><b>D4</b> consistent digest attribute set: {@code valid}, {@code worst-severity}, {@code fatal-error-count},
 * {@code error-count}, {@code warning-count}, {@code error-codes} (distinct, space-separated)</li>
 * <li><b>D5</b> root carries {@code cvrl:conformant} and {@code cvrl:status} (COMPLETED | CANCELLED); a cancelled run
 * still serializes — partial CVRL per ADR-004</li>
 * <li><b>D6</b> verbosity: full only, document by reference + checksum (no document embedding yet)</li>
 * <li><b>D7</b> scenario identity travels as detections (no {@code metadata/document} scenario embedding)</li>
 * <li><b>D8</b> APPLY_RULES reports carry {@code <schema href language>} plus {@code cvrl:engine-version} /
 * {@code cvrl:phase} from the prepared rule set</li>
 * <li><b>D9</b> detections carry {@code cvrl:line}/{@code cvrl:col} when known; the XPath stays in the message</li>
 * <li><b>D10</b> per-report timestamps are the serialization time — the true execution time needs
 * {@code ICTActionExecution} (not implemented yet)</li>
 * </ul>
 *
 * @author Andreas Schmitz
 */
public final class CvrlWriter {

    /** XVRL namespace — CVRL is a profile of XVRL, the report must validate against it. */
    public static final String NS_XVRL = "http://www.xproc.org/ns/xvrl";

    /** D1: draft namespace for the CVRL extension attributes. */
    public static final String NS_CVRL = "urn:conformatron:cvrl:draft";

    /**
     * The results of one canonical pipeline run. Fields from the cancellation point onwards are {@code null} — the
     * report then contains only the executed steps and {@code cvrl:status="CANCELLED"} (partial CVRL, ADR-004).
     */
    public record PipelineResults(ParseXMLResult parse, DetectScenariosAction.DetectScenariosResult detect,
            SelectScenarioAction.SelectScenarioResult select, RetrieveArtifactsAction.RetrieveArtifactsResult retrieve,
            PrepareRulesAction.PrepareRulesResult prepare, ApplyRulesAction.ApplyRulesActionResult apply,
            ComputeConformanceAction.ComputeConformanceActionResult conformance) {

        public boolean isCompleted() {
            return this.conformance != null;
        }

        public boolean isConformant() {
            return isCompleted() && !this.conformance.result().hasNonConformantTarget();
        }
    }

    private final String validatorName;

    private final String validatorVersion;

    public CvrlWriter(final String validatorName, final String validatorVersion) {
        this.validatorName = validatorName;
        this.validatorVersion = validatorVersion;
    }

    /**
     * Serializes the pipeline run to a CVRL report.
     *
     * @param documentName reference to the document under test (used when parsing failed before a source existed)
     * @param results the pipeline results; {@code parse} must not be {@code null}
     * @param out the target stream (UTF-8)
     */
    public void write(final String documentName, final PipelineResults results, final OutputStream out) {
        if (results == null || results.parse() == null) {
            throw new IllegalArgumentException("results with at least the parse step are required");
        }
        try {
            final XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            newline(writer, 0);
            writer.writeStartElement("reports");
            writer.writeDefaultNamespace(NS_XVRL);
            writer.writeNamespace("cvrl", NS_CVRL);
            // D5: overall verdict + run status on the root
            writer.writeAttribute(NS_CVRL, "conformant", String.valueOf(results.isConformant()));
            writer.writeAttribute(NS_CVRL, "status", results.isCompleted() ? "COMPLETED" : "CANCELLED");

            writeRootMetadata(writer, documentName, results.parse());
            writeStepReport(writer, CTActionType.PARSE_DOCUMENT, results.parse().getDetectionList(), null);
            if (results.detect() != null) {
                writeStepReport(writer, CTActionType.DETECT_SCENARIOS, results.detect().detections(), null);
            }
            if (results.select() != null) {
                writeStepReport(writer, CTActionType.SELECT_SCENARIO, results.select().detections(), null);
            }
            if (results.retrieve() != null) {
                writeStepReport(writer, CTActionType.RETRIEVE_ARTIFACTS, results.retrieve().detections(), null);
            }
            if (results.prepare() != null) {
                writeStepReport(writer, CTActionType.PREPARE_RULES, results.prepare().detections(), null);
            }
            if (results.apply() != null) {
                // D3: one report per rule set execution, in scenario order
                for (final Map.Entry<CTPreparedRuleSet, CTDetectionList> entry : results.apply().result().getResultsByRuleSet()
                        .entrySet()) {
                    writeStepReport(writer, CTActionType.APPLY_RULES, entry.getValue(), entry.getKey());
                }
            }
            if (results.conformance() != null) {
                writeStepReport(writer, CTActionType.COMPUTE_CONFORMANCE, results.conformance().detections(), null);
            }
            newline(writer, 0);
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
        } catch (final XMLStreamException e) {
            throw new IllegalStateException("Can not serialize CVRL report for " + documentName, e);
        }
    }

    private void writeRootMetadata(final XMLStreamWriter writer, final String documentName, final ParseXMLResult parse)
            throws XMLStreamException {
        newline(writer, 1);
        writer.writeStartElement(NS_XVRL, "metadata");
        newline(writer, 2);
        writer.writeStartElement(NS_XVRL, "timestamp");
        writer.writeCharacters(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
        writer.writeEndElement();
        newline(writer, 2);
        writer.writeEmptyElement(NS_XVRL, "validator");
        writer.writeAttribute("name", this.validatorName);
        writer.writeAttribute("version", this.validatorVersion);
        newline(writer, 2);
        writer.writeEmptyElement(NS_XVRL, "document");
        writer.writeAttribute("href", documentName);
        // D6: document by reference + checksum; audit-mode embedding is an open question
        final CTParsedValidationSource source = parse.getParsedSource();
        if (source != null) {
            writer.writeAttribute(NS_CVRL, "checksum", HexFormat.of().formatHex(source.getHashBytes()));
            writer.writeAttribute(NS_CVRL, "checksum-algorithm", source.getHashAlgorithmName());
        }
        newline(writer, 1);
        writer.writeEndElement();
    }

    private void writeStepReport(final XMLStreamWriter writer, final CTActionType action, final CTDetectionList detections,
            final CTPreparedRuleSet ruleSet) throws XMLStreamException {
        newline(writer, 1);
        writer.writeStartElement(NS_XVRL, "report");
        newline(writer, 2);
        writer.writeStartElement(NS_XVRL, "metadata");
        newline(writer, 3);
        writer.writeEmptyElement(NS_XVRL, "creator");
        // D2: the canonical names of ECTCanonicalAction are normative for <creator @name>
        writer.writeAttribute("name", action.getName());
        newline(writer, 3);
        // D10: serialization time, not execution time (needs ICTActionExecution)
        writer.writeStartElement(NS_XVRL, "timestamp");
        writer.writeCharacters(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
        writer.writeEndElement();
        if (ruleSet != null) {
            // D8: rule set identity on the APPLY_RULES report
            newline(writer, 3);
            writer.writeEmptyElement(NS_XVRL, "schema");
            writer.writeAttribute("href", ruleSet.getArtifactReference().getValidationArtifactReference().toString());
            writer.writeAttribute("language", ruleSet.getEngineType().getBaseType() == CTValidationSyntax.XSD ? "XSD" : "Schematron");
            if (ruleSet.getEngineVersion() != null) {
                writer.writeAttribute(NS_CVRL, "engine-version", ruleSet.getEngineVersion());
            }
            if (ruleSet.getPhase() != null) {
                writer.writeAttribute(NS_CVRL, "phase", ruleSet.getPhase());
            }
        }
        newline(writer, 2);
        writer.writeEndElement();
        writeDigest(writer, detections);
        for (final CTDetection detection : detections.getAll()) {
            writeDetection(writer, detection);
        }
        newline(writer, 1);
        writer.writeEndElement();
    }

    private static void writeDigest(final XMLStreamWriter writer, final CTDetectionList detections) throws XMLStreamException {
        // D4: consistent digest attribute set
        final long fatals = detections.getCount(d -> d.getSeverity() == CTStandardSeverity.ERROR);
        final long errors = detections.getCount(d -> d.getSeverity() == CTStandardSeverity.ERROR);
        final long warnings = detections.getCount(d -> d.getSeverity() == CTStandardSeverity.WARNING);
        final Set<String> errorCodes = new LinkedHashSet<>();
        detections.getAll().stream().filter(d -> d.getSeverity().isError()).forEach(d -> errorCodes.add(d.getCode()));
        newline(writer, 2);
        writer.writeEmptyElement(NS_XVRL, "digest");
        writer.writeAttribute("valid", String.valueOf(!detections.containsAtLeastOneError()));
        writer.writeAttribute("worst-severity", severityId(detections));
        writer.writeAttribute("fatal-error-count", String.valueOf(fatals));
        writer.writeAttribute("error-count", String.valueOf(errors));
        writer.writeAttribute("warning-count", String.valueOf(warnings));
        writer.writeAttribute("error-codes", String.join(" ", errorCodes));
    }

    private static void writeDetection(final XMLStreamWriter writer, final CTDetection detection) throws XMLStreamException {
        newline(writer, 2);
        writer.writeStartElement(NS_XVRL, "detection");
        writer.writeAttribute("severity", detection.getSeverity().getID());
        writer.writeAttribute("code", detection.getCode());
        // D9: line/col as extension attributes when known; the XPath location stays in the message
        if (detection.getLocation().getLineNumber() > 0) {
            writer.writeAttribute(NS_CVRL, "line", String.valueOf(detection.getLocation().getLineNumber()));
        }
        if (detection.getLocation().getColumnNumber() > 0) {
            writer.writeAttribute(NS_CVRL, "col", String.valueOf(detection.getLocation().getColumnNumber()));
        }
        newline(writer, 3);
        writer.writeStartElement(NS_XVRL, "message");
        writer.writeCharacters(detection.getText().getDisplayTextLocaleIndependent());
        writer.writeEndElement();
        newline(writer, 2);
        writer.writeEndElement();
    }

    private static String severityId(final CTDetectionList detections) {
        return detections.getCount() == 0 ? "info" : detections.getWorstSeverity().getID();
    }

    private static void newline(final XMLStreamWriter writer, final int indent) throws XMLStreamException {
        writer.writeCharacters("\n" + "  ".repeat(indent));
    }
}
