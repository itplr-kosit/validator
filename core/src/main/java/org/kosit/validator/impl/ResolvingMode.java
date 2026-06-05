package org.kosit.validator.impl;

import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.kosit.validator.impl.xml.RemoteResolvingStrategy;
import org.kosit.validator.impl.xml.StrictLocalResolvingStrategy;
import org.kosit.validator.impl.xml.StrictRelativeResolvingStrategy;

/**
 * Defines how artefacts are resolved internally.
 * 
 * @author Andreas Penski
 */
public enum ResolvingMode {

    STRICT_RELATIVE(new StrictRelativeResolvingStrategy()), STRICT_LOCAL(new StrictLocalResolvingStrategy()), ALLOW_REMOTE(
            new RemoteResolvingStrategy()), CUSTOM(null);

    private final ResolvingConfigurationStrategy strategy;

    private ResolvingMode(final ResolvingConfigurationStrategy strategy) {
        this.strategy = strategy;
    }

    public ResolvingConfigurationStrategy getStrategy() {
        return this.strategy;
    }
}
