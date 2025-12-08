package org.kosit.validator.server.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class ValidationServiceReadinessCheck implements HealthCheck {

    private final String NAME = "validation-service-readiness";

    @Inject
    ValidationService validationService;

    @Override
    public HealthCheckResponse call() {
        try {
            boolean ready = validationService.isReady();

            if (ready) {
                return HealthCheckResponse.named(NAME).up().withData("configurationCount", validationService.getConfigurationCount())
                        .build();
            } else {
                return HealthCheckResponse.named(NAME).down().withData("reason", "No configurations loaded")
                        .withData("configurationCount", validationService.getConfigurationCount()).build();
            }
        } catch (Exception e) {
            return HealthCheckResponse.named(NAME).down().withData("error", e.getClass().getName() + ": " + e.getMessage()).build();
        }
    }
}