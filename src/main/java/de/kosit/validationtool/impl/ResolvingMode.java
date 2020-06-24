package de.kosit.validationtool.impl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import de.kosit.validationtool.api.ResolvingConfigurationStrategy;
import de.kosit.validationtool.impl.xml.RemoteResolvingStrategy;
import de.kosit.validationtool.impl.xml.StrictLocalResolvingStrategy;
import de.kosit.validationtool.impl.xml.StrictRelativeResolvingStrategy;

/**
 * Defines how artefacts are resolved internally.
 * 
 * @author Andreas Penski
 */
@RequiredArgsConstructor
public enum ResolvingMode {

    /**
     * Resolving using only the configured content repository. No furthing resolving allowed. This
     */
    STRICT_RELATIVE(new StrictRelativeResolvingStrategy()) {

    },

    STRICT_LOCAL(new StrictLocalResolvingStrategy()),

    ALLOW_REMOTE(new RemoteResolvingStrategy()),

    CUSTOM(null);

    @Getter
    private final ResolvingConfigurationStrategy strategy;

}
