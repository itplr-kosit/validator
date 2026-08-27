package org.kosit.validator.api.xmlerror;

import org.slf4j.Logger;
import org.kosit.validator.model.XmlSyntaxErrorSeverity;

/**
 * Base class for syntax errors. Extended via the JAXB-generated class {@link org.kosit.validator.model.XmlSyntaxError}.
 *
 * @author Andreas Penski
 */
public abstract class AbstractXmlSyntaxError implements XmlError {

    /**
     * Getter from the schema
     *
     * @return severity
     */
    public abstract XmlSyntaxErrorSeverity getSeverityCode();

    /**
     * Logs the syntax error via a defined logger.
     *
     * @param logger the logger
     */
    public void log(final Logger logger) {
        final String msgTemplate = "{} At row {} at pos {}";
        final Object[] params = { getMessage(), getRowNumber(), getColumnNumber() };
        if (getSeverityCode() == XmlSyntaxErrorSeverity.SEVERITY_WARNING) {
            logger.warn(msgTemplate, params);
        } else {
            logger.error(msgTemplate, params);
        }
    }

    /**
     * This is the API access. There are two methods because the API uses a different type.
     *
     * @return the severity
     */
    @Override
    public XmlSeverity getSeverity() {
        final XmlSyntaxErrorSeverity code = getSeverityCode();
        return code != null ? XmlSeverity.valueOf(code.name()) : null;
    }

    @Override
    public String toString() {
        return getMessage() + " At row " + getRowNumber() + " at pos " + getColumnNumber();
    }
}
