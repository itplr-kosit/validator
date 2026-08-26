package org.kosit.validator.impl;

import org.jspecify.annotations.Nullable;
import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.kosit.validator.xml.resolve.RemoteResolvingStrategy;
import org.kosit.validator.xml.resolve.StrictLocalResolvingStrategy;
import org.kosit.validator.xml.resolve.StrictRelativeResolvingStrategy;

/**
 * Defines how artefacts are resolved internally.
 * 
 * @author Andreas Penski
 */
public enum ResolvingMode {

    STRICT_RELATIVE(new StrictRelativeResolvingStrategy()), STRICT_LOCAL(new StrictLocalResolvingStrategy()), ALLOW_REMOTE(
            new RemoteResolvingStrategy()), CUSTOM(null);

    private final ResolvingConfigurationStrategy strategy;

    private ResolvingMode(final @Nullable ResolvingConfigurationStrategy strategy) {
        this.strategy = strategy;
    }

    public @Nullable ResolvingConfigurationStrategy getStrategy() {
        return this.strategy;
    }
}
