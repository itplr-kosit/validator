package org.kosit.validator.xvrl;

import java.util.List;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.validator.impl.ActionMetadata;
import org.kosit.validator.scenario.v1.ResourceType;
import org.kosit.xvrl.api.XvrlHelper;
import org.kosit.xvrl.model.XvrlDetection;
import org.kosit.xvrl.model.XvrlDigest;
import org.kosit.xvrl.model.XvrlDocument;
import org.kosit.xvrl.model.XvrlMetadata;
import org.kosit.xvrl.model.XvrlReport;
import org.kosit.xvrl.model.XvrlSeverity;
import org.kosit.xvrl.model.XvrlValidator;
import org.kosit.xvrl.model.XvrlValidity;

public class XvrlReportBuilder {

    private final XvrlReport.Builder xvrlReport = XvrlReport.builder();

    private final XvrlMetadata.Builder metadata = XvrlMetadata.builder();

    private final XvrlDigest.Builder digest = XvrlDigest.builder();

    private final XvrlValidator.Builder validator = XvrlValidator.builder();

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
    }

    private @NonNull XvrlValidity calcValidity() {
        return this.xvrlReport.getDetections().stream().anyMatch(XvrlDetection::hasErrors) ? XvrlValidity.FALSE : XvrlValidity.TRUE;
    }

    @Nullable
    private Long countDetections(final @NonNull XvrlSeverity severity) {
        // Only values > 0 are emitted
        final long count = this.xvrlReport.getDetections().stream().filter(e -> e.getSeverity() == severity).count();
        return count == 0 ? null : Long.valueOf(count);
    }

    public XvrlReportBuilder name(final String name) {
        this.validator.name(name);
        return this;
    }

    private XvrlReportBuilder id(final String id) {
        this.validator.id(id);
        this.xvrlReport.id(id);
        return this;
    }

    public XvrlReportBuilder setValid() {
        return setValid(XvrlValidity.TRUE);
    }

    public XvrlReportBuilder setValid(final XvrlValidity isValid) {
        this.digest.valid(isValid);
        return this;
    }

    public XvrlReportBuilder addSchema(final ResourceType schema) {
        this.metadata.addSchema(XvrlHelper.createSchema(schema.getLocation(), schema.getName()));
        return this;
    }

    public XvrlReportBuilder addSchemas(final List<ResourceType> resources) {
        for (final var resource : resources)
            addSchema(resource);
        return this;
    }

    public XvrlReportBuilder addDocumentIdentification(final String documentReference) {
        this.metadata.addDocument(XvrlDocument.builder(documentReference));
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

    private XvrlReportBuilder addDetection(final XvrlDetection build) {
        this.xvrlReport.addDetection(build);
        return this;
    }

    public XvrlReport build() {
        this.digest.fatalErrorCount(countDetections(XvrlSeverity.FATAL_ERROR));
        this.digest.errorCount(countDetections(XvrlSeverity.ERROR));
        this.digest.warningCount(countDetections(XvrlSeverity.WARNING));
        this.digest.infoCount(countDetections(XvrlSeverity.INFO));
        this.digest.unspecifiedCount(countDetections(XvrlSeverity.UNSPECIFIED));

        // Don't overwrite manual validity state
        if (this.digest.getValid() == null)
            this.digest.valid(calcValidity());

        return this.xvrlReport.metadata(this.metadata.validator(this.validator)).digest(this.digest).build();
    }
}
