package org.kosit.validator.impl.xvrl;

import org.kosit.validator.api.xmlerror.XmlError;
import org.kosit.xvrl.model.XVRLDetectionType;
import org.kosit.xvrl.model.XVRLLocationType;

public class XmlErrorImpl implements XmlError {

    private final String message;

    private final Severity severity;

    private Long rowNumber;

    private Long columnNumber;

    public XmlErrorImpl(final XVRLDetectionType xvrlDetection) {
        this.message = xvrlDetection.getErrorMessage();
        this.severity = getSeverityFromDetection(xvrlDetection);
        final XVRLLocationType location = xvrlDetection.getErrorLocation();
        if (location != null) {
            this.rowNumber = location.getLine();
            this.columnNumber = location.getColumn();
        }
    }

    private static Severity getSeverityFromDetection(final XVRLDetectionType xvrlDetection) {
        return switch (xvrlDetection.getSeverity()) {
            case ERROR -> Severity.SEVERITY_ERROR;
            case FATAL_ERROR -> Severity.SEVERITY_FATAL_ERROR;
            default -> Severity.SEVERITY_WARNING;
        };
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public Severity getSeverity() {
        return this.severity;
    }

    @Override
    public Integer getRowNumber() {
        return Math.toIntExact(this.rowNumber);
    }

    @Override
    public Integer getColumnNumber() {
        return Math.toIntExact(this.columnNumber);
    }
}
