package org.kosit.jaxb.eventhandler;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;
import jakarta.xml.bind.ValidationEventLocator;

public class LoggingEventHandler implements ValidationEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingEventHandler.class);

    private static CTStandardSeverity toSeverity(final int n) {
        return switch (n) {
            case ValidationEvent.WARNING -> CTStandardSeverity.WARNING;
            default -> CTStandardSeverity.ERROR;
        };
    }

    private static String toString(final ValidationEventLocator locator) {
        String ret = "";
        if (locator.getURL() != null)
            ret += locator.getURL().toExternalForm();
        if (locator.getLineNumber() > 0 || locator.getColumnNumber() > 0) {
            ret += '[';
            ret += locator.getLineNumber() > 0 ? Integer.toString(locator.getLineNumber()) : "?";
            ret += ':';
            ret += locator.getColumnNumber() > 0 ? Integer.toString(locator.getColumnNumber()) : "?";
            ret += ']';
        }
        return ret;
    }

    public boolean handleEvent(final ValidationEvent event) {
        final var severity = toSeverity(event.getSeverity());
        final var prefix = "[JAXB] ";
        final var location = toString(event.getLocator());
        final var msg = event.getMessage();
        if (severity.isWarning())
            LOGGER.warn(prefix + location + " " + msg, event.getLinkedException());
        else
            LOGGER.error(prefix + location + " " + msg, event.getLinkedException());
        return true;
    }

}
