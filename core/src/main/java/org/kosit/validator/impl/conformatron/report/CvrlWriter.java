package org.kosit.validator.impl.conformatron.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.stream.StreamResult;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTDetectionLocation;
import org.conformatron.api.model.detection.CTSeverity;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.conformatron.api.model.source.CTParsedValidationSourceXML;
import org.conformatron.api.model.source.CTReadResource;
import org.conformatron.api.model.validation.CTValidationStandard;
import org.kosit.base.xml.SchemaResolver;
import org.kosit.base.xml.XmlHelper;
import org.kosit.jaxb.AbstractJaxbConverter;
import org.kosit.validator.impl.conformatron.action.ApplyRulesAction;
import org.kosit.validator.impl.conformatron.action.ComputeConformanceAction;
import org.kosit.validator.impl.conformatron.action.DecisionRecommendationAction;
import org.kosit.validator.impl.conformatron.action.PrepareRulesAction;
import org.kosit.validator.impl.conformatron.action.RetrieveArtifactsAction;
import org.kosit.validator.impl.conformatron.action.SelectScenarioAction;
import org.kosit.validator.impl.conformatron.action.detectscen.DetectScenariosResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.XmlDetection;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.model.PreparedRuleSet;
import org.kosit.validator.impl.conformatron.model.SubjectDetection;
import org.kosit.xvrl.impl.XvrlConverter;
import org.kosit.xvrl.jaxb.ObjectFactory;
import org.kosit.xvrl.jaxb.XvrlReportsType;
import org.kosit.xvrl.model.XvrlContext;
import org.kosit.xvrl.model.XvrlCreator;
import org.kosit.xvrl.model.XvrlDetection;
import org.kosit.xvrl.model.XvrlDigest;
import org.kosit.xvrl.model.XvrlDocument;
import org.kosit.xvrl.model.XvrlLocation;
import org.kosit.xvrl.model.XvrlMessage;
import org.kosit.xvrl.model.XvrlMetadata;
import org.kosit.xvrl.model.XvrlReport;
import org.kosit.xvrl.model.XvrlReports;
import org.kosit.xvrl.model.XvrlSchema;
import org.kosit.xvrl.model.XvrlSeverity;
import org.kosit.xvrl.model.XvrlSupplemental;
import org.kosit.xvrl.model.XvrlTimestamp;
import org.kosit.xvrl.model.XvrlValidator;
import org.kosit.xvrl.model.XvrlValidity;
import org.kosit.xvrl.model.XvrlWorst;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Serializes a canonical pipeline run (steps 2–8) to a CVRL report — an XVRL profile per
 * {@code conformance-validation-report-spec.md}. The report is assembled as an {@link XvrlReports} object model and
 * marshalled through the {@code xvrl} module, so the XVRL vocabulary (element and attribute names, the digest, the
 * severity and validity value spaces) comes from that model and only the CVRL extensions are named here. Every
 * opinionated default is tagged {@code D<n>} and listed as an explicit decision point.
 *
 * <ul>
 * <li><b>D1</b> extension namespace: {@code urn:conformatron:cvrl:draft} (spec placeholder was
 * {@code xmlns:cvrl="my"})</li>
 * <li><b>D2</b> canonical creator names come from {@code CTActionType} (the spec's {@code document-loader} etc. are
 * treated as outdated)</li>
 * <li><b>D3</b> one {@code <report>} per step execution; APPLY_RULES emits one report <b>per rule set</b> (sequential,
 * not nested)</li>
 * <li><b>D4</b> the digest carries {@code valid}, {@code worst}, {@code error-count}, {@code warning-count},
 * {@code error-codes} (distinct). No {@code fatal-error-count} — the severity model has no separate fatal band, so it
 * would only duplicate {@code error-count}</li>
 * <li><b>D5</b> root carries {@code cvrl:conformant} and {@code cvrl:status} (COMPLETED | CANCELLED); a cancelled run
 * still serializes — partial CVRL per ADR-004</li>
 * <li><b>D6</b> verbosity: full only. Document by reference in the root metadata; hash and parsed document are
 * <b>output</b> of the parse step and travel with the {@code document-parsed} detection — the hash as context, the
 * document as a message ({@code cvrl:mime-type}, {@code cvrl:encoding}). The payload is only written on parse success —
 * failed content is never echoed (injection safety)</li>
 * <li><b>D7</b> scenario identity travels as detections (no {@code metadata/document} scenario embedding)</li>
 * <li><b>D8</b> APPLY_RULES reports carry {@code <schema href schematypens>} plus {@code cvrl:phase} from the prepared
 * rule set; the engine identity sits on PREPARE_RULES instead, see D15</li>
 * <li><b>D9</b> everything positional lives in the XVRL {@code location} element — {@code xpath} for the node a rule
 * finding applies to, {@code line}/{@code column} for a schema violation, {@code href} for the subject the detection is
 * about. Nothing positional is written into the message text</li>
 * <li><b>D10</b> per-report timestamps are the serialization time — the true execution time needs
 * {@code CTActionExecution} (not implemented yet)</li>
 * <li><b>D11</b> a detection carries no {@code code} that merely restates the action — {@code creator/@name} already
 * does; codes with information of their own (rule ids, outcome markers) stay</li>
 * <li><b>D12</b> scenario detection omits the severity attribute where it carries no information (every match is an
 * info) — it is optional in XVRL. Errors keep it: dropping it there would leave the detection "unspecified" while the
 * digest counts an error</li>
 * <li><b>D13</b> a detection about an identified subject (scenario, artifact, conformance target) names it as
 * {@code cvrl:scenario-id} / {@code cvrl:artifact-id} / {@code cvrl:target-id}, adds what is known about it
 * ({@code cvrl:artifact-type}, {@code cvrl:conformance}) as attributes, and locates it. The selected scenario is
 * additionally embedded in full as a second message</li>
 * <li><b>D14</b> messages that belong to the same detection are identified by {@code xml:id}
 * ({@code parse-document-content}, {@code select-scenario-content}) so consumers never depend on their order</li>
 * <li><b>D15</b> the engine that transpiled/compiled the rules is reported on PREPARE_RULES as the XVRL standard
 * element {@code <validator name version/>} — it is a property of that step, not of rule application</li>
 * <li><b>D16</b> what a detection is about goes into {@code context}: {@code location} plus {@code cvrl:hash}, the
 * latter proving which bytes were validated and which rule-set version ran</li>
 * <li><b>D17</b> messages stay short; full stack traces go to {@code supplemental role="java-trace"} — the XVRL
 * {@code role} attribute, now that the xvrl module carries it</li>
 * <li><b>D18</b> the digest omits what says nothing: zero counts, and {@code worst} while everything is valid or a
 * single detection already carries its severity</li>
 * </ul>
 *
 * @author Andreas Schmitz
 */
