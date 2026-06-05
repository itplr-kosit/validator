package org.kosit.validator.impl.xvrl;

import org.kosit.validator.api.XmlError;
import org.kosit.xvrl.model.Location;
import org.kosit.xvrl.model.XVRLDetection;

public class XmlErrorImpl implements XmlError {

    private final String message;

    private final Severity severity;

    private Long rowNumber;

    private Long columnNumber;

    public XmlErrorImpl(final XVRLDetection xvrlDetection) {
        this.message = xvrlDetection.getErrorMessage();
        this.severity = getSeverityFromDetection(xvrlDetection);
        final Location location = xvrlDetection.getErrorLocation();
        if (location != null) {
            this.rowNumber = location.getLine();
            this.columnNumber = location.getColumn();
        }
    }

    private static Severity getSeverityFromDetection(final XVRLDetection xvrlDetection) {
        switch (xvrlDetection.getSeverity()) {
            case ERROR: {
                return Severity.SEVERITY_ERROR;
            }
            case FATAL_ERROR: {
                return Severity.SEVERITY_FATAL_ERROR;
            }
            default: {
                return Severity.SEVERITY_WARNING;
            }
        }
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
