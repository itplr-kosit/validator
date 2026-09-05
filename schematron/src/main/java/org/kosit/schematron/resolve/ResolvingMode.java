package org.kosit.schematron.resolve;

import org.jspecify.annotations.Nullable;

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