public final class CvrlWriter {

    /** XVRL namespace — CVRL is a profile of XVRL, the report must validate against it. */
    public static final String NS_XVRL = XvrlConverter.NS_URI;

    /** D1: draft namespace for the CVRL extension attributes. */
    public static final String NS_CVRL = "urn:conformatron:cvrl:draft";

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

    /** {@code schematypens} of an XML Schema rule set. */
    public static final String SCHEMATYPENS_XSD = "http://www.w3.org/2001/XMLSchema";

    /** {@code schematypens} of a Schematron rule set (ISO Schematron). */
    public static final String SCHEMATYPENS_SCHEMATRON = "http://purl.oclc.org/dsdl/schematron";

    /** {@code role} of the supplemental carrying a Java stack trace. */
    public static final String ROLE_JAVA_TRACE = "java-trace";

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    /** Name reported for the XSLT processor that runs the prepared rules. */
    private static final String XSLT_PROCESSOR_NAME = "Saxon";

    private static final QName ATTR_CONFORMANT = cvrl("conformant");

    private static final QName ATTR_STATUS = cvrl("status");

    private static final QName ATTR_PHASE = cvrl("phase");

    private static final QName ATTR_ORIGINAL_SEVERITY = cvrl("original-severity");

    private static final QName ATTR_MIME_TYPE = cvrl("mime-type");

    private static final QName ATTR_ENCODING = cvrl("encoding");

    private static final QName ATTR_SOURCE_ENCODING = cvrl("source-encoding");

