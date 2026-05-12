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
            if (is != null) {
                props.load(is);
            }
        } catch (final IOException e) {
            // fall through to defaults
        }
        this.name = props.getProperty("validator.name", "validator-cli");
        this.version = props.getProperty("validator.version", "unknown");
        this.frameworkVersion = props.getProperty("validator.framework-version", "unknown");
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
