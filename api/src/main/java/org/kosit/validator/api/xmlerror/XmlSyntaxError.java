//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v4.0.9 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2026.08.27 um 07:05:47 PM CEST
//

package org.kosit.validator.api.xmlerror;

import org.slf4j.Logger;

public class XmlSyntaxError implements XmlError {

    protected String message;

    protected XmlSeverity severity;

    protected Long rowNumber;

    protected Long columnNumber;

    /**
     * Ruft den Wert der message-Eigenschaft ab.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getMessage() {
        return message;
    }

    /**
     * Legt den Wert der message-Eigenschaft fest.
     * 
     * @param value allowed object is {@link String }
     * 
     */
    public void setMessage(final String value) {
        this.message = value;
    }

    /**
     * Ruft den Wert der severityCode-Eigenschaft ab.
     * 
     * @return possible object is {@link XmlSeverity }
     * 
     */
    public XmlSeverity getSeverity() {
        return severity;
    }

    /**
     * Legt den Wert der severityCode-Eigenschaft fest.
     * 
     * @param value allowed object is {@link XmlSeverity }
     * 
     */
    public void setSeverity(final XmlSeverity value) {
        this.severity = value;
    }

    /**
     * Ruft den Wert der rowNumber-Eigenschaft ab.
     * 
     * @return possible object is {@link Long }
     * 
     */
    public Long getRowNumber() {
        return rowNumber;
    }

    /**
     * Legt den Wert der rowNumber-Eigenschaft fest.
     * 
     * @param value allowed object is {@link Long }
     * 
     */
    public void setRowNumber(final Long value) {
        this.rowNumber = value;
    }

    /**
     * Ruft den Wert der columnNumber-Eigenschaft ab.
     * 
     * @return possible object is {@link Long }
     * 
     */
    public Long getColumnNumber() {
        return columnNumber;
    }

    /**
     * Legt den Wert der columnNumber-Eigenschaft fest.
     * 
     * @param value allowed object is {@link Long }
     * 
     */
    public void setColumnNumber(final Long value) {
        this.columnNumber = value;
    }

    /**
     * Logs the syntax error via a defined logger.
     *
     * @param logger the logger
     */
    public void log(final Logger logger) {
        final String msgTemplate = "{} At row {} at pos {}";
        final Object[] params = { getMessage(), getRowNumber(), getColumnNumber() };
        if (getSeverity() == XmlSeverity.WARNING) {
            logger.warn(msgTemplate, params);
        } else {
            logger.error(msgTemplate, params);
        }
    }

    @Override
    public String toString() {
        return getMessage() + " At row " + getRowNumber() + " at pos " + getColumnNumber();
    }
}
