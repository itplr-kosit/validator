package org.kosit.validator.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@RegisterRestClient(configKey = "validator_yaml")
public interface ValidatorHealthClient {

    @GET
    @Path("/q/health/ready")
    Response checkReadiness();

}
