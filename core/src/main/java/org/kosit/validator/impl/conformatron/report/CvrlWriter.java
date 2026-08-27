package org.kosit.validator.impl.conformatron.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTSeverity;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.conformatron.api.model.source.CTParsedValidationSourceXML;
import org.conformatron.api.model.source.CTReadResource;
import org.conformatron.api.model.validation.CTValidationStandard;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction;
import org.kosit.validator.impl.conformatron.action.PrepareRulesAction;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.XmlDetection;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosAction;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.ScenarioDetection;
import org.kosit.validator.impl.conformatron.util.ScenarioXml;
import org.w3c.dom.Document;

/**
 * <b>DRAFT intermediate format</b>: serializes a canonical pipeline run (steps 2–8) to a CVRL report — an XVRL profile
 * per {@code conformance-validation-report-spec.md}. Every opinionated default is tagged {@code D<n>} and listed as an
 * explicit decision point; nothing here is final until the team confirms.
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
 * <li><b>D6</b> verbosity: full only. Document by reference in the root metadata; hash and parsed document are
 * <b>output</b> of the parse step and travel as two messages of the {@code document-parsed} detection — message 1
 * carries the hash (algorithm as {@code cvrl:algorithm}, hash over the retained source bytes), message 2 embeds the
 * parsed document ({@code cvrl:mime-type}). The payload is only written on parse success — failed content is never
 * echoed (injection safety). Open point: a failed parse currently loses the document hash in the report.</li>
 * <li><b>D7</b> scenario identity travels as detections (no {@code metadata/document} scenario embedding)</li>
 * <li><b>D8</b> APPLY_RULES reports carry {@code <schema href language>} plus {@code cvrl:engine-version} /
 * {@code cvrl:phase} from the prepared rule set</li>
 * <li><b>D9</b> detections carry {@code cvrl:line}/{@code cvrl:col} when known; the XPath stays in the message</li>
 * <li><b>D10</b> per-report timestamps are the serialization time — the true execution time needs
 * {@code ICTActionExecution} (not implemented yet)</li>
 * <li><b>D11</b> a detection carries no {@code code} that merely restates the action — {@code creator/@name} already
 * does; codes with information of their own (rule ids, outcome markers) stay</li>
 * <li><b>D12</b> scenario detection omits the severity attribute where it carries no information (every match is an
 * info) — it is optional in XVRL. Errors keep it: dropping it there would leave the detection "unspecified" while the
 * digest counts an error. Open consistency question across all steps</li>
 * <li><b>D13</b> scenario detections carry {@code cvrl:scenario-id} plus a {@code location} pointing into the scenario
 * configuration; the selected scenario is embedded in full as a second message</li>
 * <li><b>D14</b> messages that belong to the same detection are identified by {@code xml:id}
 * ({@code parse-document-hash}, {@code parse-document-content}, {@code select-scenario-content}) so consumers never
 * depend on their order</li>
 * </ul>
 *
 * @author Andreas Schmitz
 */
public final class CvrlWriter {

    /** XVRL namespace — CVRL is a profile of XVRL, the report must validate against it. */
    public static final String NS_XVRL = "http://www.xproc.org/ns/xvrl";

    /** D1: draft namespace for the CVRL extension attributes. */
    public static final String NS_CVRL = "urn:conformatron:cvrl:draft";

    /** {@code xml:id} of the message carrying the document hash. */
    public static final String ID_DOCUMENT_HASH = "parse-document-hash";

    /** {@code xml:id} of the message carrying the source document. */
    public static final String ID_DOCUMENT_CONTENT = "parse-document-content";

    /** {@code xml:id} of the message carrying the selected scenario. */
    public static final String ID_SCENARIO_CONTENT = "select-scenario-content";

    /** {@code cvrl:encoding}: embedded as an XML fragment, readable and processable. */
    public static final String ENCODING_DOM = "dom";

    /** {@code cvrl:encoding}: embedded base64-encoded, byte-faithful for any encoding and syntax. */
    public static final String ENCODING_BASE64 = "base64";

