package org.kosit.validator.xvrl;

import java.util.List;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.validator.impl.ActionMetadata;
import org.kosit.validator.scenario.v1.ResourceType;
import org.kosit.xvrl.api.XvrlHelper;
import org.kosit.xvrl.model.XvrlDetectionType;
import org.kosit.xvrl.model.XvrlDigestType;
import org.kosit.xvrl.model.XvrlDocumentType;
import org.kosit.xvrl.model.XvrlMetadataType;
import org.kosit.xvrl.model.XvrlReportType;
import org.kosit.xvrl.model.XvrlSeverityType;
import org.kosit.xvrl.model.XvrlValidatorType;
import org.kosit.xvrl.model.XvrlValidityType;

public class XvrlReportBuilder {

    final XvrlReportType xvrlReport;

    public static XvrlReportBuilder builder(final String name) {
        return builder(new ActionMetadata(name, name));
    }

    public static XvrlReportBuilder builder(final ActionMetadata metadata) {
        final XvrlReportBuilder builder = new XvrlReportBuilder();
        builder.name(metadata.name());
        builder.id(metadata.id());
        return builder;
    }

    private XvrlReportBuilder() {
        this.xvrlReport = new XvrlReportType();
        this.xvrlReport.setDigest(new XvrlDigestType());
        this.xvrlReport.setMetadata(new XvrlMetadataType());
    }

    private @NonNull XvrlValidityType calcValidity() {
        return this.xvrlReport.getDetection().stream().filter(XvrlDetectionType::hasErrors).findAny().map(e -> XvrlValidityType.FALSE)
                .orElse(XvrlValidityType.TRUE);
    }

    @Nullable
    private Long countDetections(final @NonNull XvrlSeverityType severity) {
        // Only values > 0 are emitted
        final long count = this.xvrlReport.getDetection().stream().filter(e -> e.getSeverity() == severity).count();
        return count == 0 ? null : Long.valueOf(count);
    }

    private XvrlValidatorType assertValidatorExistance() {
        final var vals = this.xvrlReport.getMetadata().getValidators();
        if (vals.isEmpty()) {
            vals.add(new XvrlValidatorType());
        }
        return vals.getFirst();
    }

    public XvrlReportBuilder name(final String name) {
        assertValidatorExistance().setName(name);
        return this;
    }

    private XvrlReportBuilder id(final String id) {
        assertValidatorExistance().setId(id);
        this.xvrlReport.setId(id);
        return this;
    }

    public XvrlReportBuilder setValid() {
        return setValid(XvrlValidityType.TRUE);
    }

    public XvrlReportBuilder setValid(final XvrlValidityType isValid) {
        this.xvrlReport.getDigest().setValid(isValid);
        return this;
    }

    public XvrlReportBuilder addSchema(final ResourceType schema) {
        xvrlReport.getMetadata().getSchemas().add(XvrlHelper.createSchema(schema.getLocation(), schema.getName()));
        return this;
    }

    public XvrlReportBuilder addSchemas(final List<ResourceType> resources) {
        for (final var resource : resources)
            addSchema(resource);
        return this;
    }

    public XvrlReportBuilder addDocumentIdentification(final String documentReference) {
        final XvrlDocumentType document = new XvrlDocumentType();
        document.setHref(documentReference);
        xvrlReport.getMetadata().getDocuments().add(document);
        return this;
    }

    public XvrlReportBuilder addDetections(final Stream<XvrlDetectionBuilder> collect) {
        collect.forEach(this::addDetection);
        return this;
    }

    public XvrlReportBuilder addDetection(final XvrlDetectionBuilder detection) {
        if (detection != null) {
            addDetection(detection.build());
        }
        return this;
    }

    private XvrlReportBuilder addDetection(final XvrlDetectionType build) {
        if (build != null) {
            this.xvrlReport.getDetection().add(build);
        }
        return this;
    }

    public XvrlReportType build() {
        final XvrlDigestType digest = xvrlReport.getDigest();
        digest.setFatalErrorCount(countDetections(XvrlSeverityType.FATAL_ERROR));
        digest.setErrorCount(countDetections(XvrlSeverityType.ERROR));
        digest.setWarningCount(countDetections(XvrlSeverityType.WARNING));
        digest.setInfoCount(countDetections(XvrlSeverityType.INFO));
        digest.setUnspecifiedCount(countDetections(XvrlSeverityType.UNSPECIFIED));

        // Don't overwrite manual validity state
        if (digest.getValid() == null)
            digest.setValid(calcValidity());

        return this.xvrlReport;
    }
}
