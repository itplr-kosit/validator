package org.kosit.validator.impl.xvrl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.kosit.validator.impl.ActionMetadata;
import org.kosit.validator.scenario.v1.ResourceType;
import org.kosit.xvrl.model.Document;
import org.kosit.xvrl.model.Schema;
import org.kosit.xvrl.model.Validator;
import org.kosit.xvrl.model.XVRLDetection;
import org.kosit.xvrl.model.XVRLDigest;
import org.kosit.xvrl.model.XVRLMetadata;
import org.kosit.xvrl.model.XVRLReport;

public class XVRLReportBuilder {

    final XVRLReport xvrlReport;

    private XVRLReportBuilder() {
        this.xvrlReport = new XVRLReport();
        this.xvrlReport.setDigest(new XVRLDigest());
        this.xvrlReport.setMetadata(new XVRLMetadata());
    }

    public static XVRLReportBuilder builder(final String name) {
        return builder(new ActionMetadata(name, name));
    }

    public static XVRLReportBuilder builder(final ActionMetadata metadata) {
        final XVRLReportBuilder builder = new XVRLReportBuilder();
        builder.name(metadata.getName());
        builder.id(metadata.getId());
        return builder;
    }

    public static XvrlDetectionBuilder detectionBuilder() {
        return new XvrlDetectionBuilder();
    }

    public static XvrlSupplementalBuilder supplemental() {
        return new XvrlSupplementalBuilder();
    }

    public XVRLReport build() {
        final XVRLDigest digest = new XVRLDigest();
        digest.setErrorCount(countDetections(XVRLDetection.Severity.ERROR));
        digest.setFatalErrorCount(countDetections(XVRLDetection.Severity.FATAL_ERROR));
        digest.setInfoCount(countDetections(XVRLDetection.Severity.INFO));
        digest.setValid(calcValidity());
        this.xvrlReport.setDigest(digest);
        return this.xvrlReport;
    }

    private String calcValidity() {
        return this.xvrlReport.getDetection().stream()
                .filter(e -> ArrayUtils.contains(
                        new XVRLDetection.Severity[] { XVRLDetection.Severity.ERROR, XVRLDetection.Severity.FATAL_ERROR }, e.getSeverity()))
                .findAny().map(e -> "false").orElse("true");
    }

    private long countDetections(final XVRLDetection.Severity severity) {
        return this.xvrlReport.getDetection().stream().filter(e -> e.getSeverity() == severity).count();
    }

    public XVRLReportBuilder name(final String name) {
        assertValidatorExistance();
        this.xvrlReport.getMetadata().getValidators().get(0).setName(name);
        return this;
    }

    private void assertValidatorExistance() {
        if (this.xvrlReport.getMetadata().getValidators().isEmpty()) {
            final Validator validator = new Validator();
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
        final List<Schema> schemas = resources.stream().map(resourceType -> {
            final Schema schema = new Schema();
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
        final Document document = new Document();
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

    private XVRLReportBuilder add(final XVRLDetection build) {
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
}
