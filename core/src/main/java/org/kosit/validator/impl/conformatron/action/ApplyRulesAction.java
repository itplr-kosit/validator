package org.kosit.validator.impl.conformatron.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.conformatron.api.model.action.CTAction;
import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.rule.CTApplyRulesResult;
import org.conformatron.api.model.rule.CTPreparedRuleSet;
import org.conformatron.api.model.source.CTParsedValidationSource;
import org.kosit.svrl.impl.SvrlConverter;
import org.kosit.validator.impl.conformatron.model.ApplyRulesResult;
import org.kosit.validator.impl.conformatron.model.Detection;
import org.kosit.validator.impl.conformatron.model.DetectionList;
import org.kosit.validator.impl.conformatron.model.DetectionLocation;
import org.kosit.validator.impl.conformatron.util.SvrlDetections;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Step 7 of the canonical pipeline, {@code APPLY_RULES} (see
 * {@code conformatron-api/doc/steps/step-07-apply-rules.md}): applies every prepared rule set to the document, in the
 * fixed order from step 6, and collects the findings per rule set.
 * <p>
 * Key distinction of the spec: <b>findings are a negative but valid result</b> — failed asserts and schema violations
 * do not fail the step. Only engine errors do ({@code rule-engine-error}, FATAL, fail-fast: remaining rule sets are
 * marked skipped, their key stays present in the result map).
 * </p>
 * <p>
 * Self-contained: rules are applied on the <b>retained immutable byte array</b> of the parsed source — no repository
 * access, no re-read of the original input. Schematron rule sets run their compiled XSLT and map the SVRL output to
 * detections ({@link SvrlDetections}: assert id → detection code); XSD rule sets run a JAXP validator whose violations
 * become {@code schema-violation} detections with line/column.
 * </p>
 *
 * @author Andreas Schmitz
 */
