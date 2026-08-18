package org.kosit.validator.impl.conformatron;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.detection.ECTSeverity;
import org.conformatron.api.model.detection.ICTDetection;
import org.conformatron.api.model.detection.ICTDetectionList;
import org.conformatron.api.model.rule.ICTPreparedRuleSet;
import org.conformatron.api.model.source.ICTParsedValidationSource;
import org.kosit.svrl.impl.SvrlConversionService;
import org.kosit.validator.api.Input;
import org.kosit.validator.api.ValidationEngine;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.ResolvingMode;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;
import org.oclc.purl.dsdl.svrl.SuccessfulReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * <b>Prototype</b>: validates a document directly against a single Schematron — no scenario configuration, no
 * repository setup, no report transformation required. The Schematron URI is the only configuration.
 * <p>
 * The engine is composed of canonical actions: {@link ParseDocumentAction} (step 2), {@link RetrieveArtifactsAction}
 * (step 5, with the Schematron's parent directory as the artifact repository), {@link PrepareRulesAction} (step 6) and
 * the rule application below (step 7). Scenario detection/selection (steps 3+4) is deliberately bypassed — the caller
 * fixes the rule set explicitly, which is the ad-hoc equivalent of a user-selected scenario.
 * </p>
 * <p>
 * Result semantics: {@code SUCCESS} means the rules were applied — findings (failed asserts) do <b>not</b> fail the
 * run; {@link AdHocValidationResult#isConformant()} tells whether the document satisfied the rules. Failures of the
 * composed steps are reported under their own canonical detection codes ({@code artifact-missing},
 * {@code rule-prepare-error}); a rule-engine crash is {@code rules-processing-error} — its own code rather than
 * masquerading as a failed assert (finding from the schxslt processing-error verification).
 * </p>
 *
 * @author Andreas Schmitz
 */
public class SchematronValidation implements ValidationEngine<SchematronValidation.AdHocValidationResult> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchematronValidation.class);

    /** Detection code for a violated {@code sch:assert}; the assert id is reported as field. */
    public static final String CODE_FAILED_ASSERT = "failed-assert";

    /** Detection code for a triggered {@code sch:report}. */
    public static final String CODE_SUCCESSFUL_REPORT = "successful-report";

    /** Detection code when the rule engine crashes while applying the rules (FATAL, fails the run). */
    public static final String CODE_RULES_PROCESSING_ERROR = "rules-processing-error";

    private final Processor processor;

    private final SvrlConversionService conversionService = new SvrlConversionService();

    /** The fixed Schematron of this engine instance; {@code null} if only the two-arg {@code validate} is used. */
    private final URI schematron;

    public SchematronValidation(final Processor processor) {
        this(processor, null);
    }

    /**
     * Creates an engine instance fixed to the given Schematron ({@link ValidationEngine} assembly: the rule set is a
     * construction concern).
     *
     * @param processor the Saxon processor
     * @param schematron URI of the Schematron file this engine validates against
     */
    public SchematronValidation(final Processor processor, final URI schematron) {
        if (processor == null) {
            throw new IllegalArgumentException("processor may not be null");
        }
        this.processor = processor;
        this.schematron = schematron;
    }

    /**
     * Validates the document against the Schematron fixed at construction time ({@link ValidationEngine} contract).
     *
     * @param input the document to validate
     * @return the result including all detections
     */
    @Override
    public AdHocValidationResult validate(final Input input) {
        if (this.schematron == null) {
            throw new IllegalStateException("No schematron configured for this engine instance");
        }
        return validate(input, this.schematron);
    }

    /**
     * Result of an ad-hoc schematron validation run.
     *
     * @param status success or failure of the run itself (failure = parse, preparation or processing error)
     * @param parsedSource the parsed document; may be {@code null} if the source could not be read
     * @param detections all findings and errors of the run; never {@code null}
     */
    public record AdHocValidationResult(ECTStepResult status, ICTParsedValidationSource parsedSource, ICTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == ECTStepResult.SUCCESS;
        }

        /**
         * @return {@code true} if the run succeeded and the document satisfies the rules (no ERROR or FATAL detections)
         */
        public boolean isConformant() {
            return isSuccess() && !this.detections.containsAtLeastOneError();
        }
    }

    /**
     * Validates the document against the given Schematron.
     *
     * @param document the document to validate
     * @param schematron URI of the Schematron file ({@code .sch}); relative resources of the Schematron are resolved
     *            against its parent directory
     * @return the result including all detections
     */
    public AdHocValidationResult validate(final Input document, final URI schematron) {
        if (schematron == null) {
            throw new IllegalArgumentException("schematron may not be null");
        }
        // step 2 (PARSE_DOCUMENT): reference action, retains bytes + hash
        final ParseDocumentAction.ParseDocumentResult parsed = new ParseDocumentAction().execute(document);
        if (!parsed.isSuccess()) {
            return new AdHocValidationResult(ECTStepResult.FAILURE, parsed.parsedSource(), parsed.detections());
        }
        final String documentName = parsed.parsedSource().getSource().getName();
        // the parent directory of the schematron is the artifact repository of this ad-hoc run
        final URI base = schematron.resolve(".");
        final ValidationArtifactReference reference = ValidationArtifactReference.of(base.relativize(schematron).toString());

        // step 5 (RETRIEVE_ARTIFACTS): resolve confined to that directory
        final RetrieveArtifactsAction.RetrieveArtifactsResult retrieved = new RetrieveArtifactsAction(base).execute(List.of(reference),
                documentName);
        if (!retrieved.isSuccess()) {
            return new AdHocValidationResult(ECTStepResult.FAILURE, parsed.parsedSource(), retrieved.detections());
        }
        // step 6 (PREPARE_RULES): transpile + compile
        final ContentRepository repository = new ContentRepository(this.processor, ResolvingMode.STRICT_RELATIVE.getStrategy(), base);
        final PrepareRulesAction.PrepareRulesResult prepared = new PrepareRulesAction(repository).execute(retrieved.artifacts(),
                documentName);
        if (!prepared.isSuccess()) {
            return new AdHocValidationResult(ECTStepResult.FAILURE, parsed.parsedSource(), prepared.detections());
        }
        // step 7 (APPLY_RULES)
        return applyRules(parsed.parsedSource(), prepared.ruleSets().get(0), schematron);
    }

    private AdHocValidationResult applyRules(final ICTParsedValidationSource parsedSource, final ICTPreparedRuleSet ruleSet,
            final URI schematron) {
        final String documentName = parsedSource.getSource().getName();
        try {
            final XsltExecutable executable = (XsltExecutable) ruleSet.getCompiledArtifact().getCompilation();
            final XsltTransformer transformer = executable.load();
            final XdmDestination destination = new XdmDestination();
            transformer.setDestination(destination);
            // rules are applied on the retained immutable byte array — no re-read of the original source
            transformer.setSource(new StreamSource(new ByteArrayInputStream(parsedSource.getSourceBytes()), documentName));
            transformer.transform();
            final SchematronOutput svrl = this.conversionService.readXml(
                    new DOMSource(NodeOverNodeInfo.wrap(destination.getXdmNode().getUnderlyingNode()).getOwnerDocument()),
                    SchematronOutput.class);
            return new AdHocValidationResult(ECTStepResult.SUCCESS, parsedSource, toDetections(svrl, documentName));
        } catch (final SaxonApiException e) {
            LOGGER.error("Error processing schematron {}", schematron, e);
            final ICTDetection detection = new Detection(ECTSeverity.FATAL_ERROR, CODE_RULES_PROCESSING_ERROR,
                    DetectionLocation.ofResource(documentName), "Error processing schematron '" + schematron + "': " + e.getMessage(), e);
            return new AdHocValidationResult(ECTStepResult.FAILURE, parsedSource, DetectionList.of(detection));
        }
    }

    private static ICTDetectionList toDetections(final SchematronOutput svrl, final String documentName) {
        final List<ICTDetection> detections = new ArrayList<>();
        for (final Object entry : svrl.getActivePatternAndFiredRuleAndFailedAssert()) {
            if (entry instanceof final FailedAssert failedAssert) {
                detections.add(Detection.of(severityOf(failedAssert.getRole()), CODE_FAILED_ASSERT,
                        DetectionLocation.ofResource(documentName),
                        message(failedAssert.getId(), failedAssert.getLocation(), textOf(failedAssert.getText()))));
            } else if (entry instanceof final SuccessfulReport report) {
                detections.add(Detection.of(severityOf(report.getRole()), CODE_SUCCESSFUL_REPORT,
                        DetectionLocation.ofResource(documentName),
                        message(report.getId(), report.getLocation(), textOf(report.getText()))));
            }
        }
        return new DetectionList(detections);
    }

    private static ECTSeverity severityOf(final String role) {
        if (role == null) {
            return ECTSeverity.ERROR;
        }
        return switch (role.toLowerCase()) {
            case "information", "info" -> ECTSeverity.INFO;
            case "warning", "warn" -> ECTSeverity.WARNING;
            case "fatal" -> ECTSeverity.FATAL_ERROR;
            default -> ECTSeverity.ERROR;
        };
    }

    private static String textOf(final org.oclc.purl.dsdl.svrl.Text text) {
        if (text == null) {
            return "";
        }
        final StringBuilder builder = new StringBuilder();
        text.getContent().forEach(c -> builder.append(String.valueOf(c).trim()));
        return builder.toString();
    }

    private static String message(final String id, final String location, final String text) {
        final StringBuilder builder = new StringBuilder();
        if (id != null) {
            builder.append('[').append(id).append("] ");
        }
        builder.append(text);
        if (location != null) {
            builder.append(" (at ").append(location).append(')');
        }
        return builder.toString();
    }
}
