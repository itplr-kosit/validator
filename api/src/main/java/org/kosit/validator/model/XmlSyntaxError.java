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
import org.kosit.validator.api.xmlerror.AbstractXmlSyntaxError;


/**
 * <p>Java-Klasse für XmlSyntaxError complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="XmlSyntaxError">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="message" type="{http://www.w3.org/2001/XMLSchema}normalizedString"/>
 *         <element name="severityCode" type="{http://www.xoev.de/de/validator/framework/1/model}XmlSyntaxErrorSeverity"/>
 *         <element name="rowNumber" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         <element name="columnNumber" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XmlSyntaxError", namespace = "http://www.xoev.de/de/validator/framework/1/model", propOrder = {
    "message",
    "severityCode",
    "rowNumber",
    "columnNumber"
})
public class XmlSyntaxError
    extends AbstractXmlSyntaxError
    implements Serializable
{

    private static final long serialVersionUID = -1L;
    @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/1/model", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    @XmlSchemaType(name = "normalizedString")
    protected String message;
    @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/1/model", required = true)
    @XmlSchemaType(name = "token")
    protected XmlSyntaxErrorSeverity severityCode;
    @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/1/model")
    protected Long rowNumber;
    @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/1/model")
    protected Long columnNumber;

    /**
     * Ruft den Wert der message-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Legt den Wert der message-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Ruft den Wert der severityCode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link XmlSyntaxErrorSeverity }
     *     
     */
    public XmlSyntaxErrorSeverity getSeverityCode() {
        return severityCode;
    }

    /**
     * Legt den Wert der severityCode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlSyntaxErrorSeverity }
     *     
     */
    public void setSeverityCode(XmlSyntaxErrorSeverity value) {
        this.severityCode = value;
    }

    /**
     * Ruft den Wert der rowNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getRowNumber() {
        return rowNumber;
    }

    /**
     * Legt den Wert der rowNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setRowNumber(Long value) {
        this.rowNumber = value;
    }

    /**
     * Ruft den Wert der columnNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getColumnNumber() {
        return columnNumber;
    }

    /**
     * Legt den Wert der columnNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setColumnNumber(Long value) {
        this.columnNumber = value;
    }

}
