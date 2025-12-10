package org.kosit.validator.server.impl;

import io.quarkus.info.BuildInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.kosit.validator.impl.EngineInformation;

@ApplicationScoped
public class ServerEngineInformation implements EngineInformation {

    @Inject
    BuildInfo buildInfo;

    @ConfigProperty(name = "validator.framework-version")
    String frameworkVersion;

    @Override
    public String getName() {
        return buildInfo.artifact();
    }

    @Override
    public String getFrameworkVersion() {
        return frameworkVersion;
    }

    @Override
    public String getVersion() {
        return buildInfo.version();
    }

    @Override
    public String getBuild() {
        return buildInfo.version();
    }
}