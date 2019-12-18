package de.kosit.validationtool.impl.input;

import java.io.InputStream;

import de.kosit.validationtool.api.Input;

/**
 * Internal interface used for lazy generation of the hashcode for document identification.
 * 
 * @see StreamHelper#wrapDigesting(LazyReadInput, InputStream, String) for details
 * @author Andreas Penski
 */
interface LazyReadInput {

    /**
     * Sets a hashcode
     * 
     * @param digest the digest
     */
    void setHashCode(byte[] digest);

    /**
     * Determines whether a hashcode has been computed yet
     * 
     * @return true when computed
     */
    boolean isHashcodeComputed();

    /**
     * Setting the length of the {@link Input}.
     * 
     * @param length the length
     */
    void setLength(long length);

}
