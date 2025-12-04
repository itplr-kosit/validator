package org.kosit.validator.server.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "validator")
public interface ValidationConfig {

    @WithName("debugOutput")
    boolean debugOutput();

    @WithName("debugLog")
    boolean debugLog();

    @WithName("logLevel")
    Level logLevel();

    List<ScenarioBundle> scenarios();

    interface ScenarioBundle {

        @WithName("scenarioPath")
        Path scenarioPath();

        @WithName("repositoryPath")
        @WithDefault("")
        Path repositoryPath();

        default Optional<Path> repositoryOpt() {
            return Optional.ofNullable(repositoryPath()).filter(p -> !p.toString().isBlank());
        }
    }
}