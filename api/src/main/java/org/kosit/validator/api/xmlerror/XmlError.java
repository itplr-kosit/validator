package org.kosit.validator.api.xmlerror;

/**
 * Error object for providing errors from internal processing, e.g. schema validation errors.
 *
 * @author Andreas Penski
 */
public interface XmlError {

    /**
     * Returns the error message.
     *
     * @return The message itself
     */
    String getMessage();

    /**
     * Indicates the severity of the error message.
     *
     * @return The severity of the error.
     * @see XmlSeverity
     */
    XmlSeverity getSeverity();

    /**
     * Optionally returns a row number from which the error originates.
     *
     * @return The row number or <code>null</code>.
     */
    Long getRowNumber();

    /**
     * Optionally returns a column number from which the error originates.
     *
     * @return The column number or <code>null</code>.
     */
    Long getColumnNumber();

}
