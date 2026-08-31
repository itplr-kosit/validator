package org.kosit.validator.xvrl;

import java.util.stream.Collectors;

import org.kosit.base.error.SimpleError;
import org.kosit.base.string.StringHelper;
import org.kosit.xvrl.api.XvrlHelper;
import org.kosit.xvrl.model.XvrlDetection;
import org.kosit.xvrl.model.XvrlMessage;
import org.kosit.xvrl.model.XvrlSeverity;
import org.kosit.xvrl.model.XvrlSupplemental;
import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;

public class XvrlDetectionBuilder {

    private final XvrlDetection.Builder detection = XvrlDetection.builder();

    public static XvrlDetectionBuilder builder() {
        return new XvrlDetectionBuilder();
    }

    public static XvrlDetectionBuilder builderInfo() {
        return builder().severityInfo();
    }

    public static XvrlDetectionBuilder builderError() {
        return builder().severityError();
    }

    private static XvrlMessage createMessage(final FailedAssert failedAssert) {
        final String string = failedAssert.getText().getContent().stream().map(Object::toString).collect(Collectors.joining());
        return XvrlHelper.createMessage(string);
    }

    private XvrlDetectionBuilder() {
    }

    public XvrlDetectionBuilder supplemental(final XvrlSupplementalBuilder addContent) {
        return addContent != null ? supplemental(addContent.build()) : this;
    }

    private XvrlDetectionBuilder supplemental(final XvrlSupplemental build) {
        this.detection.addSupplemental(build);
        return this;
    }

    public XvrlDetectionBuilder addMessage(final String message) {
        if (StringHelper.isNotBlank(message)) {
            this.detection.addMessage(XvrlHelper.createMessage(message));
        }
        return this;
    }

    public XvrlDetectionBuilder addError(final SimpleError error) {
        if (error != null) {
            addMessage(error.getMessage());
            severity(XvrlHelper.translate(error.getSeverity()));
            if (error.hasLineOrColumnNumber()) {
                this.detection.addLocation(XvrlHelper.createLocation(error));
            }
        }
        return this;
    }

    public XvrlDetectionBuilder add(final ActivePattern activePattern) {
        if (activePattern != null) {
            severityInfo();
            this.detection.code(activePattern.getName());
        }
        return this;
    }

    public XvrlDetectionBuilder add(final FiredRule firedRule) {
        if (firedRule == null) {
            return this;
        }
        severityInfo();
        this.detection.code(firedRule.getName());
        return this;
    }

    public XvrlDetectionBuilder add(final FailedAssert failedAssert) {
        if (failedAssert == null) {
            return this;
        }

        severityError();
        this.detection.addMessage(createMessage(failedAssert));

        return this;
    }

    private XvrlDetectionBuilder severity(final XvrlSeverity severity) {
        // TODO this "worse than" check needs improvement
        if (this.detection.getSeverity() == null || severity.ordinal() > this.detection.getSeverity().ordinal())
            this.detection.severity(severity);
        return this;
    }

    public XvrlDetectionBuilder severityInfo() {
        return severity(XvrlSeverity.INFO);
    }

    public XvrlDetectionBuilder severityWarning() {
        return severity(XvrlSeverity.WARNING);
    }

    public XvrlDetectionBuilder severityError() {
        return severity(XvrlSeverity.ERROR);
    }

    public XvrlDetectionBuilder code(final String code) {
        if (StringHelper.isNotBlank(code)) {
            this.detection.code(code);
        }
        return this;
    }

    public XvrlDetectionBuilder id(final String id) {
        if (StringHelper.isNotBlank(id)) {
            this.detection.id(id);
        }
        return this;
    }

    public XvrlDetection build() {
        return this.detection.build();
    }
}
