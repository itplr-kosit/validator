package de.kosit.validationtool.daemon;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.sun.net.httpserver.HttpExchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.config.Keys;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.model.scenarios.Scenarios;

/**
 * Handler that returns the actual configuration used for this daemon instance.
 * 
 * @author Andreas Penski
 */
@Slf4j
@RequiredArgsConstructor
class ConfigHandler extends BaseHandler {

    private final Configuration configuration;

    private final ConversionService conversionService;

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        try {
            final Optional<String> xml = getSource();
            if (xml.isPresent()) {
                write(exchange, xml.get().getBytes(), APPLICATION_XML);
            } else {
                error(exchange, 404, "No configuration found");
            }
        } catch (final Exception e) {
            log.error("Error grabbing configuration", e);
            error(exchange, 500, "Error grabbing configuration: " + e.getMessage());
        }
    }

    private Optional<String> getSource() {
        final URI fileUri = (URI) this.configuration.getAdditionalParameters().get(Keys.SCENARIOS_FILE);
        return fileUri != null ? loadFile(fileUri) : loadFromConfig();
    }

    private static Optional<String> loadFile(final URI fileUri) {
        try ( final Reader in = new InputStreamReader(fileUri.toURL().openStream());
              final StringWriter out = new StringWriter() ) {
            IOUtils.copy(in, out);
            return Optional.of(out.toString());
        } catch (final IOException e) {
            return Optional.empty();
        }
    }

    private Optional<String> loadFromConfig() {
        final Optional<String> result;
        final Scenarios scenarios = (Scenarios) this.configuration.getAdditionalParameters().get(Keys.SCENARIO_DEFINITION);
        if (scenarios != null) {
            final String s = this.conversionService.writeXml(scenarios);
            result = Optional.of(s);
        } else {
            result = Optional.empty();
        }
        return result;
    }

}
