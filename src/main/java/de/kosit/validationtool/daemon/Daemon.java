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

import static de.kosit.validationtool.impl.Printer.writeOut;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.DefaultCheck;
import de.kosit.validationtool.model.daemon.HealthType;

/**
 * HTTP-Daemon für die Bereitstellung der Prüf-Funktionalität via http.
 *
 * @author Roula Antoun
 */
@RequiredArgsConstructor
@Setter
@Slf4j
public class Daemon {

    private static final String DEFAULT_HOST = "localhost";

    private static final int DEFAULT_PORT = 8080;

    private String bindAddress;

    private int port;

    private int threadCount;

    private boolean guiEnabled = true;

    /**
     * Create a new daemon.
     * 
     * @param hostname the interface to bind to
     * @param port the port to expose
     * @param threadCount the number of working threads
     */
    public Daemon(final String hostname, final int port, final int threadCount) {
        this.bindAddress = hostname;
        this.port = port;
        this.threadCount = threadCount;
    }

    /**
     * Methode zum Starten des Servers
     * 
     * @param config the configuration to use
     */
    public void startServer(final Configuration config) {
        HttpServer server = null;
        try {
            final ConversionService healthConverter = new ConversionService();
            healthConverter.initialize(HealthType.class.getPackage());
            final ConversionService converter = new ConversionService();

            server = HttpServer.create(getSocket(), 0);
            server.createContext("/", createRootHandler(config));
            server.createContext("/server/health", new HealthHandler(config, healthConverter));
            server.createContext("/server/config", new ConfigHandler(config, converter));
            server.setExecutor(createExecutor());
            server.start();
            log.info("Server {} started", server.getAddress());
            writeOut("Daemon started. Visit http://{0}", this.bindAddress + ":" + this.port);
        } catch (final IOException e) {
            log.error("Error starting HttpServer for Valdidator: {}", e.getMessage(), e);
        }
    }

    private HttpHandler createRootHandler(final Configuration config) {
        final HttpHandler rootHandler;
        final DefaultCheck check = new DefaultCheck(config);
        final CheckHandler checkHandler = new CheckHandler(check, config.getContentRepository().getProcessor());
        if (this.guiEnabled) {
            final GuiHandler gui = new GuiHandler();
            rootHandler = new RoutingHandler(checkHandler, gui);
        } else {
            rootHandler = checkHandler;
        }
        return rootHandler;
    }

    private ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(this.threadCount > 0 ? this.threadCount : Runtime.getRuntime().availableProcessors());
    }

    private InetSocketAddress getSocket() {
        return new InetSocketAddress(defaultIfBlank(this.bindAddress, DEFAULT_HOST), this.port > 0 ? this.port : DEFAULT_PORT);
    }

}
