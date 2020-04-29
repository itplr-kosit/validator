package de.kosit.validationtool.daemon;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.model.daemon.HealthType;
import de.kosit.validationtool.model.daemon.MemoryType;

/**
 * Handler that implements a simple health check. Useful for monitoring the service.
 * 
 * @author Andreas Penski`
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

    private static HealthType createHealth() {
        final HealthType h = new HealthType();
        h.setMemory(createMemory());
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
}