    /**
     * The results of one canonical pipeline run. Fields from the cancellation point onwards are {@code null} — the
     * report then contains only the executed steps and {@code cvrl:status="CANCELLED"} (partial CVRL, ADR-004).
     */
    public record PipelineResults(ParseXmlResult parse, DetectScenariosResult detect, SelectScenarioAction.SelectScenarioResult select,
            RetrieveArtifactsAction.RetrieveArtifactsResult retrieve, PrepareRulesAction.PrepareRulesResult prepare,
            ApplyRulesAction.ApplyRulesActionResult apply, ComputeConformanceAction.ComputeConformanceActionResult conformance,
            DecisionRecommendationAction.DecisionRecommendationResult decision) {

        /**
         * Assembles the run from the results of steps 2–8 and lets step 9 decide it. Step 9 always runs (step-09 spec),
         * also for a cancelled run — so a run without a decision cannot be assembled from step results.
         */
        public PipelineResults(final ParseXmlResult parse, final DetectScenariosResult detect,
                final SelectScenarioAction.SelectScenarioResult select, final RetrieveArtifactsAction.RetrieveArtifactsResult retrieve,
                final PrepareRulesAction.PrepareRulesResult prepare, final ApplyRulesAction.ApplyRulesActionResult apply,
                final ComputeConformanceAction.ComputeConformanceActionResult conformance) {
            this(parse, detect, select, retrieve, prepare, apply, conformance,
                    decide(parse, detect, select, retrieve, prepare, apply, conformance));
        }

        private static DecisionRecommendationAction.DecisionRecommendationResult decide(final ParseXmlResult parse,
                final DetectScenariosResult detect, final SelectScenarioAction.SelectScenarioResult select,
                final RetrieveArtifactsAction.RetrieveArtifactsResult retrieve, final PrepareRulesAction.PrepareRulesResult prepare,
                final ApplyRulesAction.ApplyRulesActionResult apply,
                final ComputeConformanceAction.ComputeConformanceActionResult conformance) {
            final DecisionRecommendationAction action = new DecisionRecommendationAction();
            if (conformance != null) {
                return action.execute(conformance.result());
            }
            final String resourceId = parse != null && parse.getParsedSource() != null ? parse.getParsedSource().getSource().getName()
                    : null;
            // the first step without a successor is the one that cancelled the run
            if (parse != null && !parse.isSuccess()) {
                return action.executeCancelled(CTActionType.PARSE_DOCUMENT, parse.getDetectionList(), resourceId);
            }
            if (detect != null && !detect.isSuccess()) {
                return action.executeCancelled(CTActionType.DETECT_SCENARIOS, detect.detections(), resourceId);
            }
            if (select != null && !select.isSuccess()) {
                return action.executeCancelled(CTActionType.SELECT_SCENARIO, select.detections(), resourceId);
            }
            if (retrieve != null && !retrieve.isSuccess()) {
                return action.executeCancelled(CTActionType.RETRIEVE_ARTIFACTS, retrieve.detections(), resourceId);
            }
            if (prepare != null && !prepare.isSuccess()) {
                return action.executeCancelled(CTActionType.PREPARE_RULES, prepare.detections(), resourceId);
            }
            if (apply != null && !apply.isSuccess()) {
                return action.executeCancelled(CTActionType.APPLY_RULES, apply.detections(), resourceId);
            }
            // steps 2–7 succeeded but step 8 is missing: the run stopped without a failing step
            return action.executeCancelled(CTActionType.COMPUTE_CONFORMANCE, DetectionList.empty(), resourceId);
        }

        /** Whether the run reached step 8; the decision of step 9 exists in every case. */
        public boolean isCompleted() {
            return this.conformance != null;
        }

        public boolean isConformant() {
            return isCompleted() && !this.conformance.result().hasNonConformantTarget();
        }
    }

    /**
     * Marshals the XVRL object model with the CVRL prefix declared. {@link XvrlConverter} fixes its prefix map
     * privately, so the profile brings its own converter over the same JAXB context and schema.
     */
    private static final class CvrlConverter extends AbstractJaxbConverter<XvrlReportsType> {

