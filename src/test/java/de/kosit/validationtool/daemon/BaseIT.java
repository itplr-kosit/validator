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

package de.kosit.validationtool.daemon;

import org.junit.Before;

import io.restassured.RestAssured;

/**
 * Base for integration tests.
 * 
 * @author Andreas Penski
 */
public abstract class BaseIT {

    @Before
    public void setup() {
        final String port = System.getProperty("daemon.port");
        if (port != null) {
            RestAssured.port = Integer.valueOf(port);
        }
        final String baseHost = System.getProperty("daemon.host");
        if (baseHost != null) {
            RestAssured.baseURI = baseHost;
        }
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
