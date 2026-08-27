//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v4.0.9 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2026.08.27 um 07:05:47 PM CEST
//

package org.kosit.validator.model;

/**
 * Dient der eindeutigen Identifikation des geprüften Dokuments anhand seines Hashwertes, der durch eine
 * Dokumentenreferenz ergänzt werden kann.
 * 
 */
public class DocumentIdentificationType {

    /**
     * Angaben zum Hashwert des geprüften Dokuments.
     * 
     */
    protected DocumentHash documentHash;

    /**
     * Eine von der prüfenden Organisationseinheit festgelegte, möglichst eindeutige Referenz des geprüften Dokuments.
     * 
     */
    protected String documentReference;

    /**
     * Angaben zum Hashwert des geprüften Dokuments.
     * 
     * @return possible object is {@link DocumentHash }
     * 
     */
    public DocumentHash getDocumentHash() {
        return documentHash;
    }

    /**
     * Legt den Wert der documentHash-Eigenschaft fest.
     * 
     * @param value allowed object is {@link DocumentHash }
     * 
     * @see #getDocumentHash()
     */
    public void setDocumentHash(final DocumentHash value) {
        this.documentHash = value;
    }

    /**
     * Eine von der prüfenden Organisationseinheit festgelegte, möglichst eindeutige Referenz des geprüften Dokuments.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getDocumentReference() {
        return documentReference;
    }

    /**
     * Legt den Wert der documentReference-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String }
     * 
     * @see #getDocumentReference()
     */
    public void setDocumentReference(final String value) {
        this.documentReference = value;
    }

}
