//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v4.0.9 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2026.08.27 um 07:05:47 PM CEST
//

package org.kosit.validator.model;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Dient der eindeutigen Identifikation des geprüften Dokuments anhand seines Hashwertes, der durch eine
 * Dokumentenreferenz ergänzt werden kann.
 * 
 * <p>
 * Java-Klasse für DocumentIdentificationType complex type.
 * </p>
 * 
 * <p>
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * </p>
 * 
 * <pre>
 * {@code
 * <complexType name="DocumentIdentificationType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="documentHash">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element name="hashAlgorithm" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *                   <element name="hashValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
 *         <element name="documentReference" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DocumentIdentificationType", namespace = "http://www.xoev.de/de/validator/framework/1/model",
         propOrder = { "documentHash", "documentReference" })
public class DocumentIdentificationType implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * Angaben zum Hashwert des geprüften Dokuments.
     * 
     */
    @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/1/model", required = true)
    protected DocumentIdentificationType.DocumentHash documentHash;

    /**
     * Eine von der prüfenden Organisationseinheit festgelegte, möglichst eindeutige Referenz des geprüften Dokuments.
     * 
     */
    @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/1/model", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String documentReference;

    /**
     * Angaben zum Hashwert des geprüften Dokuments.
     * 
     * @return possible object is {@link DocumentIdentificationType.DocumentHash }
     * 
     */
    public DocumentIdentificationType.DocumentHash getDocumentHash() {
        return documentHash;
    }

    /**
     * Legt den Wert der documentHash-Eigenschaft fest.
     * 
     * @param value allowed object is {@link DocumentIdentificationType.DocumentHash }
     * 
     * @see #getDocumentHash()
     */
    public void setDocumentHash(DocumentIdentificationType.DocumentHash value) {
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
    public void setDocumentReference(String value) {
        this.documentReference = value;
    }

    /**
     * <p>
     * Java-Klasse für anonymous complex type.
     * </p>
     * 
     * <p>
     * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     * </p>
     * 
     * <pre>
     * {@code
     * <complexType>
     *   <complexContent>
     *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       <sequence>
     *         <element name="hashAlgorithm" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
     *         <element name="hashValue" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
     *       </sequence>
     *     </restriction>
     *   </complexContent>
     * </complexType>
     * }
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = { "hashAlgorithm", "hashValue" })
    public static class DocumentHash implements Serializable {

        private static final long serialVersionUID = -1L;

        /**
         * Benennung eines Algorithmus zur Berechnung des Hashwerts.
         * 
         */
        @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/1/model", required = true)
        @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
        @XmlSchemaType(name = "normalizedString")
        protected String hashAlgorithm;

        /**
         * Der Hashwert des geprüften Dokuments bei Anwendung des bezeichneten Algorithmus.
         * 
         */
        @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/1/model", required = true)
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
        public void setHashAlgorithm(String value) {
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
        public void setHashValue(byte[] value) {
            this.hashValue = value;
        }

    }

}
