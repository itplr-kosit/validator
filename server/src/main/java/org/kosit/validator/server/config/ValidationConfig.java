package org.kosit.validator.server.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@ConfigMapping(prefix = "validator")
public interface ValidationConfig {

    @WithName("framework-version")
    String frameworkVersion();

    Logging logging();

    interface Logging {

        /**
         * Should the console logging output JSON?
         */
        @WithDefault("false")
        boolean json();

        @WithName("debug")
        @WithDefault("false")
        boolean debugLog();
    }

    List<ScenarioBundle> scenarios();

    /**
     * The ScenarioBundle represents a configuration tuple of paths for the {@link org.kosit.validator.impl.Scenario}
     * XML file and the associated {@link org.kosit.validator.impl.ContentRepository}.
     */
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