        CvrlConverter() {
            super(XvrlConverter.JAXB_CTX, XvrlReportsType.class, new ObjectFactory()::createReports);
            withSchema(SchemaResolver.createParsedSchema(XvrlConverter.class.getResource(XvrlConverter.XVRL_XSD_PATH)));
            withNamespacePrefixMap(Map.of(NS_XVRL, "", NS_CVRL, "cvrl"));
        }
    }

    private final String validatorName;

    private final String validatorVersion;

    private final CvrlConverter converter = new CvrlConverter();

    /** Owner document for the DOM nodes the profile adds ({@code cvrl:hash}); never serialized itself. */
    private final Document factory = XmlHelper.createSafeDocumentBuilder().newDocument();

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
        // the Result path is the one that marshals directly and therefore honours the converter's formatted output;
        // the stream and writer paths go through an XMLStreamWriter, which JAXB does not indent
        this.converter.writeXml(XvrlJaxbCreatorBridge.toJaxb(build(documentName, results)), new StreamResult(out));
    }

    /**
     * Assembles the pipeline run as an XVRL object model — the CVRL profile before serialization.
     *
     * @param documentName reference to the document under test
     * @param results the pipeline results; {@code parse} must not be {@code null}
     * @return the report model
     */
    public XvrlReports build(final String documentName, final PipelineResults results) throws IOException {
        if (results == null || results.parse() == null) {
            throw new IllegalArgumentException("results with at least the parse step are required");
        }
        final XvrlReports.Builder reports = XvrlReports.builder()
                // D5: overall verdict + run status on the root
                .otherAttribute(ATTR_CONFORMANT, String.valueOf(results.isConformant()))
                .otherAttribute(ATTR_STATUS, results.isCompleted() ? "COMPLETED" : "CANCELLED")
                // D6: reference only — hash and parsed payload are output of the parse step
                .metadata(XvrlMetadata.builder().addTimestamp(now())
                        .addValidator(XvrlValidator.builder(this.validatorName).version(this.validatorVersion))
                        .addDocument(XvrlDocument.builder(documentName)));

        reports.addReport(
                stepReport(CTActionType.PARSE_DOCUMENT, results.parse().getDetectionList(), null, results.parse().getParsedSource(), null));
        if (results.detect() != null) {
            reports.addReport(stepReport(CTActionType.DETECT_SCENARIOS, results.detect().detections(), null, null, null));
        }
        if (results.select() != null) {
            reports.addReport(stepReport(CTActionType.SELECT_SCENARIO, results.select().detections(), null, null, null));
        }
        if (results.retrieve() != null) {
            reports.addReport(stepReport(CTActionType.RETRIEVE_ARTIFACTS, results.retrieve().detections(), null, null, null));
        }
        if (results.prepare() != null) {
            reports.addReport(
                    stepReport(CTActionType.PREPARE_RULES, results.prepare().detections(), null, null, results.prepare().ruleSets()));
        }
        if (results.apply() != null) {
            // D3: one report per rule set execution, in scenario order
            for (final Map.Entry<CTPreparedRuleSet, CTDetectionList> entry : results.apply().result().getResultsByRuleSet().entrySet()) {
                reports.addReport(stepReport(CTActionType.APPLY_RULES, entry.getValue(), entry.getKey(), null, null));
            }
        }
        if (results.conformance() != null) {
            reports.addReport(stepReport(CTActionType.COMPUTE_CONFORMANCE, results.conformance().detections(), null, null, null));
        }
        if (results.decision() != null) {
            // step 9 is the terminal step and runs for cancelled runs too — it is what makes every report end in a
            // verdict
            reports.addReport(stepReport(CTActionType.DECISION_RECOMMENDATION, results.decision().detections(), null, null, null));
        }
        return reports.build();
    }

    private XvrlReport.Builder stepReport(final CTActionType action, final CTDetectionList detections, final CTPreparedRuleSet ruleSet,
            final CTParsedValidationSource parseEvidence, final List<CTPreparedRuleSet> engines) throws IOException {
        // D2: the canonical action names are normative for <creator @name>; D10: serialization time, not execution time
        final XvrlMetadata.Builder metadata = XvrlMetadata.builder().addCreator(XvrlCreator.builder(action.getName())).addTimestamp(now());
        if (action == CTActionType.PREPARE_RULES && engines != null) {
            // D15: which engine compiled the rules is a property of this step, and XVRL has a standard element for it
            for (final String transpiler : transpilers(engines)) {
                metadata.addValidator(XvrlValidator.builder(transpiler));
            }
            for (final String engine : engineIdentities(engines)) {
                metadata.addValidator(
                        XvrlValidator.builder(engine.substring(0, engine.indexOf('/'))).version(engine.substring(engine.indexOf('/') + 1)));
            }
        }
        if (ruleSet != null) {
            // D8: rule set identity on the APPLY_RULES report. schematypens names the rule language by its namespace;
            // XVRL has no "language" attribute
            metadata.addSchema(XvrlSchema.builder().href(ruleSet.getArtifactReference().getValidationArtifactReference().toString())
                    .schemaTypeNs(
                            ruleSet.getEngineType().getStandard() == CTValidationStandard.XSD ? SCHEMATYPENS_XSD : SCHEMATYPENS_SCHEMATRON)
                    .otherAttribute(ATTR_PHASE, ruleSet.getPhase()));
        }
        final XvrlReport.Builder report = XvrlReport.builder().metadata(metadata);
        for (final CTDetection detection : detections.getAll()) {
            report.addDetection(detection(action, detection, parseEvidence));
        }
        // XVRL puts the digest last: it summarises the detections above it
        return report.digest(digest(detections));
    }

    /**
     * D4: the digest is always present — a step without one would be indistinguishable from a step that did not run.
     * Only what carries information is written: counts of zero say nothing, and {@code worst} says nothing while
     * everything is valid or while there is a single detection that already states its own severity.
     */
    private static XvrlDigest.Builder digest(final CTDetectionList detections) {
        // the severity model has no separate fatal band, so a fatal-error-count would only duplicate error-count
        final long errors = detections.getCount(d -> d.getSeverity() == CTStandardSeverity.ERROR);
        final long warnings = detections.getCount(d -> d.getSeverity() == CTStandardSeverity.WARNING);
        final Set<String> errorCodes = new LinkedHashSet<>();
        detections.getAll().stream().filter(d -> d.getSeverity().isError()).forEach(d -> errorCodes.add(d.getCode()));
        final boolean valid = !detections.containsAtLeastOneError();
        final XvrlDigest.Builder digest = XvrlDigest.builder().valid(valid ? XvrlValidity.TRUE : XvrlValidity.FALSE);
        if (!valid && detections.getCount() > 1) {
            digest.worst(worst(detections.getWorstSeverity()));
        }
        if (errors > 0) {
            digest.errorCount(errors);
        }
        if (warnings > 0) {
            digest.warningCount(warnings);
        }
        return digest.addErrorCodes(errorCodes);
    }

    private XvrlDetection.Builder detection(final CTActionType action, final CTDetection detection, final CTParsedValidationSource parseEvidence)
            throws IOException {
        final XvrlDetection.Builder ret = XvrlDetection.builder();
        // D11/D12: severity and code appear exactly where they carry information — see carriesOwnIdentity
        if (carriesOwnIdentity(action, detection)) {
            ret.severity(xvrlSeverity(detection.getSeverity())).code(detection.getCode());
        }
        // customLevel: severity is the effective one; the declared severity stays auditable
        if (detection instanceof final Detection impl && impl.getOriginalSeverity() != null) {
            ret.otherAttribute(ATTR_ORIGINAL_SEVERITY, xvrlSeverity(impl.getOriginalSeverity()).getID());
        }
        final SubjectDetection subject = detection instanceof final SubjectDetection sd ? sd : null;
        if (subject != null) {
            // D13: what the detection is about, and what is known about it, as attributes rather than prose
            if (subject.getSubjectId() != null) {
                ret.otherAttribute(cvrl(subject.getSubjectAttribute()), subject.getSubjectId());
            }
            for (final Map.Entry<String, String> attribute : subject.getAttributes().entrySet()) {
                ret.otherAttribute(cvrl(attribute.getKey()), attribute.getValue());
            }
        }
        final XvrlContext.Builder context = context(detection, subject, parseEvidence);
        if (context != null) {
            ret.addContext(context);
        }
        if (XmlDetection.CODE_DOCUMENT_PARSED.equals(detection.getCode()) && parseEvidence != null) {
            ret.addMessage(parseEvidence(parseEvidence));
        } else {
            ret.addMessage(detection.getText().getDisplayTextLocaleIndependent());
            if (subject != null && subject.getEmbeddedXml() != null) {
                ret.addMessage(subjectEvidence(subject));
            }
        }
        // D17: the message stays short and readable; the full technical trace goes to supplemental
        if (detection.getLinkedException() != null) {
            final StringWriter trace = new StringWriter();
            try ( PrintWriter out = new PrintWriter(trace) ) {
                detection.getLinkedException().printStackTrace(out);
            }
            ret.addSupplemental(XvrlSupplemental.builder(trace.toString()).role(ROLE_JAVA_TRACE));
        }
        return ret;
    }

    /**
     * D9/D16: what a detection is <i>about</i> goes into {@code context}: where it applies or can be looked up
     * ({@code location}) and the fingerprint of the thing it concerns ({@code cvrl:hash}). Both are structure rather
     * than prose, so a consumer can navigate and verify without parsing message text.
     *
     * @return the context, or {@code null} when there is nothing to put into it
     */
    private XvrlContext.Builder context(final CTDetection detection, final SubjectDetection subject,
            final CTParsedValidationSource parseEvidence) {
        final boolean documentHash = XmlDetection.CODE_DOCUMENT_PARSED.equals(detection.getCode()) && parseEvidence != null;
        final boolean subjectHash = subject != null && subject.getHashValue() != null;
        if (!hasLocation(detection, subject) && !documentHash && !subjectHash) {
            return null;
        }
        final XvrlContext.Builder context = XvrlContext.builder();
        if (hasLocation(detection, subject)) {
            context.location(location(detection, subject));
        }
        if (documentHash) {
            final CTReadResource resource = parseEvidence.getSource().getReadResource();
            context.addContent(hash(resource.getHashAlgorithmName(), HexFormat.of().formatHex(resource.getHashBytes())));
        }
        if (subjectHash) {
            context.addContent(hash(subject.getHashAlgorithm(), subject.getHashValue()));
        }
        return context;
    }

    /** {@code <cvrl:hash cvrl:algorithm="…">hex</cvrl:hash>} — the one CVRL element, so it is built as a DOM node. */
    private Element hash(final String algorithm, final String value) {
        final Element hash = this.factory.createElementNS(NS_CVRL, "cvrl:hash");
        hash.setAttributeNS(NS_CVRL, "cvrl:algorithm", algorithm);
        hash.setTextContent(value);
        return hash;
    }

    private static boolean hasLocation(final CTDetection detection, final SubjectDetection subject) {
        final CTDetectionLocation location = detection.getLocation();
        final boolean positional = xpathOf(location) != null || location.hasLineNumber() || location.hasColumnNumber();
        return positional || (subject != null && (subject.getSubjectLocation() != null || subject.getSecondaryLocation() != null));
    }

    private static String xpathOf(final CTDetectionLocation location) {
        return location instanceof final DetectionLocation impl && impl.hasXPath() ? impl.getXPath() : null;
    }

    private static XvrlLocation.Builder location(final CTDetection detection, final SubjectDetection subject) {
        final CTDetectionLocation location = detection.getLocation();
        final String xpath = xpathOf(location);
        final XvrlLocation.Builder ret = XvrlLocation.builder().xpath(xpath);
        if (location.hasLineNumber()) {
            ret.line(location.getLineNumber());
        }
        if (location.hasColumnNumber()) {
            ret.column(location.getColumnNumber());
        }
        if (subject == null) {
            return ret;
        }
        // the subject location only needs its own attribute when the positional one has not already used it
        if (subject.getSubjectLocation() != null && !(xpath != null && subject.getLocationKind() == SubjectDetection.LocationKind.XPATH)) {
            if (subject.getLocationKind() == SubjectDetection.LocationKind.XPATH) {
                ret.xpath(subject.getSubjectLocation());
            } else {
                ret.href(subject.getSubjectLocation());
            }
        }
        if (subject.getSecondaryLocation() != null) {
            // the containing file, next to the pointer inside it — both are needed to look a scenario up
            ret.href(subject.getSecondaryLocation());
        }
        for (final Map.Entry<String, String> attribute : subject.getLocationAttributes().entrySet()) {
            ret.otherAttribute(cvrl(attribute.getKey()), attribute.getValue());
        }
        return ret;
    }

    /**
     * The Schematron transpilers that produced the executables ({@code schxslt}, {@code iso-schematron}). Reported
     * without a version because we do not know theirs — the version that follows belongs to the XSLT processor.
     */
    private static Set<String> transpilers(final List<CTPreparedRuleSet> ruleSets) {
        final Set<String> ret = new LinkedHashSet<>();
        for (final CTPreparedRuleSet ruleSet : ruleSets) {
            if (ruleSet instanceof final PreparedRuleSet impl && impl.getTranspilerId() != null) {
                ret.add(impl.getTranspilerId());
            }
        }
        return ret;
    }

    /**
     * Versions of the XSLT processor the rule sets were prepared with. The recorded engine version is the processor's,
     * so the element names the processor — labelling it with the rule language would attach Saxon's version to
     * something that does not have one.
     */
    private static Set<String> engineIdentities(final List<CTPreparedRuleSet> ruleSets) {
        final Set<String> ret = new LinkedHashSet<>();
        for (final CTPreparedRuleSet ruleSet : ruleSets) {
            if (ruleSet.getEngineVersion() != null) {
                ret.add(XSLT_PROCESSOR_NAME + "/" + ruleSet.getEngineVersion());
            }
        }
        return ret;
    }

    /**
     * D11/D12: whether a detection says something of its own beyond "this step ran". Two kinds do:
     * <ul>
     * <li>anything in the error band — the code names the failure and the digest lists it for triage;</li>
     * <li>a rule finding from APPLY_RULES — the code is the rule id, which stays even when a scenario override
     * downgraded the finding to information.</li>
     * </ul>
     * Everything else is a step-status statement: the creator name in the metadata already says which step ran, and a
     * severity of "info" on it would suggest a finding where there is none. Those get neither attribute.
     */
    private static boolean carriesOwnIdentity(final CTActionType action, final CTDetection detection) {
        if (detection.getSeverity().isError() || detection.getSeverity() == CTStandardSeverity.WARNING) {
            return true;
        }
        return action == CTActionType.APPLY_RULES && !ApplyRulesAction.CODE_RULES_APPLIED.equals(detection.getCode())
                && !ApplyRulesAction.CODE_STEP_SKIPPED.equals(detection.getCode());
    }

    /**
     * The subject's own XML, embedded as a second message so a consumer sees exactly what was applied — today the
     * selected scenario, which means the <b>individual scenario</b>, not the configuration file it came from. Scenario
     * configurations are UTF-8 by definition, so no base64 detour is needed here.
     */
    private XvrlMessage.Builder subjectEvidence(final SubjectDetection subject) throws IOException {
        return XvrlMessage.builder().id(ID_SCENARIO_CONTENT).otherAttribute(ATTR_MIME_TYPE, MIME_TYPE_XML)
                .otherAttribute(ATTR_ENCODING, ENCODING_DOM)
                .addContent(parse(new ByteArrayInputStream(subject.getEmbeddedXml())).getDocumentElement());
    }

    /**
     * The {@code document-parsed} detection embeds the source document as a message identified by its own
     * {@code xml:id}; the document hash sits in the detection's context. The payload is only ever written for a
     * successfully parsed document — failed content is not echoed (injection safety).
     * <p>
     * <b>Embedding rule</b> ({@code cvrl:encoding}): an XML document declared as UTF-8 is embedded as a DOM fragment —
     * readable, and in the report's own encoding. Any other encoding is embedded as {@code base64}, because serializing
     * the fragment into the UTF-8 report would silently transcode it and lose the original XML declaration. The
     * declared encoding is then reported as {@code cvrl:source-encoding} (needed to write a base64 payload back out).
     * Non-XML sources are always base64.
     * </p>
     */
    private static XvrlMessage.Builder parseEvidence(final CTParsedValidationSource source) throws IOException {
        final CTReadResource resource = source.getSource().getReadResource();
        final Document dom = domOf(source);
        final String sourceEncoding = sourceEncodingOf(dom);
        final boolean asDom = dom != null && StandardCharsets.UTF_8.name().equalsIgnoreCase(sourceEncoding);
        final XvrlMessage.Builder message = XvrlMessage.builder().id(ID_DOCUMENT_CONTENT)
                .otherAttribute(ATTR_MIME_TYPE, dom != null ? MIME_TYPE_XML : MIME_TYPE_OCTET_STREAM)
                .otherAttribute(ATTR_ENCODING, asDom ? ENCODING_DOM : ENCODING_BASE64);
        if (asDom) {
            return message.addContent(dom.getDocumentElement());
        }
        // only a base64 payload needs the source encoding — writing the document back out requires it, whereas a DOM
        // fragment is by construction in the report's own encoding
        message.otherAttribute(ATTR_SOURCE_ENCODING, sourceEncoding);
        try ( InputStream in = resource.getSourceStream() ) {
            return message.addContent(Base64.getEncoder().encodeToString(in.readAllBytes()));
        }
    }

    /**
     * The encoding the source document declares. For XML the DOM knows it: {@code getXmlEncoding()} is the encoding
     * from the XML declaration, {@code getInputEncoding()} the one the parser actually used (set when the declaration
     * omits it). Without a parsed DOM the XML default applies.
     */
    private static String sourceEncodingOf(final Document dom) {
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

    private static Document domOf(final CTParsedValidationSource source) {
        return source instanceof final CTParsedValidationSourceXML xml ? xml.getAsDom() : null;
    }

    private static Document parse(final InputStream xml) throws IOException {
        try {
            final DocumentBuilder builder = XmlHelper.createSafeDocumentBuilder();
            return builder.parse(xml);
        } catch (final SAXException e) {
            throw new IOException("Embedded XML is not well-formed", e);
        }
    }

    private static XvrlTimestamp.Builder now() {
        return XvrlTimestamp.builder(OffsetDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    /**
     * Formats a timestamp as {@code xs:dateTime}. {@link OffsetDateTime#toString()} leaves the seconds out when they
     * happen to be zero, which is valid ISO-8601 but not a valid {@code xs:dateTime} — so roughly one report in sixty
     * would fail schema validation, at random.
     *
     * @param moment the moment to format
     * @return the timestamp with seconds always present
     */
    static String timestamp(final OffsetDateTime moment) {
        return moment.truncatedTo(ChronoUnit.SECONDS).format(TIMESTAMP_FORMAT);
    }

    private static XvrlWorst worst(final CTSeverity severity) {
        return switch (xvrlSeverity(severity)) {
            case ERROR -> XvrlWorst.ERROR;
            case WARNING -> XvrlWorst.WARNING;
            case INFO -> XvrlWorst.INFO;
            case FATAL_ERROR -> XvrlWorst.FATAL_ERROR;
            case UNSPECIFIED -> XvrlWorst.UNSPECIFIED;
        };
    }

    /**
     * Maps the model severity onto the XVRL severity vocabulary. Deliberately not {@link XvrlDetection#translate}: that
     * maps {@code NONE} to {@code unspecified}, whereas a downgraded rule finding (customLevel) is an
     * <i>information</i> — a consumer must be able to tell it from a detection whose severity is simply unknown.
     */
    private static XvrlSeverity xvrlSeverity(final CTSeverity severity) {
        if (severity == CTStandardSeverity.ERROR) {
            return XvrlSeverity.ERROR;
        }
        if (severity == CTStandardSeverity.WARNING) {
            return XvrlSeverity.WARNING;
        }
        if (severity == CTStandardSeverity.NONE) {
            return XvrlSeverity.INFO;
        }
        return XvrlSeverity.UNSPECIFIED;
    }

    private static QName cvrl(final String localName) {
        return new QName(NS_CVRL, localName, "cvrl");
    }

    /** Keeps the JAXB mapping in one place and out of the profile's own code. */
    private static final class XvrlJaxbCreatorBridge {

        private XvrlJaxbCreatorBridge() {
            // static utility
        }

        static XvrlReportsType toJaxb(final XvrlReports reports) {
            return org.kosit.xvrl.jaxb.XvrlJaxbCreator.createReportsType(reports);
        }
    }
}
