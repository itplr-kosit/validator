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

package org.kosit.validator.config;

import org.kosit.validator.api.Configuration;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.ResolvingMode;

import java.net.URI;
import java.util.Date;

import static org.kosit.validator.config.ConfigurationBuilder.*;

/**
 * @author Andreas Penski
 */
public class TestConfigurationFactory {

    public static ConfigurationBuilder createSimpleConfiguration() {
        return Configuration.create().name("Simple-API").author("me").description("test desc").date(new Date())
                .with(createScenario().description("awesome scenario")).with(fallback().name("default").source("report.xsl"))

                .resolvingMode(ResolvingMode.STRICT_RELATIVE).useRepository(Simple.REPOSITORY_URI);
    }

    public static ConfigurationLoader loadSimpleConfiguration() {
        return Configuration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI);
    }

    public static ScenarioBuilder createScenario() {
        return scenario("simple").validate(schema("Sample Schema").schemaLocation(URI.create("simple.xsd")))
                .validate(schematron("Sample Schematron").source(Simple.SCHEMATRON))
                .with(report("Report für eRechnung").source("report.xsl")).acceptWith("count(//test:rejected) = 0")
                .declareNamespace("xvrl", "http://www.xproc.org/ns/xvrl").declareNamespace("rpt", "http://validator.kosit.de/test-report")
                .declareNamespace("test", "http://validator.kosit.de/test-sample").match("/test:simple");
    }
}