    private static final String MIME_TYPE_XML = "application/xml";

    private static final String MIME_TYPE_OCTET_STREAM = "application/octet-stream";

    /**
     * The results of one canonical pipeline run. Fields from the cancellation point onwards are {@code null} — the
     * report then contains only the executed steps and {@code cvrl:status="CANCELLED"} (partial CVRL, ADR-004).
     */
    public record PipelineResults(ParseXmlResult parse, DetectScenariosResult detect, SelectScenarioAction.SelectScenarioResult select,
            RetrieveArtifactsAction.RetrieveArtifactsResult retrieve, PrepareRulesAction.PrepareRulesResult prepare,
            ApplyRulesAction.ApplyRulesActionResult apply, ComputeConformanceAction.ComputeConformanceActionResult conformance) {

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
    public void write(final String documentName, final PipelineResults results, final OutputStream out) throws IOException {
        if (results == null || results.parse() == null) {
            throw new IllegalArgumentException("results with at least the parse step are required");
        }
        try {
            final XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, StandardCharsets.UTF_8.name());
            writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            newline(writer, 0);
            writer.writeStartElement("reports");
            writer.writeDefaultNamespace(NS_XVRL);
            writer.writeNamespace("cvrl", NS_CVRL);
            // D5: overall verdict + run status on the root
            writer.writeAttribute(NS_CVRL, "conformant", String.valueOf(results.isConformant()));
            writer.writeAttribute(NS_CVRL, "status", results.isCompleted() ? "COMPLETED" : "CANCELLED");

            writeRootMetadata(writer, documentName);
            writeStepReport(writer, CTActionType.PARSE_DOCUMENT, results.parse().getDetectionList(), null,
                    results.parse().getParsedSource());
            if (results.detect() != null) {
                writeStepReport(writer, CTActionType.DETECT_SCENARIOS, results.detect().detections(), null, null);
            }
            if (results.select() != null) {
                writeStepReport(writer, CTActionType.SELECT_SCENARIO, results.select().detections(), null, null);
            }
            if (results.retrieve() != null) {
                writeStepReport(writer, CTActionType.RETRIEVE_ARTIFACTS, results.retrieve().detections(), null, null);
            }
            if (results.prepare() != null) {
                writeStepReport(writer, CTActionType.PREPARE_RULES, results.prepare().detections(), null, null);
            }
            if (results.apply() != null) {
                // D3: one report per rule set execution, in scenario order
                for (final Map.Entry<CTPreparedRuleSet, CTDetectionList> entry : results.apply().result().getResultsByRuleSet()
                        .entrySet()) {
                    writeStepReport(writer, CTActionType.APPLY_RULES, entry.getValue(), entry.getKey(), null);
                }
            }
            if (results.conformance() != null) {
                writeStepReport(writer, CTActionType.COMPUTE_CONFORMANCE, results.conformance().detections(), null, null);
            }
            newline(writer, 0);
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
        } catch (final XMLStreamException e) {
            throw new IllegalStateException("Can not serialize CVRL report for " + documentName, e);
        }
    }

    private void writeRootMetadata(final XMLStreamWriter writer, final String documentName) throws XMLStreamException {
        newline(writer, 1);
        writer.writeStartElement(NS_XVRL, "metadata");
        newline(writer, 2);
        // open point: make runs distinguishable beyond the timestamp
        writer.writeComment(" TODO: ggf. UUID pro Validierungslauf ergaenzen ");
        newline(writer, 2);
        writer.writeStartElement(NS_XVRL, "timestamp");
        writer.writeCharacters(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString());
        writer.writeEndElement();
        newline(writer, 2);
        writer.writeEmptyElement(NS_XVRL, "validator");
        writer.writeAttribute("name", this.validatorName);
        writer.writeAttribute("version", this.validatorVersion);
        newline(writer, 2);
        // D6: reference only — hash and parsed payload are output of the parse step
        writer.writeEmptyElement(NS_XVRL, "document");
        writer.writeAttribute("href", documentName);
        newline(writer, 1);
        writer.writeEndElement();
    }

