package org.conformatron.api.model.source;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Carries the parsed representation of the input document through the pipeline.
 * <p>
 * Produced by step 2 ({@code PARSE_DOCUMENT}) and forwarded unchanged to every subsequent step (steps 3–9). Steps must
 * treat this object as immutable.
 * </p>
 * <ul>
 * <li>The entire source document is retained as an <b>immutable byte array</b>.</li>
 * <li>A <b>SHA-512</b> hash is computed at parse time and carried here for integrity / audit.</li>
 * <li>The parsed representation ({@link #getParsedContent()}) is engine-specific (e.g. a Saxon {@code XdmNode}). Steps
 * that do not need it may ignore it.</li>
 * </ul>
 * <p>
 * <b>ADR note:</b> the use of an engine-specific type for {@link #getParsedContent()} is an open architectural
 * decision. Until resolved, the return type is {@code Object}; callers cast based on the
 * {@link CTValidationSource#getDetectedSyntax()} value.
 * </p>
 */
public interface CTParsedValidationSource {

    /**
     * @return The original validation source (name, detected syntax, completeness indicator).
     */
    @NonNull
    CTValidationSource getSource();

    /**
     * @return The engine-specific parsed representation of the document (e.g. Saxon {@code XdmNode} for XML).
     *         {@code null} if parsing failed. Callers must cast based on the detected syntax.
     * @see CTValidationSource#getDetectedSyntax()
     */
    @Nullable
    Object getParsedContent();

    /**
     * @return {@code true} if parsing succeeded and a parsed content object is available.
     */
    default boolean isParsed() {
        return getParsedContent() != null;
    }
}
