package org.kosit.validator.impl.model;

import org.slf4j.Logger;

import org.kosit.validator.api.XmlError;
import org.kosit.validator.model.XMLSyntaxErrorSeverity;

/**
 * Base class for syntax errors. Extended via the JAXB-generated class {@link org.kosit.validator.model.XMLSyntaxError}.
 *
 * @author Andreas Penski
 */
public abstract class BaseXMLSyntaxError implements XmlError {

    /**
     * Logs the syntax error via a defined logger.
     *
     * @param logger the logger
     */
    public void log(final Logger logger) {
        final String msgTemplate = "{} At row {} at pos {}";
        final Object[] params = { getMessage(), getRowNumber(), getColumnNumber() };
        if (getSeverityCode() == XMLSyntaxErrorSeverity.SEVERITY_WARNING) {
            logger.warn(msgTemplate, params);
        } else {
            logger.error(msgTemplate, params);
        }

    }

    @Override
    public String toString() {
        return getMessage() + " At row " + getRowNumber() + " at pos " + getColumnNumber();
    }

    /**
     * Getter from the schema
     *
     * @return severity
     */
    public abstract XMLSyntaxErrorSeverity getSeverityCode();

    /**
     * This is the API access. There are two methods because the API uses a different type.
     *
     * @return the severity
     */
    @Override
    public Severity getSeverity() {
        final XMLSyntaxErrorSeverity code = getSeverityCode();
        return code != null ? Severity.valueOf(code.name()) : null;
    }
}
