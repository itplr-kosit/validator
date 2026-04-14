package org.kosit.validator.client.filter;

import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;

@Dependent
@Unremovable
public class ValidationRequestFilter implements ClientRequestFilter {

    @Inject
    ValidationRequestConfig config;

    @Override
    public void filter(ClientRequestContext requestContext) {
        requestContext.getHeaders().putSingle(HttpHeaders.ACCEPT, config.getAcceptType().toString());
    }
}