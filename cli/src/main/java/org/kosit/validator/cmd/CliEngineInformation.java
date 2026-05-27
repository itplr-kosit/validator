package org.kosit.validator.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.kosit.validator.impl.EngineInformation;

/**
 * Static implementation of {@link EngineInformation} reading build metadata from a Maven-filtered properties file.
 */
public class CliEngineInformation implements EngineInformation {

    private static final String RESOURCE = "/cli-info.properties";

    private final String name;

    private final String version;

    private final String frameworkVersion;

    public CliEngineInformation() {
        final Properties props = new Properties();
        try ( InputStream is = CliEngineInformation.class.getResourceAsStream(RESOURCE) ) {
            if (is == null) {
                throw new IllegalStateException("Required classpath resource " + RESOURCE + " is missing");
            }
            props.load(is);
        } catch (final IOException e) {
            throw new IllegalStateException("Unable to load " + RESOURCE, e);
        }
        this.name = required(props, "validator.name");
        this.version = required(props, "validator.version");
        this.frameworkVersion = required(props, "validator.framework-version");
    }

    private static String required(final Properties props, final String key) {
        final String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required property '" + key + "' is missing in " + RESOURCE);
        }
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFrameworkVersion() {
        return frameworkVersion;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getBuild() {
        return version;
    }
}
