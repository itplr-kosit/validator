package de.kosit.validationtool.daemon;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.DefaultCheck;

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

    private static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    private final String hostName;

    private final int port;

    private final int threadCount;

    /**
     * Methode zum Starten des Servers
     * 
     * @param config the configuration to use
     */
    public void startServer(final Configuration config) {
        HttpServer server = null;
        try {
            final ConversionService converter = new ConversionService();

            server = HttpServer.create(getSocket(), 0);
            final DefaultCheck check = new DefaultCheck(config);
            server.createContext("/", new CheckHandler(check, config.getContentRepository().getProcessor()));
            server.createContext("/server/health", new HealthHandler(config, converter));
            server.createContext("/server/config", new ConfigHandler(config, converter));
            server.setExecutor(createExecutor());
            server.start();
            log.info("Server {} started", server.getAddress());
        } catch (final IOException e) {
            log.error("Error starting HttpServer for Valdidator: {}", e.getMessage(), e);
        }
    }

    private ExecutorService createExecutor() {
        return Executors.newFixedThreadPool(this.threadCount > 0 ? this.threadCount : DEFAULT_THREAD_COUNT);
    }

    private InetSocketAddress getSocket() {
        return new InetSocketAddress(defaultIfBlank(this.hostName, DEFAULT_HOST), this.port > 0 ? this.port : DEFAULT_PORT);
    }
}
