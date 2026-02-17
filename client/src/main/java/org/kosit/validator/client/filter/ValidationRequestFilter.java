package org.kosit.validator.client.filter;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationRequestFilter implements ClientRequestFilter {

    @Inject
    ValidationRequestConfig config;

    @Override
    public void filter(ClientRequestContext requestContext) {
        requestContext.getHeaders().putSingle(HttpHeaders.ACCEPT, config.getAcceptType().toString());
    }
}