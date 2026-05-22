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

package de.kosit.validationtool.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Holds static information about this validator.
 *
 * @author Andreas Penski
 */
public class EngineInformation {

    private static final Properties PROPERTIES;

    static {
        PROPERTIES = new Properties();
        try ( final InputStream input = EngineInformation.class.getClassLoader().getResourceAsStream("app-info.properties") ) {
            if (input != null) {
                PROPERTIES.load(input);
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Can not engine information", e);
        }
    }

    private EngineInformation() {
        // hide
    }

    /**
     * Returns the version number of the validator.
     *
     * @return the version
     */
    public static String getVersion() {
        return PROPERTIES.getProperty("project_version");
    }

    /**
     * Returns the name of the engine.
     *
     * @return the name
     */
    public static String getName() {
        return PROPERTIES.getProperty("engine_name");
    }

    /**
     * Returns the version number of the framework in use. This is relevant for aligning scenario configuration and
     * validator versions.
     *
     * @return the framework version
     */
    public static String getFrameworkVersion() {
        return PROPERTIES.getProperty("framework_version");
    }

    /**
     * Returns the major version number of the framework in use.
     *
     * @return the major version number
     */
    public static String getFrameworkMajorVersion() {
        return getFrameworkVersion().substring(0, 1);
    }

    public static String getBuild() {
        return PROPERTIES.getProperty("build_number");
    }

    /**
     * Returns the namespace of the framework in use.
     *
     * @return the major version number
     */
    public static String getFrameworkNamespace() {
        return "http://www.xoev.de/de/validator/framework/" + getFrameworkMajorVersion() + "/createreportinput";
    }
}
