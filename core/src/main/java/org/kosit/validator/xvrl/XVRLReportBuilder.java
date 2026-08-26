package org.kosit.validator.xvrl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.kosit.validator.impl.ActionMetadata;
import org.kosit.validator.scenario.v1.ResourceType;
import org.kosit.xvrl.model.XVRLDetectionType;
import org.kosit.xvrl.model.XVRLDigestType;
import org.kosit.xvrl.model.XVRLDocumentType;
import org.kosit.xvrl.model.XVRLMetadataType;
import org.kosit.xvrl.model.XVRLReportType;
import org.kosit.xvrl.model.XVRLSchemaType;
import org.kosit.xvrl.model.XVRLValidatorType;

public class XVRLReportBuilder {

    final XVRLReportType xvrlReport;

    public static XVRLReportBuilder builder(final String name) {
        return builder(new ActionMetadata(name, name));
    }

    public static XVRLReportBuilder builder(final ActionMetadata metadata) {
        final XVRLReportBuilder builder = new XVRLReportBuilder();
        builder.name(metadata.name());
        builder.id(metadata.id());
        return builder;
    }

    private XVRLReportBuilder() {
        this.xvrlReport = new XVRLReportType();
        this.xvrlReport.setDigest(new XVRLDigestType());
        this.xvrlReport.setMetadata(new XVRLMetadataType());
    }

    private String calcValidity() {
        return this.xvrlReport.getDetection().stream()
                .filter(e -> ArrayUtils.contains(
                        new XVRLDetectionType.Severity[] { XVRLDetectionType.Severity.ERROR, XVRLDetectionType.Severity.FATAL_ERROR },
                        e.getSeverity()))
                .findAny().map(e -> "false").orElse("true");
    }

    private long countDetections(final XVRLDetectionType.Severity severity) {
        return this.xvrlReport.getDetection().stream().filter(e -> e.getSeverity() == severity).count();
    }

    public XVRLReportBuilder name(final String name) {
        assertValidatorExistance();
        this.xvrlReport.getMetadata().getValidators().get(0).setName(name);
        return this;
    }

    private void assertValidatorExistance() {
        if (this.xvrlReport.getMetadata().getValidators().isEmpty()) {
            final XVRLValidatorType validator = new XVRLValidatorType();
            this.xvrlReport.getMetadata().getValidators().add(validator);
        }
    }

    private XVRLReportBuilder id(final String id) {
        assertValidatorExistance();
        this.xvrlReport.getMetadata().getValidators().get(0).setId(id);
        this.xvrlReport.setId(id);
        return this;
    }

    public XVRLReportBuilder setValid() {
        setValid("true");
        return this;
    }

    public XVRLReportBuilder setValid(final String isValid) {
        this.xvrlReport.getDigest().setValid(isValid);
        return this;
    }

    public XVRLReportBuilder addSchema(final ResourceType schema) {
        return addSchemas(Collections.singletonList(schema));
    }

    public XVRLReportBuilder addSchemas(final List<ResourceType> resources) {
        final List<XVRLSchemaType> schemas = resources.stream().map(resourceType -> {
            final XVRLSchemaType schema = new XVRLSchemaType();
            schema.setHref(resourceType.getLocation());
            schema.setSchematypens(resourceType.getName());
            return schema;
        }).toList();
        this.xvrlReport.getMetadata().getSchemas().addAll(schemas);
        return this;
    }

    public XVRLReportBuilder setErrorCount(final long errorCount) {
        this.xvrlReport.getDigest().setErrorCount(errorCount);
        return this;
    }

    public XVRLReportBuilder setFatalErrorCount(final long errorCount) {
        this.xvrlReport.getDigest().setFatalErrorCount(errorCount);
        return this;
    }

    public XVRLReportBuilder addDocumentIdentification(final String documentReference) {
        final XVRLDocumentType document = new XVRLDocumentType();
        document.setHref(documentReference);
        this.xvrlReport.getMetadata().getDocuments().add(document);
        return this;
    }

    public XVRLReportBuilder add(final XvrlDetectionBuilder detection) {
        if (detection != null) {
            add(detection.build());
        }
        return this;
    }

    private XVRLReportBuilder add(final XVRLDetectionType build) {
        if (build != null) {
            this.xvrlReport.getDetection().add(build);
        }
        return this;
    }

    public XVRLReportBuilder addAll(final Stream<XvrlDetectionBuilder> collect) {
        collect.forEach(this::add);
        return this;
    }

    public XVRLReportBuilder addAll(final List<XvrlDetectionBuilder> collect) {
        return addAll(collect.stream());
    }

    public XVRLReportType build() {
        final XVRLDigestType digest = new XVRLDigestType();
        digest.setErrorCount(countDetections(XVRLDetectionType.Severity.ERROR));
        digest.setFatalErrorCount(countDetections(XVRLDetectionType.Severity.FATAL_ERROR));
        digest.setInfoCount(countDetections(XVRLDetectionType.Severity.INFO));
        digest.setValid(calcValidity());
        this.xvrlReport.setDigest(digest);
        return this.xvrlReport;
    }
}
