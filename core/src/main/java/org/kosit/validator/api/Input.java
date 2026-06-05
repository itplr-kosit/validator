package org.kosit.validator.api;

import java.io.IOException;

import javax.xml.transform.Source;

/**
 * An input for the validator.
 *
 * @author apenski
 */

public interface Input {

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
