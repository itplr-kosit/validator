/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    STRICT_RELATIVE(new StrictRelativeResolvingStrategy()) {
    },
    STRICT_LOCAL(new StrictLocalResolvingStrategy()), ALLOW_REMOTE(new RemoteResolvingStrategy()), CUSTOM(null);

    private final ResolvingConfigurationStrategy strategy;

    private ResolvingMode(final ResolvingConfigurationStrategy strategy) {
        this.strategy = strategy;
    }

    public ResolvingConfigurationStrategy getStrategy() {
        return this.strategy;
    }
}
