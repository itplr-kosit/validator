package org.kosit.validator.xvrl;

import static org.kosit.validator.api.xmlerror.XmlError.Severity.SEVERITY_FATAL_ERROR;

import java.util.stream.Collectors;

import org.kosit.base.string.StringHelper;
import org.kosit.validator.api.xmlerror.XmlError;
import org.kosit.xvrl.model.XvrlDetectionType;
import org.kosit.xvrl.model.XvrlLocationType;
import org.kosit.xvrl.model.XvrlMessageType;
import org.kosit.xvrl.model.XvrlSupplementalType;
import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;

public class XvrlDetectionBuilder {

    private final XvrlDetectionType detection = new XvrlDetectionType();

    public static XvrlDetectionBuilder detectionBuilder() {
        return new XvrlDetectionBuilder();
    }

    private static XvrlDetectionType.Severity translate(final XmlError.Severity severity) {
        if (severity == SEVERITY_FATAL_ERROR) {
            return XvrlDetectionType.Severity.FATAL_ERROR;
        }
        return XvrlDetectionType.Severity.ERROR;

    }

    private static XvrlLocationType createLocation(final Long line, final Long row, final String xpath) {
        final XvrlLocationType location = new XvrlLocationType();
        location.setLine(line);
        location.setColumn(row);
        location.setXpath(xpath);
        return location;
    }

    private static XvrlMessageType createMessage(final String message) {
        final XvrlMessageType messageObject = new XvrlMessageType();
        messageObject.getContent().add(message);
        return messageObject;
    }

    private static XvrlMessageType getMessage(final FailedAssert failedAssert) {
        final String string = failedAssert.getText().getContent().stream().map(Object::toString).collect(Collectors.joining());
        return createMessage(string);
    }

    public XvrlDetectionBuilder add(final XvrlSupplementalBuilder addContent) {
        if (addContent != null) {
            add(addContent.build());
        }
        return this;
    }

    private XvrlDetectionBuilder add(final XvrlSupplementalType build) {
        if (build != null) {
            this.detection.getSupplementals().add(build);
        }
        return this;
    }

    public XvrlDetectionBuilder addError(final String message) {
        addMessage(message);
        this.detection.setSeverity(XvrlDetectionType.Severity.ERROR);
        return this;
    }

    public XvrlDetectionBuilder addMessage(final String message) {
        if (StringHelper.isNotBlank(message)) {
            this.detection.getMessages().add(createMessage(message));
        }
        return this;
    }

    public XvrlDetectionBuilder addError(final XmlError error) {
        if (error == null) {
            return this;
        }
        addMessage(error.getMessage());
        this.detection.setSeverity(translate(error.getSeverity()));

        if (error.getRowNumber() != null && error.getColumnNumber() != null) {
            this.detection.getLocations().add(createLocation(error.getRowNumber(), error.getColumnNumber(), null));
        }
        return this;
    }

    public XvrlDetectionBuilder add(final ActivePattern activePattern) {
        if (activePattern == null) {
            return this;
        }
        this.detection.setSeverity(XvrlDetectionType.Severity.INFO);
        this.detection.setCode(activePattern.getName());
        return this;
    }

    public XvrlDetectionBuilder add(final FiredRule firedRule) {
        if (firedRule == null) {
            return this;
        }
        this.detection.setSeverity(XvrlDetectionType.Severity.INFO);
        this.detection.setCode(firedRule.getName());
        return this;
    }

    public XvrlDetectionBuilder add(final FailedAssert failedAssert) {
        if (failedAssert == null) {
            return this;
        }

        this.detection.setSeverity(XvrlDetectionType.Severity.ERROR);
        this.detection.getMessages().add(getMessage(failedAssert));

        return this;
    }

    public XvrlDetectionBuilder severity(final XvrlDetectionType.Severity info) {
        this.detection.setSeverity(info);
        return this;
    }

    public XvrlDetectionBuilder code(final String code) {
        if (StringHelper.isNotBlank(code)) {
            this.detection.setCode(code);
        }
        return this;
    }

    public XvrlDetectionBuilder id(final String id) {
        if (StringHelper.isNotBlank(id)) {
            this.detection.setId(id);
        }
        return this;
    }

    public XvrlDetectionType build() {
        if (this.detection.getSeverity() == null) {
            this.detection.setSeverity(XvrlDetectionType.Severity.INFO);
        }
        return this.detection;
    }
}