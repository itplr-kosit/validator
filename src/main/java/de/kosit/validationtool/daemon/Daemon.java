package de.kosit.validationtool.daemon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

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
            converter.initialize(HealthType.class.getPackage());

            server = HttpServer.create(new InetSocketAddress(this.hostName, this.port), 0);
            final DefaultCheck check = new DefaultCheck(config);
            server.createContext("/", new CheckHandler(check, config.getContentRepository().getProcessor()));
            server.createContext("/server/health", new HealthHandler(config, converter));
            server.createContext("/server/config", new ConfigHandler(config, new ConversionService()));
            server.setExecutor(Executors.newFixedThreadPool(this.threadCount));
            server.start();
            log.info("Server unter Port {} ist erfolgreich gestartet", this.port);
        } catch (final IOException e) {
            log.error("Fehler beim HttpServer erstellen: {}", e.getMessage(), e);
        }
    }
}
