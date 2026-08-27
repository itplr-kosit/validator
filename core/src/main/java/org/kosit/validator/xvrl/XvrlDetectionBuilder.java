package org.kosit.validator.xvrl;

import java.util.stream.Collectors;

import org.kosit.base.error.SimpleError;
import org.kosit.base.string.StringHelper;
import org.kosit.xvrl.api.XvrlHelper;
import org.kosit.xvrl.model.XvrlDetectionType;
import org.kosit.xvrl.model.XvrlMessageType;
import org.kosit.xvrl.model.XvrlSeverityType;
import org.kosit.xvrl.model.XvrlSupplementalType;
import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;

public class XvrlDetectionBuilder {

    private final XvrlDetectionType detection = new XvrlDetectionType();

    public static XvrlDetectionBuilder builder() {
        return new XvrlDetectionBuilder();
    }

    public static XvrlDetectionBuilder builderInfo() {
        return builder().severityInfo();
    }

    public static XvrlDetectionBuilder builderError() {
        return builder().severityError();
    }

    private static XvrlMessageType createMessage(final FailedAssert failedAssert) {
        final String string = failedAssert.getText().getContent().stream().map(Object::toString).collect(Collectors.joining());
        return XvrlHelper.createMessage(string);
    }

    private XvrlDetectionBuilder() {
    }

    public XvrlDetectionBuilder supplemental(final XvrlSupplementalBuilder addContent) {
        return addContent != null ? supplemental(addContent.build()) : this;
    }

    private XvrlDetectionBuilder supplemental(final XvrlSupplementalType build) {
        if (build != null) {
            detection.getSupplementals().add(build);
        }
        return this;
    }

    public XvrlDetectionBuilder addMessage(final String message) {
        if (StringHelper.isNotBlank(message)) {
            this.detection.getMessages().add(XvrlHelper.createMessage(message));
        }
        return this;
    }

    public XvrlDetectionBuilder addError(final SimpleError error) {
        if (error != null) {
            addMessage(error.getMessage());
            severity(XvrlHelper.translate(error.getSeverity()));
            if (error.hasLineOrColumnNumber()) {
                this.detection.getLocations().add(XvrlHelper.createLocation(error));
            }
        }
        return this;
    }

    public XvrlDetectionBuilder add(final ActivePattern activePattern) {
        if (activePattern != null) {
            severityInfo();
            this.detection.setCode(activePattern.getName());
        }
        return this;
    }

    public XvrlDetectionBuilder add(final FiredRule firedRule) {
        if (firedRule == null) {
            return this;
        }
        severityInfo();
        this.detection.setCode(firedRule.getName());
        return this;
    }

    public XvrlDetectionBuilder add(final FailedAssert failedAssert) {
        if (failedAssert == null) {
            return this;
        }

        severityError();
        this.detection.getMessages().add(createMessage(failedAssert));

        return this;
    }

    private XvrlDetectionBuilder severity(final XvrlSeverityType severity) {
        // TODO this "worse than" check needs improvement
        if (detection.getSeverity() == null || severity.ordinal() > (detection.getSeverity().ordinal()))
            detection.setSeverity(severity);
        return this;
    }

    public XvrlDetectionBuilder severityInfo() {
        return severity(XvrlSeverityType.INFO);
    }

    public XvrlDetectionBuilder severityWarning() {
        return severity(XvrlSeverityType.WARNING);
    }

    public XvrlDetectionBuilder severityError() {
        return severity(XvrlSeverityType.ERROR);
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
        return this.detection;
    }
}