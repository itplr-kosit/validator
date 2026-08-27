/*
 * Copyright 2017-2026  Koordinierungsstelle für IT-Standards (KoSIT)
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
package org.kosit.validator.impl.conformatron.util;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.namespace.QName;

import org.jspecify.annotations.NonNull;
import org.kosit.validator.scenario.v1.Scenario1ConversionService;
import org.kosit.validator.scenario.v1.ScenarioType;

import jakarta.xml.bind.JAXBElement;

/**
 * Serializes an individual scenario back to XML so the report can embed the scenario that was selected. Note the
 * distinction: this is the <b>single scenario</b>, not the scenario configuration file it came from.
 * <p>
 * Scenario configurations are UTF-8 by definition, so the embedded scenario never needs the base64 detour that applies
 * to arbitrary source documents.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class ScenarioXml {

    /** Namespace of the scenario configuration (framework 2). */
    public static final String NS_SCENARIOS = "http://www.xoev.de/de/validator/framework/2/scenarios";

    private static final QName SCENARIO_QNAME = new QName(NS_SCENARIOS, "scenario");

    private static final Scenario1ConversionService CONVERSION_SERVICE = new Scenario1ConversionService();

    private ScenarioXml() {
        // static utility
    }

    /**
     * Serializes the given scenario as a standalone XML document.
     *
     * @param configuration the scenario configuration to serialize
     * @return the scenario as UTF-8 encoded XML
     */
    public static byte @NonNull [] toXmlBytes(final @NonNull ScenarioType configuration) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        // the generated type carries no root element, so the element name is supplied explicitly
        CONVERSION_SERVICE.writeXml(new JAXBElement<>(SCENARIO_QNAME, ScenarioType.class, configuration), out);
        return out.toByteArray();
    }

    /**
     * Serializes the given scenario as a standalone XML document.
     *
     * @param configuration the scenario configuration to serialize
     * @return the scenario as XML text
     */
    public static @NonNull String toXml(final @NonNull ScenarioType configuration) {
        return new String(toXmlBytes(configuration), StandardCharsets.UTF_8);
    }
}
