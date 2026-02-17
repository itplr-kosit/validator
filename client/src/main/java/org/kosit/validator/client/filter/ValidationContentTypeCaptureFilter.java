package org.kosit.validator.client.filter;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationContentTypeCaptureFilter implements ClientResponseFilter {

    @Inject
    ValidationResponseMetadata responseMetadata;

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        String ct = responseContext.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        responseMetadata.setContentType(ct);
        responseMetadata.setStatusCode(responseContext.getStatus());
    }
}
