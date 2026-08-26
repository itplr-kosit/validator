package org.kosit.validator.impl.xvrl;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kosit.validator.api.XmlError.Severity.SEVERITY_FATAL_ERROR;

import java.util.stream.Collectors;

import org.kosit.validator.api.XmlError;
import org.kosit.xvrl.model.Location;
import org.kosit.xvrl.model.Supplemental;
import org.kosit.xvrl.model.XVRLDetection;
import org.kosit.xvrl.model.XVRLMessage;
import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;

public class XvrlDetectionBuilder {

    private final XVRLDetection detection = new XVRLDetection();

    private static XVRLDetection.Severity translate(final XmlError.Severity severity) {
        if (severity == SEVERITY_FATAL_ERROR) {
            return XVRLDetection.Severity.FATAL_ERROR;
        }
        return XVRLDetection.Severity.ERROR;

    }

    private static Location createLocation(final int line, final int row, final String xpath) {
        final Location location = new Location();
        location.setLine(Long.valueOf(line));
        location.setColumn(Long.valueOf(row));
        location.setXpath(xpath);
        return location;
    }

    private static XVRLMessage createMessage(final String message) {
        final XVRLMessage messageObject = new XVRLMessage();
        messageObject.getContent().add(message);
        return messageObject;
    }

    private static XVRLMessage getMessage(final FailedAssert failedAssert) {
        final String string = failedAssert.getText().getContent().stream().map(Object::toString).collect(Collectors.joining());
        return createMessage(string);
    }

    public XvrlDetectionBuilder add(final XvrlSupplementalBuilder addContent) {
        if (addContent != null) {
            add(addContent.build());
        }
        return this;
    }

    private XvrlDetectionBuilder add(final Supplemental build) {
        if (build != null) {
            this.detection.getSupplementals().add(build);
        }
        return this;
    }

    public XvrlDetectionBuilder addError(final String message) {
        addMessage(message);
        this.detection.setSeverity(XVRLDetection.Severity.ERROR);
        return this;
    }

    public XvrlDetectionBuilder addMessage(final String message) {
        if (isNotBlank(message)) {
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
        this.detection.setSeverity(XVRLDetection.Severity.INFO);
        this.detection.setCode(activePattern.getName());
        return this;
    }

    public XvrlDetectionBuilder add(final FiredRule firedRule) {
        if (firedRule == null) {
            return this;
        }
        this.detection.setSeverity(XVRLDetection.Severity.INFO);
        this.detection.setCode(firedRule.getName());
        return this;
    }

    public XvrlDetectionBuilder add(final FailedAssert failedAssert) {
        if (failedAssert == null) {
            return this;
        }

        this.detection.setSeverity(XVRLDetection.Severity.ERROR);
        this.detection.getMessages().add(getMessage(failedAssert));

        return this;
    }

    public XvrlDetectionBuilder severity(final XVRLDetection.Severity info) {
        this.detection.setSeverity(info);
        return this;
    }

    public XvrlDetectionBuilder code(final String code) {
        if (isNotBlank(code)) {
            this.detection.setCode(code);
        }
        return this;
    }

    public XvrlDetectionBuilder id(final String id) {
        if (isNotBlank(id)) {
            this.detection.setId(id);
        }
        return this;
    }

    public XVRLDetection build() {
        if (this.detection.getSeverity() == null) {
            this.detection.setSeverity(XVRLDetection.Severity.INFO);
        }
        return this.detection;
    }
}