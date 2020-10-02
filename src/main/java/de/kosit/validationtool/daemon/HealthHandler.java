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

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.EngineInformation;
import de.kosit.validationtool.model.daemon.ApplicationType;
import de.kosit.validationtool.model.daemon.HealthType;
import de.kosit.validationtool.model.daemon.MemoryType;

/**
 * Handler that implements a simple health check. Useful for monitoring the service.
 * 
 * @author Andreas Penski
 */
@Slf4j
@RequiredArgsConstructor
class HealthHandler extends BaseHandler {

    private final Configuration scenarios;

    private final ConversionService conversionService;

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        final HealthType health = createHealth();
        final String xml = this.conversionService.writeXml(health);
        write(httpExchange, xml.getBytes(), APPLICATION_XML);

    }

    private HealthType createHealth() {
        final HealthType h = new HealthType();
        h.setMemory(createMemory());
        h.setApplication(createApplication());
        h.setStatus(!this.scenarios.getScenarios().isEmpty() ? "UP" : "DOWN");
        return h;
    }

    private static MemoryType createMemory() {
        final MemoryType m = new MemoryType();
        final Runtime runtime = Runtime.getRuntime();
        m.setFreeMemory(runtime.freeMemory());
        m.setMaxMemory(runtime.maxMemory());
        m.setTotalMemory(runtime.totalMemory());
        return m;
    }

    private static ApplicationType createApplication() {
        final ApplicationType a = new ApplicationType();
        a.setBuild(EngineInformation.getBuild());
        a.setName(EngineInformation.getName());
        a.setVersion(EngineInformation.getVersion());
        return a;
    }
}
