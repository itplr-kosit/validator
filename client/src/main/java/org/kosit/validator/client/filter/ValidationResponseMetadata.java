package org.kosit.validator.client.filter;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
public class ValidationResponseMetadata {

    private MediaType contentType;

    private int statusCode;

    public MediaType getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = MediaType.valueOf(contentType);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}