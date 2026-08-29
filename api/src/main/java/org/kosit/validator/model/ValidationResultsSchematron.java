package org.kosit.validator.model;

import org.kosit.validator.scenario.v2.ResourceType;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;

public class ValidationResultsSchematron {

    protected ResourceType resource;

    protected SchematronOutputType results;

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
    public void setResource(final ResourceType value) {
        this.resource = value;
    }

    /**
     * Ruft den Wert der results-Eigenschaft ab.
     * 
     * @return possible object is {@link ValidationResultsSchematron.Results }
     * 
     */
    public SchematronOutputType getResults() {
        return results;
    }

    /**
     * Legt den Wert der results-Eigenschaft fest.
     * 
     * @param value allowed object is {@link ValidationResultsSchematron.Results }
     * 
     */
    public void setResults(final SchematronOutputType value) {
        this.results = value;
    }

}
