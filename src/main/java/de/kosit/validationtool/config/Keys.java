/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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

package de.kosit.validationtool.config;

/**
 * Defines some keys used for supplying additional parameters internally.
 * 
 * @author Andreas Penski
 */
public final class Keys {

    /**
     * The actual scenarios file location as used with {@link ConfigurationLoader}.
     */
    public static final String SCENARIOS_FILE = "scenarios_file";

    /**
     * The actual scenarios configuration represented as serializable tree. This either loaded from file or build
     * manually via {@link ConfigurationBuilder}
     */
    public static final String SCENARIO_DEFINITION = "scenario_definition";

    private Keys() {
        // hide
    }
}
