package org.kosit.validator.impl.input;

/**
 * Internal interface used for lazy generation of the hashcode for document identification.
 * 
 * @see StreamHelper#wrapDigesting(LazyReadInput, InputStream, String) for details
 * @author Andreas Penski
 */
@Deprecated(since = "2.0.0", forRemoval = true)
interface LazyReadInput {

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * Sets a hashcode
     * 
     * @param digest the digest
     */
    void setHashCode(byte[] digest);

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * Determines whether a hashcode has been computed yet
     * 
     * @return true when computed
     */
    boolean isHashcodeComputed();

    @Deprecated(since = "2.0.0", forRemoval = true)
    /**
     * Setting the length of the {@link VInput}.
     * 
     * @param length the length
     */
    void setLength(long length);

}
