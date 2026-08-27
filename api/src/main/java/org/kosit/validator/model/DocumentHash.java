package org.kosit.validator.model;

public class DocumentHash {

    /**
     * Benennung eines Algorithmus zur Berechnung des Hashwerts.
     * 
     */
    protected String hashAlgorithm;

    /**
     * Der Hashwert des geprüften Dokuments bei Anwendung des bezeichneten Algorithmus.
     * 
     */
    protected byte[] hashValue;

    /**
     * Benennung eines Algorithmus zur Berechnung des Hashwerts.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    /**
     * Legt den Wert der hashAlgorithm-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String }
     * 
     * @see #getHashAlgorithm()
     */
    public void setHashAlgorithm(final String value) {
        this.hashAlgorithm = value;
    }

    /**
     * Der Hashwert des geprüften Dokuments bei Anwendung des bezeichneten Algorithmus.
     * 
     * @return possible object is byte[]
     */
    public byte[] getHashValue() {
        return hashValue;
    }

    /**
     * Legt den Wert der hashValue-Eigenschaft fest.
     * 
     * @param value allowed object is byte[]
     * @see #getHashValue()
     */
    public void setHashValue(final byte[] value) {
        this.hashValue = value;
    }

}