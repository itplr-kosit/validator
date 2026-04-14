package org.kosit.validator.client.filter;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
public class ValidationRequestConfig {

    private MediaType acceptType = MediaType.APPLICATION_XML_TYPE; // Default

    public MediaType getAcceptType() {
        return acceptType;
    }

    public void setAcceptType(MediaType acceptType) {
        this.acceptType = acceptType;
    }
}