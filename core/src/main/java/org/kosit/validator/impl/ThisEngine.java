package org.kosit.validator.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.kosit.base.string.StringHelper;

/**
 * What this validator says about itself, read from a Maven filtered properties file of this module. This is the
 * {@link EngineInformation} every engine answers with unless an application that embeds it states its own.
 *
 * @author Andreas Schmitz
 */
final class ThisEngine implements EngineInformation {

    private static final String RESOURCE = "/validator-engine.properties";

    static final EngineInformation INSTANCE = new ThisEngine();

    private final String name;

    private final String version;

    private ThisEngine() {
        final Properties properties = new Properties();
        try ( InputStream is = ThisEngine.class.getResourceAsStream(RESOURCE) ) {
            if (is == null) {
                throw new IllegalStateException("Required classpath resource " + RESOURCE + " is missing");
            }
            properties.load(is);
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to load " + RESOURCE, e);
        }
        this.name = required(properties, "validator.name");
        this.version = required(properties, "validator.version");
    }

    private static String required(final Properties properties, final String key) {
        final String value = properties.getProperty(key);
        if (StringHelper.isBlank(value)) {
            throw new IllegalStateException("Required property '" + key + "' is missing in " + RESOURCE);
        }
        return value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    @Override
    public String toString() {
        return this.name + " " + this.version;
    }
}
