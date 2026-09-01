package org.kosit.base.xml;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class LoggingSaxErrorHandler implements ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingSaxErrorHandler.class);

    private void log(final CTStandardSeverity severity, final SAXParseException ex) {
        final String msg = ex.getMessage();
        if (severity.isError())
            LOGGER.error(msg);
        else
            LOGGER.warn(msg);
    }

    public void warning(final SAXParseException exception) throws SAXException {
        log(CTStandardSeverity.WARNING, exception);
    }

    public void fatalError(final SAXParseException exception) throws SAXException {
        log(CTStandardSeverity.ERROR, exception);
    }

    public void error(final SAXParseException exception) throws SAXException {
        log(CTStandardSeverity.ERROR, exception);
    }
}