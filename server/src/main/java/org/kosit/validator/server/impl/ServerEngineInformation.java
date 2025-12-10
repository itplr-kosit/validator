package org.kosit.validator.server.impl;

import io.quarkus.info.BuildInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kosit.validator.impl.EngineInformation;

@ApplicationScoped
public class ServerEngineInformation implements EngineInformation {

    @Inject
    BuildInfo buildInfo;

    @Override
    public String getName() {
        return buildInfo.artifact();
    }

    @Override
    public String getFrameworkVersion() {
        return "";
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