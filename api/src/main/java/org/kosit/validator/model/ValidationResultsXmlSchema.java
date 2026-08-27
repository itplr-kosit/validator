//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v4.0.9 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2026.08.27 um 07:05:47 PM CEST
//

package org.kosit.validator.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import org.kosit.validator.api.xmlerror.XmlSyntaxError;
import org.kosit.validator.scenario.v1.ResourceType;


/**
 * <p>Java-Klasse für ValidationResultsXmlSchema complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="ValidationResultsXmlSchema">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element ref="{http://www.xoev.de/de/validator/framework/2/scenarios}resource" maxOccurs="unbounded"/>
 *         <element name="xmlSyntaxError" type="{http://www.xoev.de/de/validator/framework/1/model}XmlSyntaxError" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValidationResultsXmlSchema", namespace = "http://www.xoev.de/de/validator/framework/1/model", propOrder = {
    "resource",
    "xmlSyntaxError"
})
public class ValidationResultsXmlSchema
    implements Serializable
{

    private static final long serialVersionUID = -1L;
    @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/2/scenarios", required = true)
    protected List<ResourceType> resource;
    @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/1/model")
    protected List<XmlSyntaxError> xmlSyntaxError;

    /**
     * Gets the value of the resource property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore, any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the resource property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getResource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResourceType }
     * </p>
     * 
     * 
     * @return
     *     The value of the resource property.
     */
    public List<ResourceType> getResource() {
        if (resource == null) {
            resource = new ArrayList<>();
        }
        return this.resource;
    }

    /**
     * Gets the value of the xmlSyntaxError property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore, any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a {@code set} method for the xmlSyntaxError property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getXmlSyntaxError().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XmlSyntaxError }
     * </p>
     * 
     * 
     * @return
     *     The value of the xmlSyntaxError property.
     */
    public List<XmlSyntaxError> getXmlSyntaxError() {
        if (xmlSyntaxError == null) {
            xmlSyntaxError = new ArrayList<>();
        }
        return this.xmlSyntaxError;
    }

}
