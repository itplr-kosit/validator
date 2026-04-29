/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kosit.validator.impl.xvrl;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kosit.validator.api.XmlError.Severity.SEVERITY_FATAL_ERROR;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.kosit.validator.api.XmlError;
import org.kosit.validator.impl.ActionMetadata;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.validator.model.XMLSyntaxErrorSeverity;
import org.kosit.validator.model.scenarios.ResourceType;
import org.kosit.validator.model.xvrl.Document;
import org.kosit.validator.model.xvrl.Location;
import org.kosit.validator.model.xvrl.Schema;
import org.kosit.validator.model.xvrl.Supplemental;
import org.kosit.validator.model.xvrl.Validator;
import org.kosit.validator.model.xvrl.XVRLDetection;
import org.kosit.validator.model.xvrl.XVRLDigest;
import org.kosit.validator.model.xvrl.XVRLMessage;
import org.kosit.validator.model.xvrl.XVRLMetadata;
import org.kosit.validator.model.xvrl.XVRLReport;
import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;
import org.w3c.dom.Element;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.XdmNode;

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

    private static boolean isFatalError(final XMLSyntaxError xmlSyntaxError) {
        return xmlSyntaxError.getSeverityCode() == XMLSyntaxErrorSeverity.SEVERITY_FATAL_ERROR;
    }

    private static boolean isError(final XMLSyntaxError xmlSyntaxError) {
        return xmlSyntaxError.getSeverityCode() == XMLSyntaxErrorSeverity.SEVERITY_ERROR;
    }

    public static DetectionBuilder detection() {
        return new DetectionBuilder();
    }

    public static SupplementalBuilder supplemantal() {
        return new SupplementalBuilder();
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
        }).collect(Collectors.toList());
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

    public XVRLReportBuilder add(final DetectionBuilder detection) {
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

    public XVRLReportBuilder addAll(final Stream<DetectionBuilder> collect) {
        collect.forEach(this::add);
        return this;
    }

    public XVRLReportBuilder addAll(final List<DetectionBuilder> collect) {
        return addAll(collect.stream());
    }

    public static class DetectionBuilder {

        private final XVRLDetection detection = new XVRLDetection();

        private static XVRLDetection.Severity translate(final XmlError.Severity severity) {
            if (severity == SEVERITY_FATAL_ERROR) {
                return XVRLDetection.Severity.FATAL_ERROR;
            }
            return XVRLDetection.Severity.ERROR;

        }

        private static Location createLocation(final int line, final int row, final String xpath) {
            final Location location = new Location();
            location.setLine(Integer.valueOf(line).longValue());
            location.setColumn(Integer.valueOf(row).longValue());
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

        public DetectionBuilder add(final SupplementalBuilder addContent) {
            if (addContent != null) {
                add(addContent.build());
            }
            return this;
        }

        private DetectionBuilder add(final Supplemental build) {
            if (build != null) {
                this.detection.getSupplementals().add(build);
            }
            return this;
        }

        public DetectionBuilder addError(final String message) {
            addMessage(message);
            this.detection.setSeverity(XVRLDetection.Severity.ERROR);
            return this;
        }

        public DetectionBuilder addMessage(final String message) {
            if (isNotBlank(message)) {
                this.detection.getMessages().add(createMessage(message));
            }
            return this;
        }

        public DetectionBuilder addError(final XmlError error) {
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

        public DetectionBuilder add(final ActivePattern activePattern) {
            if (activePattern == null) {
                return this;
            }
            this.detection.setSeverity(XVRLDetection.Severity.INFO);
            this.detection.setCode(activePattern.getName());
            return this;
        }

        public DetectionBuilder add(final FiredRule firedRule) {
            if (firedRule == null) {
                return this;
            }
            this.detection.setSeverity(XVRLDetection.Severity.INFO);
            this.detection.setCode(firedRule.getName());
            return this;
        }

        public DetectionBuilder add(final FailedAssert failedAssert) {
            if (failedAssert == null) {
                return this;
            }

            this.detection.setSeverity(XVRLDetection.Severity.ERROR);
            this.detection.getMessages().add(getMessage(failedAssert));

            return this;
        }

        public XVRLDetection build() {
            if (this.detection.getSeverity() == null) {
                this.detection.setSeverity(XVRLDetection.Severity.INFO);
            }
            return this.detection;
        }

        public DetectionBuilder severity(final XVRLDetection.Severity info) {
            this.detection.setSeverity(info);
            return this;
        }

        public DetectionBuilder code(final String code) {
            if (isNotBlank(code)) {
                this.detection.setCode(code);
            }
            return this;
        }

        public DetectionBuilder id(final String id) {
            if (isNotBlank(id)) {
                this.detection.setId(id);
            }
            return this;
        }
    }

    public static class SupplementalBuilder {

        private final Supplemental sup = new Supplemental();

        public SupplementalBuilder id(final String id) {
            this.sup.setId(id);
            return this;
        }

        public SupplementalBuilder addContent(final XdmNode node) {
            if (node != null) {
                final Element node2element = NodeOverNodeInfo.wrap(node.getUnderlyingNode()).getOwnerDocument().getDocumentElement();
                this.sup.getContent().add(node2element);
            }
            return this;
        }

        public Supplemental build() {
            return this.sup;
        }
    }
}
