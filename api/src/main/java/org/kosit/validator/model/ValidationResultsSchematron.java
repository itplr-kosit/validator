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
import jakarta.xml.bind.annotation.XmlType;
import org.kosit.validator.scenario.v1.ResourceType;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;

/**
 * <p>
 * Java-Klasse für ValidationResultsSchematron complex type.
 * </p>
 * 
 * <p>
 * Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * </p>
 * 
 * <pre>
 * {@code
 * <complexType name="ValidationResultsSchematron">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element ref="{http://www.xoev.de/de/validator/framework/2/scenarios}resource"/>
 *         <element name="results">
 *           <complexType>
 *             <complexContent>
 *               <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 <sequence>
 *                   <element ref="{http://purl.oclc.org/dsdl/svrl}schematron-output"/>
 *                 </sequence>
 *               </restriction>
 *             </complexContent>
 *           </complexType>
 *         </element>
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
@XmlType(name = "ValidationResultsSchematron", namespace = "http://www.xoev.de/de/validator/framework/1/model",
         propOrder = { "resource", "results" })
public class ValidationResultsSchematron implements Serializable {

    private static final long serialVersionUID = -1L;

    @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/2/scenarios", required = true)
    protected ResourceType resource;

    @XmlElement(namespace = "http://www.xoev.de/de/validator/framework/1/model", required = true)
    protected ValidationResultsSchematron.Results results;

    /**
     * Ruft den Wert der resource-Eigenschaft ab.
     * 
     * @return possible object is {@link ResourceType }
     * 
     */
    public ResourceType getResource() {
        return resource;
    }

    /**
     * Legt den Wert der resource-Eigenschaft fest.
     * 
     * @param value allowed object is {@link ResourceType }
     * 
     */
    public void setResource(ResourceType value) {
        this.resource = value;
    }

    /**
     * Ruft den Wert der results-Eigenschaft ab.
     * 
     * @return possible object is {@link ValidationResultsSchematron.Results }
     * 
     */
    public ValidationResultsSchematron.Results getResults() {
        return results;
    }

    /**
     * Legt den Wert der results-Eigenschaft fest.
     * 
     * @param value allowed object is {@link ValidationResultsSchematron.Results }
     * 
     */
    public void setResults(ValidationResultsSchematron.Results value) {
        this.results = value;
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
     *         <element ref="{http://purl.oclc.org/dsdl/svrl}schematron-output"/>
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
    @XmlType(name = "", propOrder = { "schematronOutput" })
    public static class Results implements Serializable {

        private static final long serialVersionUID = -1L;

        @XmlElement(name = "schematron-output", namespace = "http://purl.oclc.org/dsdl/svrl", required = true)
        protected SchematronOutputType schematronOutput;

        /**
         * Ruft den Wert der schematronOutput-Eigenschaft ab.
         * 
         * @return possible object is {@link SchematronOutputType }
         * 
         */
        public SchematronOutputType getSchematronOutput() {
            return schematronOutput;
        }

        /**
         * Legt den Wert der schematronOutput-Eigenschaft fest.
         * 
         * @param value allowed object is {@link SchematronOutputType }
         * 
         */
        public void setSchematronOutput(SchematronOutputType value) {
            this.schematronOutput = value;
        }

    }

}
