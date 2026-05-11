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

/**
 * Holds static information about this validator.
 *
 * @author Andreas Penski
 */
public interface EngineInformation {

    /**
     * Returns the version number of the validator.
     *
     * @return the version
     */
    String getVersion();

    /**
     * Returns the name of the engine.
     *
     * @return the name
     */
    String getName();

    /**
     * Returns the version number of the framework used. This is relevant to align scenario configuration and validator
     * versions with each other.
     *
     * @return the framework version
     */
    String getFrameworkVersion();

    String getBuild();

}
