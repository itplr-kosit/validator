package org.kosit.validator.impl.conformatron.action.parsedoc.xml;

import java.util.List;

import org.conformatron.api.model.detection.CTDetection;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Collects every well-formedness error as a FATAL detection (one detection per parser error, with line/column) instead
 * of aborting on the first one.
 * 
 * @param resourceId Resource ID of the resource being read
 * @param errors The error list to be filled.
 */
public record CollectingErrorHandler(String resourceId, List<CTDetection> errors) implements ErrorHandler {

    @Override
    public void warning(final SAXParseException e) {
        // well-formedness only: parser warnings do not affect the outcome of this step
    }

    @Override
    public void error(final SAXParseException e) {
        this.errors.add(XMLDetection.errorNotWellformed(this.resourceId, e));
    }

    @Override
    public void fatalError(final SAXParseException e) throws SAXException {
        this.errors.add(XMLDetection.errorNotWellformed(this.resourceId, e));
        throw e;
    }
}