    private void writeStepReport(final XMLStreamWriter writer, final CTActionType action, final CTDetectionList detections,
            final CTPreparedRuleSet ruleSet, final CTParsedValidationSource parseEvidence) throws XMLStreamException, IOException {
        newline(writer, 1);
        writer.writeStartElement(NS_XVRL, "report");
        newline(writer, 2);
        writer.writeStartElement(NS_XVRL, "metadata");
        newline(writer, 3);
        if (action == CTActionType.PARSE_DOCUMENT) {
            writer.writeComment(" Offene Frage: how to deal with reports from other validator software ");
            newline(writer, 3);
        }
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
            writer.writeAttribute("language", ruleSet.getEngineType().getStandard() == CTValidationStandard.XSD ? "XSD" : "Schematron");
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
            writeDetection(writer, action, detection, parseEvidence);
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

    private static void writeDetection(final XMLStreamWriter writer, final CTActionType action, final CTDetection detection,
            final CTParsedValidationSource parseEvidence) throws XMLStreamException, IOException {
        newline(writer, 2);
        writer.writeStartElement(NS_XVRL, "detection");
        // D12: scenario detection omits the severity that says nothing — an error is of course still reported
        final boolean omitSeverity = action == CTActionType.DETECT_SCENARIOS && detection.getSeverity() == CTStandardSeverity.NONE;
        if (!omitSeverity) {
            writer.writeAttribute("severity", xvrlSeverity(detection.getSeverity()));
        }
        // D11: no code that merely restates the action — the creator name in the metadata already carries it
        if (!isRedundantCode(action, detection.getCode())) {
            writer.writeAttribute("code", detection.getCode());
        }
        // D9: line/col as extension attributes when known; the XPath location stays in the message
        if (detection.getLocation().getLineNumber() > 0) {
            writer.writeAttribute(NS_CVRL, "line", String.valueOf(detection.getLocation().getLineNumber()));
        }
        if (detection.getLocation().getColumnNumber() > 0) {
            writer.writeAttribute(NS_CVRL, "col", String.valueOf(detection.getLocation().getColumnNumber()));
        }
        // customLevel: severity is the effective one; the declared severity stays auditable
        if (detection instanceof final Detection impl && impl.getOriginalSeverity() != null) {
            writer.writeAttribute(NS_CVRL, "original-severity", xvrlSeverity(impl.getOriginalSeverity()));
        }
        final ScenarioDetection scenario = detection instanceof final ScenarioDetection sd ? sd : null;
        if (scenario != null && scenario.getScenarioID() != null) {
            // D13: consumers that use the validator for more than plain validation need the scenario id everywhere
            writer.writeAttribute(NS_CVRL, "scenario-id", scenario.getScenarioID());
        }
        if (scenario != null && scenario.getConfigurationLocation() != null) {
            // the location points into the scenario configuration so the scenario can be looked up quickly
            newline(writer, 3);
            writer.writeEmptyElement(NS_XVRL, "location");
            writer.writeAttribute("xpath", scenario.getConfigurationLocation());
        }
        if (XmlDetection.CODE_DOCUMENT_PARSED.equals(detection.getCode()) && parseEvidence != null) {
            writeParseEvidence(writer, parseEvidence);
        } else {
            newline(writer, 3);
            writer.writeStartElement(NS_XVRL, "message");
            writer.writeCharacters(detection.getText().getDisplayTextLocaleIndependent());
            writer.writeEndElement();
            if (scenario != null && scenario.getConfiguration() != null) {
                writeScenarioEvidence(writer, scenario);
            }
        }
        newline(writer, 2);
        writer.writeEndElement();
    }

    /**
     * D11: a detection code that only repeats what {@code metadata/creator/@name} already says is dropped from the
     * report. Codes that carry information of their own — rule ids, outcome markers — always stay.
     */
    private static boolean isRedundantCode(final CTActionType action, final String code) {
        if (code == null) {
            return true;
        }
        return switch (action) {
            case PARSE_DOCUMENT -> XmlDetection.CODE_DOCUMENT_PARSED.equals(code);
            case DETECT_SCENARIOS -> DetectScenariosAction.CODE_SCENARIO_MATCHED.equals(code)
                    || DetectScenariosAction.CODE_SCENARIO_USER_SELECTED.equals(code);
            default -> false;
        };
    }

    /**
     * The selected scenario is embedded in its original form as a second message, so a report consumer sees exactly
     * which rules were applied. This is the <b>individual scenario</b>, not the scenario configuration file. Scenario
     * configurations are UTF-8 by definition, so no base64 detour is needed here.
     */
    private static void writeScenarioEvidence(final XMLStreamWriter writer, final ScenarioDetection scenario)
            throws XMLStreamException, IOException {
        newline(writer, 3);
        writer.writeStartElement(NS_XVRL, "message");
        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", ID_SCENARIO_CONTENT);
        writer.writeAttribute(NS_CVRL, "mime-type", MIME_TYPE_XML);
        writer.writeAttribute(NS_CVRL, "encoding", ENCODING_DOM);
        writer.writeAttribute(NS_CVRL, "source-encoding", StandardCharsets.UTF_8.name());
        embedXml(writer, new ByteArrayInputStream(ScenarioXml.toXmlBytes(scenario.getConfiguration())));
        writer.writeEndElement();
    }

    /**
     * The {@code document-parsed} detection carries two messages, each identified by its own {@code xml:id} so a
     * consumer never has to rely on their order — first the document hash (over the retained source bytes, algorithm as
     * {@code cvrl:algorithm}), then the source document itself. Separate elements so a streaming consumer can skip the
     * payload. The payload is only ever written for a successfully parsed document — failed content is not echoed
     * (injection safety).
     * <p>
     * <b>Embedding rule</b> ({@code cvrl:encoding}): an XML document declared as UTF-8 is embedded as a DOM fragment —
     * readable, and byte-faithful because the report itself is UTF-8. Any other encoding is embedded as {@code base64},
     * because serializing the fragment into the UTF-8 report would silently transcode it and lose the original XML
     * declaration. The declared encoding is always reported as {@code cvrl:source-encoding} (needed to write a base64
     * payload back out). Non-XML sources are always base64.
     * </p>
     */
    private static void writeParseEvidence(final XMLStreamWriter writer, final CTParsedValidationSource source)
            throws XMLStreamException, IOException {
        final CTReadResource resource = source.getSource().getReadResource();
        newline(writer, 3);
        writer.writeStartElement(NS_XVRL, "message");
        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", ID_DOCUMENT_HASH);
        writer.writeAttribute(NS_CVRL, "algorithm", resource.getHashAlgorithmName());
        writer.writeCharacters(HexFormat.of().formatHex(resource.getHashBytes()));
        writer.writeEndElement();

        final String sourceEncoding = sourceEncodingOf(source);
        final boolean asDom = isXml(source) && StandardCharsets.UTF_8.name().equalsIgnoreCase(sourceEncoding);
        newline(writer, 3);
        writer.writeStartElement(NS_XVRL, "message");
        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", ID_DOCUMENT_CONTENT);
        writer.writeAttribute(NS_CVRL, "mime-type", isXml(source) ? MIME_TYPE_XML : MIME_TYPE_OCTET_STREAM);
        writer.writeAttribute(NS_CVRL, "encoding", asDom ? ENCODING_DOM : ENCODING_BASE64);
        writer.writeAttribute(NS_CVRL, "source-encoding", sourceEncoding);
        if (asDom) {
            embedXml(writer, resource.getSourceStream());
        } else {
            embedBase64(writer, resource);
        }
        writer.writeEndElement();
    }

    /**
     * The encoding the source document declares. For XML the DOM knows it: {@code getXmlEncoding()} is the encoding
     * from the XML declaration, {@code getInputEncoding()} the one the parser actually used (set when the declaration
     * omits it). Without a parsed DOM the XML default applies.
     */
    private static String sourceEncodingOf(final CTParsedValidationSource source) {
        final Document dom = domOf(source);
        if (dom != null) {
            if (dom.getXmlEncoding() != null) {
                return dom.getXmlEncoding();
            }
            if (dom.getInputEncoding() != null) {
                return dom.getInputEncoding();
            }
        }
        return StandardCharsets.UTF_8.name();
    }

    private static boolean isXml(final CTParsedValidationSource source) {
        return domOf(source) != null;
    }

    private static Document domOf(final CTParsedValidationSource source) {
        return source instanceof final CTParsedValidationSourceXML xml ? xml.getAsDom() : null;
    }

    /** Writes the source bytes as base64 text content — byte-faithful for any encoding and any syntax. */
    private static void embedBase64(final XMLStreamWriter writer, final CTReadResource resource) throws XMLStreamException, IOException {
        try ( InputStream in = resource.getSourceStream() ) {
            writer.writeCharacters(Base64.getEncoder().encodeToString(in.readAllBytes()));
        }
    }

    /**
     * Streams the retained source bytes into the report as element content (flat copy, XML declaration and DTD
     * excluded). The copy is event-based so the embedded document keeps its own namespaces without re-serialization.
     */
    private static void embedXml(final XMLStreamWriter writer, final InputStream sourceStream) throws XMLStreamException, IOException {
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        final XMLStreamReader reader = inputFactory.createXMLStreamReader(sourceStream);
        try {
            while (reader.hasNext()) {
                copyEvent(writer, reader, reader.next());
            }
        } finally {
            reader.close();
        }
    }

    private static void copyEvent(final XMLStreamWriter writer, final XMLStreamReader reader, final int event) throws XMLStreamException {
        switch (event) {
            case XMLStreamConstants.START_ELEMENT -> {
                writer.writeStartElement(defaultIfNull(reader.getPrefix()), reader.getLocalName(), defaultIfNull(reader.getNamespaceURI()));
                for (int i = 0; i < reader.getNamespaceCount(); i++) {
                    writer.writeNamespace(defaultIfNull(reader.getNamespacePrefix(i)), defaultIfNull(reader.getNamespaceURI(i)));
                }
                for (int i = 0; i < reader.getAttributeCount(); i++) {
                    writer.writeAttribute(defaultIfNull(reader.getAttributePrefix(i)), defaultIfNull(reader.getAttributeNamespace(i)),
                            reader.getAttributeLocalName(i), reader.getAttributeValue(i));
                }
            }
            case XMLStreamConstants.END_ELEMENT -> writer.writeEndElement();
            case XMLStreamConstants.CHARACTERS, XMLStreamConstants.SPACE, XMLStreamConstants.CDATA -> writer
                    .writeCharacters(reader.getText());
            case XMLStreamConstants.COMMENT -> writer.writeComment(reader.getText());
            case XMLStreamConstants.PROCESSING_INSTRUCTION -> writer.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
            default -> {
                // START_DOCUMENT/END_DOCUMENT and DTD events are intentionally not copied
            }
        }
    }

    private static String defaultIfNull(final String value) {
        return value == null ? "" : value;
    }

    private static String severityId(final CTDetectionList detections) {
        return detections.getCount() == 0 ? "info" : xvrlSeverity(detections.getWorstSeverity());
    }

    /**
     * Maps the model severity onto the XVRL severity vocabulary ({@code info | warning | error | fatal-error |
     * unspecified}) — CVRL is an XVRL profile, so the report never emits raw model IDs (the API currently returns
     * non-XVRL IDs such as {@code warn} and {@code none}; flagged upstream).
     */
    private static String xvrlSeverity(final CTSeverity severity) {
        if (severity == CTStandardSeverity.ERROR) {
            return "error";
        }
        if (severity == CTStandardSeverity.WARNING) {
            return "warning";
        }
        if (severity == CTStandardSeverity.NONE) {
            return "info";
        }
        return "unspecified";
    }

    private static void newline(final XMLStreamWriter writer, final int indent) throws XMLStreamException {
        writer.writeCharacters("\n" + "  ".repeat(indent));
    }
}