public class ApplyRulesAction implements CTAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyRulesAction.class);

    /** Detection code when a rule set was applied without findings (INFO, one per clean rule set). */
    public static final String CODE_RULES_APPLIED = "rules-applied";

    /** Detection code for an XSD violation (ERROR/WARNING, one per violation, with line/column). */
    public static final String CODE_SCHEMA_VIOLATION = "schema-violation";

    /** Detection code when the rule engine crashes for a rule set (FATAL, cancels the process). */
    public static final String CODE_RULE_ENGINE_ERROR = "rule-engine-error";

    /** Detection code for skipped executions (no rule sets, or a previous execution failed). */
    public static final String CODE_STEP_SKIPPED = "step-skipped";

    /**
     * Result of a single execution of this action.
     *
     * @param status success, failure (engine error, cancels the process) or skipped (no rule sets)
     * @param result the per-rule-set results (keys always present, also for failed/skipped executions); forwarded to
     *            steps 8 and 9 — partial on failure per spec
     * @param detections this execution's contribution to the report: all per-rule-set detections, flattened in
     *            execution order; never {@code null}
     */
    public record ApplyRulesActionResult(CTStepResult status, CTApplyRulesResult result, CTDetectionList detections) {

        public boolean isSuccess() {
            return this.status == CTStepResult.SUCCESS;
        }
    }

    @Override
    public String getName() {
        return CTActionType.APPLY_RULES.getName();
    }

    @Override
    public CTActionType getType() {
        return CTActionType.APPLY_RULES;
    }

    /**
     * Applies all prepared rule sets to the document, in order.
     *
     * @param parsedSource the parsed source from step 2 (rules run on its retained bytes)
     * @param ruleSets the prepared rule sets from step 6; an empty list skips the step
     * @return the result including the per-rule-set detections
     */
    public ApplyRulesActionResult execute(final CTParsedValidationSource parsedSource, final List<CTPreparedRuleSet> ruleSets) {
        if (parsedSource == null) {
            throw new IllegalArgumentException("parsedSource may not be null");
        }
        if (ruleSets == null) {
            throw new IllegalArgumentException("ruleSets may not be null");
        }
        final String documentName = parsedSource.getSource().getName();
        if (ruleSets.isEmpty()) {
            final CTDetection skipped = Detection.of(CTStandardSeverity.NONE, CODE_STEP_SKIPPED, DetectionLocation.of(documentName),
                    "No rule sets prepared (reason: no-rule-sets)");
            return new ApplyRulesActionResult(CTStepResult.SKIPPED, ApplyRulesResult.empty(parsedSource), DetectionList.of(skipped));
        }
        final LinkedHashMap<CTPreparedRuleSet, CTDetectionList> results = new LinkedHashMap<>();
        boolean failed = false;
        for (final CTPreparedRuleSet ruleSet : ruleSets) {
            if (failed) {
                // fail-fast per spec: executions after an engine failure are skipped, but keep their key
                results.put(ruleSet,
                        DetectionList.of(Detection.of(CTStandardSeverity.NONE, CODE_STEP_SKIPPED, DetectionLocation.of(documentName),
                                "Rule set '" + href(ruleSet) + "' skipped (reason: previous-execution-failed)")));
                continue;
            }
            final CTDetectionList detections = applyOne(parsedSource, ruleSet, documentName);
            results.put(ruleSet, detections);
            failed = detections.getAll().stream().anyMatch(d -> CODE_RULE_ENGINE_ERROR.equals(d.getCode()));
        }
        final List<CTDetection> all = new ArrayList<>();
        results.values().forEach(list -> all.addAll(list.getAll()));
        return new ApplyRulesActionResult(failed ? CTStepResult.FAILURE : CTStepResult.SUCCESS, new ApplyRulesResult(parsedSource, results),
                new DetectionList(all));
    }

    private CTDetectionList applyOne(final CTParsedValidationSource parsedSource, final CTPreparedRuleSet ruleSet,
            final String documentName) {
        try {
            final CTDetectionList findings = switch (ruleSet.getEngineType().getStandard()) {
                case SCHEMATRON -> applySchematron(parsedSource, ruleSet, documentName);
                case XSD -> applySchema(parsedSource, ruleSet, documentName);
                default -> throw new IllegalStateException(
                        "Unsupported engine type " + ruleSet.getEngineType().getID() + " for rule application");
            };
            if (findings.getCount() == 0) {
                return DetectionList.of(Detection.of(CTStandardSeverity.NONE, CODE_RULES_APPLIED, DetectionLocation.of(documentName),
                        "Rule set '" + href(ruleSet) + "' applied without findings"));
            }
            return findings;
        } catch (final SaxonApiException | IOException | RuntimeException e) {
            LOGGER.error("Rule engine error applying {}", href(ruleSet), e);
            return DetectionList.of(new Detection(CTStandardSeverity.ERROR, CODE_RULE_ENGINE_ERROR, DetectionLocation.of(documentName),
                    "Rule set '" + href(ruleSet) + "' could not be applied: " + e.getMessage(), e));
        }
    }

    private CTDetectionList applySchematron(final CTParsedValidationSource parsedSource, final CTPreparedRuleSet ruleSet,
            final String documentName) throws SaxonApiException, IOException {
        final XsltExecutable executable = (XsltExecutable) ruleSet.getCompiledArtifact().getCompilation();
        final XsltTransformer transformer = executable.load();
        final XdmDestination destination = new XdmDestination();
        transformer.setDestination(destination);
        // apply on the retained immutable byte array — no re-read of the original source
        transformer.setSource(parsedSource.getSource().getReadResource().getAsSource(documentName));
        transformer.transform();
        final SchematronOutputType svrl = new SvrlConverter()
                .readXml(new DOMSource(NodeOverNodeInfo.wrap(destination.getXdmNode().getUnderlyingNode()).getOwnerDocument()));
        return SvrlDetections.toDetections(svrl, documentName);
    }

    private static CTDetectionList applySchema(final CTParsedValidationSource parsedSource, final CTPreparedRuleSet ruleSet,
            final String documentName) throws IOException {
        final Schema schema = (Schema) ruleSet.getCompiledArtifact().getCompilation();
        final Validator validator = schema.newValidator();
        secure(validator);
        final List<CTDetection> violations = new ArrayList<>();
        validator.setErrorHandler(new CollectingSchemaErrorHandler(violations, documentName));
        try {
            validator.validate(parsedSource.getSource().getReadResource().getAsSource(documentName));
        } catch (final SAXException e) {
            // a fatal violation aborts JAXP validation; it was already collected by the handler
            if (violations.isEmpty()) {
                throw new IllegalStateException("Schema validation aborted: " + e.getMessage(), e);
            }
        }
        return new DetectionList(violations);
    }

    private static void secure(final Validator validator) {
        try {
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (final SAXException e) {
            LOGGER.warn("Validator implementation does not support secure processing properties", e);
        }
    }

    private static String href(final CTPreparedRuleSet ruleSet) {
        return ruleSet.getArtifactReference().getValidationArtifactReference().toString();
    }

    /** Collects XSD violations as {@code schema-violation} detections with line/column. */
    private static final class CollectingSchemaErrorHandler implements ErrorHandler {

        private final List<CTDetection> violations;

        private final String documentName;

        CollectingSchemaErrorHandler(final List<CTDetection> violations, final String documentName) {
            this.violations = violations;
            this.documentName = documentName;
        }

        @Override
        public void warning(final SAXParseException exception) {
            add(CTStandardSeverity.WARNING, exception);
        }

        @Override
        public void error(final SAXParseException exception) {
            add(CTStandardSeverity.ERROR, exception);
        }

        @Override
        public void fatalError(final SAXParseException exception) {
            add(CTStandardSeverity.ERROR, exception);
        }

        private void add(final CTStandardSeverity severity, final SAXParseException exception) {
            this.violations.add(new Detection(severity, CODE_SCHEMA_VIOLATION,
                    new DetectionLocation(this.documentName, exception.getLineNumber(), exception.getColumnNumber()),
                    exception.getMessage(), exception));
        }
    }
}
