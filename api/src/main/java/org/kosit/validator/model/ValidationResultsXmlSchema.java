package org.kosit.validator.model;

import java.util.ArrayList;
import java.util.List;

import org.kosit.base.error.SimpleError;
import org.kosit.validator.scenario.v2.ResourceType;

public class ValidationResultsXmlSchema {

    protected List<ResourceType> resource;

    protected List<SimpleError> xmlSyntaxError;

    /**
     * Gets the value of the resource property.
     * 
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore, any modification you make
     * to the returned list will be present inside the Jakarta XML Binding object. This is why there is not a
     * {@code set} method for the resource property.
     * </p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * 
     * <pre>
     * getResource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ResourceType }
     * </p>
     * 
     * 
     * @return The value of the resource property.
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
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore, any modification you make
     * to the returned list will be present inside the Jakarta XML Binding object. This is why there is not a
     * {@code set} method for the xmlSyntaxError property.
     * </p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * 
     * <pre>
     * getXmlSyntaxError().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list {@link SimpleError }
     * </p>
     * 
     * 
     * @return The value of the xmlSyntaxError property.
     */
    public List<SimpleError> getXmlSyntaxError() {
        if (xmlSyntaxError == null) {
            xmlSyntaxError = new ArrayList<>();
        }
        return this.xmlSyntaxError;
    }

}
