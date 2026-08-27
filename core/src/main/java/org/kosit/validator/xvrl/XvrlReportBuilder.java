package org.kosit.validator.xvrl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.kosit.validator.impl.ActionMetadata;
import org.kosit.validator.scenario.v1.ResourceType;
import org.kosit.xvrl.model.XvrlDetectionType;
import org.kosit.xvrl.model.XvrlDigestType;
import org.kosit.xvrl.model.XvrlDocumentType;
import org.kosit.xvrl.model.XvrlMetadataType;
import org.kosit.xvrl.model.XvrlReportType;
import org.kosit.xvrl.model.XvrlSchemaType;
import org.kosit.xvrl.model.XvrlSeverityType;
import org.kosit.xvrl.model.XvrlValidatorType;

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

    private @NonNull Boolean calcValidity() {
        return this.xvrlReport.getDetection().stream().filter(XvrlDetectionType::hasErrors).findAny().map(e -> Boolean.FALSE).orElse(Boolean.TRUE);
    }

    private long countDetections(final @NonNull XvrlSeverityType severity) {
        return this.xvrlReport.getDetection().stream().filter(e -> e.getSeverity() == severity).count();
    }

    public XvrlReportBuilder name(final String name) {
        assertValidatorExistance();
        this.xvrlReport.getMetadata().getValidators().get(0).setName(name);
        return this;
    }

    private void assertValidatorExistance() {
        if (this.xvrlReport.getMetadata().getValidators().isEmpty()) {
            final XvrlValidatorType validator = new XvrlValidatorType();
            this.xvrlReport.getMetadata().getValidators().add(validator);
        }
    }

    private XvrlReportBuilder id(final String id) {
        assertValidatorExistance();
        this.xvrlReport.getMetadata().getValidators().get(0).setId(id);
        this.xvrlReport.setId(id);
        return this;
    }

    public XvrlReportBuilder setValid() {
        setValid("true");
        return this;
    }

    public XvrlReportBuilder setValid(final String isValid) {
        this.xvrlReport.getDigest().setValid(isValid);
        return this;
    }

    public XvrlReportBuilder addSchema(final ResourceType schema) {
        return addSchemas(Collections.singletonList(schema));
    }

    public XvrlReportBuilder addSchemas(final List<ResourceType> resources) {
        final List<XvrlSchemaType> schemas = resources.stream().map(resourceType -> {
            final XvrlSchemaType schema = new XvrlSchemaType();
            schema.setHref(resourceType.getLocation());
            schema.setSchematypens(resourceType.getName());
            return schema;
        }).toList();
        this.xvrlReport.getMetadata().getSchemas().addAll(schemas);
        return this;
    }

    public XvrlReportBuilder setErrorCount(final long errorCount) {
        this.xvrlReport.getDigest().setErrorCount(errorCount);
        return this;
    }

    public XvrlReportBuilder setFatalErrorCount(final long errorCount) {
        this.xvrlReport.getDigest().setFatalErrorCount(errorCount);
        return this;
    }

    public XvrlReportBuilder addDocumentIdentification(final String documentReference) {
        final XvrlDocumentType document = new XvrlDocumentType();
        document.setHref(documentReference);
        this.xvrlReport.getMetadata().getDocuments().add(document);
        return this;
    }

    public XvrlReportBuilder add(final XvrlDetectionBuilder detection) {
        if (detection != null) {
            add(detection.build());
        }
        return this;
    }

    private XvrlReportBuilder add(final XvrlDetectionType build) {
        if (build != null) {
            this.xvrlReport.getDetection().add(build);
        }
        return this;
    }

    public XvrlReportBuilder addAll(final Stream<XvrlDetectionBuilder> collect) {
        collect.forEach(this::add);
        return this;
    }

    public XvrlReportBuilder addAll(final List<XvrlDetectionBuilder> collect) {
        return addAll(collect.stream());
    }

    public XvrlReportType build() {
        final XvrlDigestType digest = new XvrlDigestType();
        digest.setErrorCount(countDetections(XvrlSeverityType.ERROR));
        digest.setFatalErrorCount(countDetections(XvrlSeverityType.FATAL_ERROR));
        digest.setInfoCount(countDetections(XvrlSeverityType.INFO));
        digest.setValid(calcValidity().toString());
        this.xvrlReport.setDigest(digest);
        return this.xvrlReport;
    }
}
