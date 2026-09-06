package org.kosit.schematron.resolve;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Andreas Penski
 */
public abstract class AbstractResolvingStrategy implements ResolvingConfigurationStrategy {

    @FunctionalInterface
    private interface PropertySetter {

        void apply() throws SAXException;
    }

    protected static final boolean DEFAULT_LENIENT = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResolvingStrategy.class);

    protected AbstractResolvingStrategy() {
    }

    private void setProperty(final PropertySetter setter, final boolean lenient, final String errorMessage) {
        try {
            setter.apply();
        } catch (final SAXException e) {
            if (!lenient) {
                throw new IllegalStateException(errorMessage);
            }

            if (LOGGER.isDebugEnabled())
                LOGGER.debug(errorMessage + " - " + e.getMessage(), e);
            else
                LOGGER.warn(errorMessage);
        }
    }

    protected void allowExternalSchema(final Validator validator, final boolean lenient, final String... schemes) {
        final String schemeString = String.join(",", schemes);
        setProperty(() -> validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, schemeString), lenient,
                "Can set  external schema  access to schemes (" + schemeString + "). Maybe an unsupported JAXP implementation is used.");
    }

    protected void allowExternalSchema(final SchemaFactory schemaFactory, final boolean lenient, final String... schemes) {
        final String schemeString = String.join(",", schemes);
        setProperty(() -> schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, schemeString), lenient,
                "Can set  external schema  access to schemes (" + schemeString + "). Maybe an unsupported JAXP implementation is used.");
    }

    protected void disableExternalEntities(final Validator validator, final boolean lenient) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Try to disable extern DTD access");
        setProperty(() -> validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""), lenient,
                "Can not disable external DTD access. Maybe an unsupported JAXP implementation is used.");
    }

    protected void disableExternalEntities(final SchemaFactory schemaFactory, final boolean lenient) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Try to disable extern DTD access");
        setProperty(() -> schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""), lenient,
                "Can not disable external DTD access. Maybe an unsupported JAXP implementation is used.");
    }
}
