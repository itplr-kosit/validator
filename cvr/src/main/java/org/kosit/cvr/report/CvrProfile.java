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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionList;
import org.jspecify.annotations.NonNull;
import org.kosit.base.xml.SchemaResolver;
import org.kosit.conformatron.detection.Detection;
import org.kosit.conformatron.detection.DetectionList;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.schematron.SchematronCompilerRegistry;
import org.kosit.schematron.compiler.SchXslt2Compiler;
import org.kosit.schematron.util.SvrlDetections;
import org.kosit.svrl.impl.SvrlConverter;
import org.kosit.xvrl.impl.XvrlConverter;
import org.kost.validator.api.error.DetailedValidationResult;
import org.kost.validator.api.saxon.ProcessorProvider;
import org.kost.validator.api.xml.CollectingSaxErrorHandler;
import org.kost.validator.api.xml.XmlParser;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * CVR - <b>Conformance Validation Report</b> - is the XVRL profile of the validator. This class is the profile: it
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

    /** Detection code emitted when the profile Schematron itself could not be applied. */
    public static final String CODE_RULE_ENGINE_ERROR = "rule-engine-error";

    /** D1: draft namespace of the CVR extension vocabulary. */
    public static final String NS_CVR = "urn:conformatron:cvr:draft";

    /** Classpath location of the schema typing the CVR extension vocabulary. */
    public static final String CVR_XSD_PATH = "/xsd/cvr-1.0.xsd";

    /** Classpath location of the Schematron carrying the profile constraints. */
    public static final String CVR_SCH_PATH = "/sch/cvr-1.0.sch";

    private static final Logger LOGGER = LoggerFactory.getLogger(CvrProfile.class);

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

    public static @NonNull Source createCvrSchemaSource() {
        return SchemaResolver.resolve(resource(CVR_XSD_PATH));
    }

    public static @NonNull Schema createCvrSchema() {
        // CVR before XVRL
        return SchemaResolver.createParsedSchema(new Source[] { createCvrSchemaSource(), XvrlConverter.getXvrlSchemaSource() });
    }

    /**
     * @return the URI of the profile Schematron, which lives inside the jar of this module at runtime. Never
     *         {@code null}.
     */
    public static @NonNull URI getCvrSchematronUri() {
        return uriOf(CVR_SCH_PATH);
    }

    /**
     * The profile Schematron, transpiled to XSLT on first use. Transpiling dominates the runtime of a check, and the
     * result is a reusable read-only DOM - the same caching {@code ContentRepository} does for scenario artifacts.
     */
    private static final class SchematronXsltHolder {

        static final Source XSLT = transpileCvrSchematron();
    }

    /**
     * @return the profile Schematron as an XSLT stylesheet producing SVRL. Never {@code null}.
     */
    private static @NonNull Source transpileCvrSchematron() {
        final var compilerRegistry = SchematronCompilerRegistry.defaultSchematronCompilerRegistry(ProcessorProvider.getProcessor());
        final var compiler = compilerRegistry.get(SchXslt2Compiler.COMPILER_ID);
        // the profile Schematron is self contained, so the resolver only ever hands out the one resource
        return compiler.compileToXslt(getCvrSchematronUri(), _ -> new StreamSource(CvrProfile.class.getResourceAsStream(CVR_SCH_PATH)));
    }

    /**
     * Applies the profile Schematron to the report and maps the SVRL output to detections - the assertion id becomes
     * the detection code, so a violation names the rule it violated.
     *
     * @param processor the Saxon processor running the profile Schematron
     * @param xmlInstanceRes XML resource to read
     * @return the violations in document order; empty when the report satisfies the profile
     * @throws SaxonApiException if the rule engine fails
     * @throws IOException if the report can not be read again
     */
    private static @NonNull CTDetectionList validateAgainstSchematron(final @NonNull Processor processor,
            final @NonNull ReadResource xmlInstanceRes) throws SaxonApiException, IOException {
        final XsltTransformer transformer = processor.newXsltCompiler().compile(SchematronXsltHolder.XSLT).load();
        final XdmDestination destination = new XdmDestination();
        transformer.setDestination(destination);
        transformer.setSource(xmlInstanceRes.getAsSource());
        transformer.transform();

        final SchematronOutputType svrl = new SvrlConverter()
                .readXml(new DOMSource(NodeOverNodeInfo.wrap(destination.getXdmNode().getUnderlyingNode()).getOwnerDocument()));
        return SvrlDetections.toDetections(svrl, xmlInstanceRes.getName());
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
            // 1. Parse to DOM
            final AtomicReference<XdmNode> docHolder = new AtomicReference<>();
            final var parseDetections = XmlParser.parseXdmNode(xmlInstanceRes, docHolder::set);
            if (parseDetections.containsAtLeastOneError())
                return new DetailedValidationResult(parseDetections, null, null);

            // 2. Do schema validation
            final var xsdDetections = validateAgainstXmlSchema(xmlInstanceRes.getName(), docHolder.get());
            if (xsdDetections.containsAtLeastOneError())
                return new DetailedValidationResult(parseDetections, xsdDetections, null);

            // 3. continue with Schematron
            final CTDetectionList schematronDetections;
            try {
                schematronDetections = validateAgainstSchematron(processor, xmlInstanceRes);
            } catch (final SaxonApiException | RuntimeException e) {
                // the profile itself could not be applied - that is not a statement about the report
                LOGGER.error("Failed to apply the CVR profile Schematron to '" + xmlInstanceRes.getName() + "'", e);
                return new DetailedValidationResult(parseDetections, xsdDetections,
                        new DetectionList(Detection.builderError().code(CODE_RULE_ENGINE_ERROR).location(xmlInstanceRes.getName())
                                .text("The CVR profile Schematron could not be applied: " + e.getMessage()).linkedException(e).build()));
            }
            return new DetailedValidationResult(parseDetections, xsdDetections, schematronDetections);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Validates the report against the XML schema, listing every violation rather than only the first - one structural
     * mistake usually produces several, and seeing them together is what makes them fixable.
     *
     * @param resourceId the name the report is reported under
     * @param xdmNode the node to read
     * @return the violations in document order; empty when the report is valid XVRL
     */
    public static @NonNull CTDetectionList validateAgainstXmlSchema(@NonNull final String resourceId, @NonNull final XdmNode xdmNode) {
        final List<CTDetection> violations = new ArrayList<>();
        final Validator validator = createCvrSchema().newValidator();
        validator.setErrorHandler(new CollectingSaxErrorHandler(resourceId, violations));
        try {
            // a Saxon NodeSource is not accepted by JAXP, so hand over the read-only DOM view of the same tree
            validator.validate(new DOMSource(NodeOverNodeInfo.wrap(xdmNode.getUnderlyingNode())));
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
