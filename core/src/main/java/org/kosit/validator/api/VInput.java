package org.kosit.validator.api;

import java.io.IOException;

import javax.xml.transform.Source;

/**
 * An input for the validator.
 *
 * @author apenski
 *
 * @deprecated Replaced by the conformatron-api handshake types: at the very beginning of the processing pipeline by
 *             {@link org.conformatron.api.model.source.CTValidationSource} and after parsing by
 *             {@link org.conformatron.api.model.source.CTParsedValidationSource}. Validator implementations live in
 *             {@code org.kosit.validator.impl.conformatron} ({@code ValidationSource}, {@code DomValidationSource},
 *             {@code XdmNodeValidationSource}). The digest handling ({@link #getHashCode()} /
 *             {@link #getDigestAlgorithm()}) is superseded by ADR-003: hash computation is its own concern (SHA-512,
 *             {@code SourceDigest}) and no longer configured on the source. Remaining usages mark the code paths still
 *             to be migrated.
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public interface VInput {

    /**
     * The name of the input for document identification
     * 
     * @return the name
     */
    String getName();

    /**
     * The hashcode for document identification
     * 
     * @return the computed hashcode
     */
    byte[] getHashCode();

    /**
     * The digest algorithm used for computing the {@link #getHashCode()}
     * 
     * @return the name of the digest algorith
     */
    String getDigestAlgorithm();

    /**
     * Creates a new {@link Source } for this input which carries the actual data
     * 
     * @return an open {@link Source}
     * @throws IOException on I/O while opening the source
     */
    Source getSource() throws IOException;

}
