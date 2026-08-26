package org.kosit.validator.impl.xml;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Andreas Penski
 */
public abstract class BaseResolvingStrategy implements ResolvingConfigurationStrategy {

    @FunctionalInterface
    private interface PropertySetter {

        void apply() throws SAXException;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseResolvingStrategy.class);

    protected BaseResolvingStrategy() {
    }

    private void setProperty(final PropertySetter setter, final boolean lenient, final String errorMessage) {
        try {
            setter.apply();
        } catch (final SAXException e) {
            if (!lenient) {
                throw new IllegalStateException(errorMessage);
            }
            LOGGER.warn(errorMessage);
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(e.getMessage(), e);
        }
    }

    protected void allowExternalSchema(final Validator validator, final String... scheme) {
        allowExternalSchema(validator, false, scheme);
    }

    protected void allowExternalSchema(final Validator validator, final boolean lenient, final String... schemes) {
        final String schemeString = String.join(",", schemes);
        setProperty(() -> validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, schemeString), lenient,
                "Can set  external schema  access to schemes (" + schemeString + "). Maybe an unsupported JAXP implementation is used.");
    }

    protected void allowExternalSchema(final SchemaFactory schemaFactory, final String... scheme) {
        allowExternalSchema(schemaFactory, false, scheme);
    }

    protected void allowExternalSchema(final SchemaFactory schemaFactory, final boolean lenient, final String... schemes) {
        final String schemeString = String.join(",", schemes);
        setProperty(() -> schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, schemeString), lenient,
                "Can set  external schema  access to schemes (" + schemeString + "). Maybe an unsupported JAXP implementation is used.");
    }

    protected void disableExternalEntities(final Validator validator) {
        disableExternalEntities(validator, false);
    }

    protected void disableExternalEntities(final Validator validator, final boolean lenient) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Try to disable extern DTD access");
        setProperty(() -> validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""), lenient,
                "Can not disable external DTD access. Maybe an unsupported JAXP implementation is used.");
    }

    protected void disableExternalEntities(final SchemaFactory schemaFactory) {
        disableExternalEntities(schemaFactory, false);
    }

    protected void disableExternalEntities(final SchemaFactory schemaFactory, final boolean lenient) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Try to disable extern DTD access");
        setProperty(() -> schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, ""), lenient,
                "Can not disable external DTD access. Maybe an unsupported JAXP implementation is used.");
    }
}
