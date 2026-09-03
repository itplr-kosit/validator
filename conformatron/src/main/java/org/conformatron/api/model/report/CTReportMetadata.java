package org.conformatron.api.model.report;

import java.time.OffsetDateTime;
import java.util.Map;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Metadata for a single validation report, identifying which action produced it, which tools were used, and what was
 * validated against.
 * <p>
 * Maps to the XVRL {@code <report>/<metadata>} element group.
 * </p>
 * <h3>XVRL Mapping</h3>
 * 
 * <pre>
 * &lt;report&gt;
 *   &lt;metadata&gt;
 *     &lt;timestamp&gt;2025-03-10T14:00:00Z&lt;/timestamp&gt;
 *     &lt;creator name="rule-validator" version="1.0" /&gt;
 *     &lt;validator name="iso-schematron" version="4.0.0" /&gt;
 *     &lt;schema href="path/to/rules.xsl" language="Schematron" version="1.5" /&gt;
 *   &lt;/metadata&gt;
 *   ...
 * &lt;/report&gt;
 * </pre>
 *
 * @author Andreas Schmitz
 */
public interface CTReportMetadata {
    // -- Action identification --

    /**
     * @return The canonical name of the validation action that produced this report. Maps to XVRL
     *         {@code <creator @name>}. Examples: "scenario-matcher", "structure-validator", "rule-validator",
     *         "schematron-transpiler", "decision-recommender"
     */
    @NonNull
    @Nonempty
    String getActionName();

    /**
     * @return An optional human-readable title for this report. Maps to XVRL {@code <title>}. Example:
     *         "schematron-execution"
     */
    @Nullable
    String getTitle();

    /**
     * @return The timestamp (UTC) when this specific action was executed. Maps to XVRL {@code <timestamp>}. May be
     *         {@code null} if timing is not recorded for this action.
     */
    @Nullable
    OffsetDateTime getTimestampUTC();

    // -- Tool identification --

    /**
     * @return The name of the validation engine/tool used by this action. Maps to XVRL {@code <validator @name>} or
     *         {@code <creator @name>}. Example: "iso-schematron", "jaxp-schema-validator", "schxslt2" May be
     *         {@code null} if the action does not use a specific engine.
     */
    @Nullable
    String getEngineName();

    /**
     * @return The version of the validation engine/tool. Maps to XVRL {@code <validator @version>}. May be {@code null}
     *         if unknown or not applicable.
     */
    @Nullable
    String getEngineVersion();

    // -- Schema / artifact reference --

    /**
     * @return A reference (URI or path) to the validation artifact used by this action. Maps to XVRL
     *         {@code <schema @href>}. Example: "resources/xrechnung/1.2.2/xsl/XRechnung-UBL-validation-Invoice.xsl" May
     *         be {@code null} if the action does not use a schema/artifact (e.g. decision-making).
     */
    @Nullable
    String getSchemaReference();

    /**
     * @return The language/type of the validation artifact. Maps to XVRL {@code <schema @language>}. Example: "XSD",
     *         "Schematron", "XSLT" May be {@code null} if not applicable.
     */
    @Nullable
    String getSchemaLanguage();

    /**
     * @return The version of the validation artifact (e.g. Schematron version). Maps to XVRL {@code <schema @version>}.
     *         May be {@code null} if unknown or not applicable.
     */
    @Nullable
    String getSchemaVersion();

    /**
     * @return An optional reference to the schema type namespace. Maps to XVRL {@code <schema @schematypens>}. Example:
     *         "http://xml.ascc.net/schematron/" May be {@code null} if not applicable.
     */
    @Nullable
    String getSchemaTypeNamespace();

    // -- Document reference (Gap 1) --

    /**
     * @return An optional reference to a document relevant to this action. For scenario-matching this is the scenario
     *         definition; for parse/load this may contain the document content as-is. Maps to XVRL
     *         {@code <metadata>/<document>}. May be {@code null} if not applicable.
     */
    @Nullable
    String getDocumentReference();

    // -- Invocation details (Gap 6) --

    /**
     * @return An optional description of how this action was invoked. Maps to XVRL {@code <creator>/<invocation>}.
     *         Example: "java -jar validator.jar --input doc.xml" May be {@code null} if not recorded.
     */
    @Nullable
    String getInvocationDetails();

    // -- Supplemental parameters (Gap 2) --

    /**
     * @return Optional supplemental key-value parameters for this action. Maps to XVRL
     *         {@code <metadata>/<supplemental>}. For Schematron validation this may contain the phase
     *         ({@code "phase" -> "#all"}) or other engine-specific parameters. Returns an empty map if no supplemental
     *         parameters exist.
     */
    @NonNull
    Map<String, String> getSupplementalParameters();
}
