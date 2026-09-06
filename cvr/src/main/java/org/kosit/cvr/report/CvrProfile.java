package org.kosit.cvr.report;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.jspecify.annotations.NonNull;
import org.kosit.base.uri.UriHelper;
import org.kosit.base.xml.SchemaResolver;
import org.kosit.conformatron.detection.Detection;
import org.kosit.conformatron.detection.DetectionList;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.xvrl.impl.XvrlConverter;
import org.kost.validator.api.error.DetailedValidationResult;
import org.kost.validator.api.saxon.ProcessorProvider;
import org.kost.validator.api.xml.CollectingSaxErrorHandler;
import org.kost.validator.api.xml.XmlParser;
import org.w3c.dom.Document;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;

/**
 * CVR - <b>Conformatron Validation Result</b> - is the XVRL profile of the validator. This class is the profile: it
 * locates its two artifacts and checks a report against both of them.
 * <p>
 * A report is checked in two steps, because the two questions are different:
 * </p>
 * <ol>
 * <li><b>Is it XVRL?</b> - answered by {@code xvrl-1.0.xsd}, the schema of the format CVR is a profile of.</li>
 * <li><b>Is it CVR?</b> - answered by {@code sch/cvr.sch}: the canonical pipeline steps a report is built from, the
 * extension vocabulary it may use, and the internal consistency the producer guarantees. Those constraints are not
 * expressible in XML Schema, because XVRL admits foreign attributes with {@code processContents="skip"} and because
 * most of them relate one part of the report to another.</li>
 * </ol>
 * <p>
 * The vocabulary itself is typed by {@code xsd/cvr.xsd}, which states the value spaces of the extension attributes for
 * tooling and for the reader.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class CvrProfile {

    /** D1: draft namespace of the CVR extension vocabulary. */
    public static final String NS_CVR = "urn:conformatron:cvr:draft";

    /** Classpath location of the schema typing the CVR extension vocabulary. */
    public static final String CVR_XSD_PATH = "/xsd/cvr-1.0.xsd";

    /** Classpath location of the Schematron carrying the profile constraints. */
    public static final String CVR_SCH_PATH = "/sch/cvr-1.0.sch";

    private static @NonNull URL resource(final String path) {
        final URL ret = CvrProfile.class.getResource(path);
        if (ret == null) {
            throw new IllegalStateException("The CVR profile is incomplete: '" + path + "' is not on the classpath");
        }
        return ret;
    }

    /**
     * @param path the classpath location
     * @return the URI of a profile artifact. Never {@code null}.
     */
    private static URI uriOf(final String path) {
        try {
            return resource(path).toURI();
        } catch (final URISyntaxException e) {
            throw new IllegalStateException("Can not address '" + path + "' as a URI", e);
        }
    }

    /**
     * @return the URI of the profile Schematron, which lives inside the jar of this module at runtime. Never
     *         {@code null}.
     */
    public static @NonNull URI getSchematronUri() {
        return uriOf(CVR_SCH_PATH);
    }

    public static @NonNull Schema createCvrXmlSchema() {
        // CVR before XVRL
        return SchemaResolver
                .createParsedSchema(new Source[] { SchemaResolver.resolve(resource(CVR_XSD_PATH)), XvrlConverter.getXvrlSchemaSource() });
    }

    /**
     * Checks the report against the XVRL schema and the profile Schematron, using the shared secured Saxon processor.
     *
     * @param xmlInstanceRes XML resource to read
     * @return the outcome, never {@code null}
     */
    public static @NonNull DetailedValidationResult validate(final @NonNull ReadResource xmlInstanceRes) {
        return validate(ProcessorProvider.getProcessor(), xmlInstanceRes);
    }

    /**
     * Checks the report against the XVRL schema and the profile Schematron.
     *
     * @param processor the Saxon processor running the profile Schematron
     * @param xmlInstanceRes XML resource to read
     * @return the outcome, never {@code null}
     */
    public static @NonNull DetailedValidationResult validate(final @NonNull Processor processor,
            final @NonNull ReadResource xmlInstanceRes) {
        try {
            // Parse to DOM
            AtomicReference<Document> docHolder = new AtomicReference<>();
            var parseDetections = XmlParser.parseXdmNode(xmlInstanceRes, null);
            if (parseDetections.containsAtLeastOneError())
                return new DetailedValidationResult(parseDetections, null, null);

            // Start with XSD
            var xsdDetections = validateAgainstXmlSchema(xmlInstanceRes);
            if (xsdDetections.containsAtLeastOneError())
                return new DetailedValidationResult(parseDetections, xsdDetections, null);

            // Now continue with Schematron
            final URI schematron = getSchematronUri();
            final AdHocValidationResult ret = new SchematronValidation(processor).validate(xmlInstanceRes, schematron,
                    UriHelper.isArchiveUri(schematron));
            // TODO
            return new DetectionList(detections);

        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Validates the report against the XML schema, listing every violation rather than only the first - one structural
     * mistake usually produces several, and seeing them together is what makes them fixable.
     *
     * @param xdmNode the node to read
     * @return the violations in document order; empty when the report is valid XVRL
     */
    public static @NonNull CTDetectionList validateAgainstXmlSchema(@NonNull String resourceId, @NonNull XdmNode xdmNode) {
        final List<CTDetection> violations = new ArrayList<>();
        final Validator validator = createCvrXmlSchema().newValidator();
        validator.setErrorHandler(new CollectingSaxErrorHandler(resourceId, violations));
        try {
            validator.validate();
        } catch (final Exception e) {
            violations.add(Detection.builderError().location(resourceId).text("XML schema exception: " + e.getMessage()).linkedException(e)
                    .build());
        }
        return new DetectionList(violations);
    }

    private CvrProfile() {
        // static utility
    }

}
