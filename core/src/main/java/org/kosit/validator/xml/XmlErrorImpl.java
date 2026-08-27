package org.kosit.validator.xml;

import org.kosit.validator.api.xmlerror.XmlError;
import org.kosit.validator.api.xmlerror.XmlSeverity;
import org.kosit.xvrl.model.XvrlDetectionType;
import org.kosit.xvrl.model.XvrlLocationType;

public class XmlErrorImpl implements XmlError {

    private final String message;

    private final XmlSeverity severity;

    private Long rowNumber;

    private Long columnNumber;

    private static XmlSeverity getSeverityFromDetection(final XvrlDetectionType xvrlDetection) {
        return switch (xvrlDetection.getSeverity()) {
            case ERROR -> XmlSeverity.SEVERITY_ERROR;
            case FATAL_ERROR -> XmlSeverity.SEVERITY_FATAL_ERROR;
            default -> XmlSeverity.SEVERITY_WARNING;
        };
    }

    public XmlErrorImpl(final XvrlDetectionType xvrlDetection) {
        this.message = xvrlDetection.getErrorMessage();
        this.severity = getSeverityFromDetection(xvrlDetection);
        final XvrlLocationType location = xvrlDetection.getErrorLocation();
        if (location != null) {
            this.rowNumber = location.getLine();
            this.columnNumber = location.getColumn();
        }
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public XmlSeverity getSeverity() {
        return this.severity;
    }

    @Override
    public Long getRowNumber() {
        return this.rowNumber;
    }

    @Override
    public Long getColumnNumber() {
        return this.columnNumber;
    }
}
