package org.kosit.cvr.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.jspecify.annotations.NonNull;
import org.kosit.base.uri.UriHelper;
import org.kosit.base.xml.SchemaResolver;
import org.kosit.cvr.source.ReadResource;
import org.kosit.cvr.source.Resource;
import org.kosit.schematron.SchematronValidation;
import org.kosit.schematron.SchematronValidation.AdHocValidationResult;
import org.kosit.schematron.saxon.ProcessorProvider;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import net.sf.saxon.s9api.Processor;

/**
 * CVRL - <b>Conformatron Validation Result</b> - is the XVRL profile of the validator. This class is the profile: it
 * locates its two artifacts and checks a report against both of them.
 * <p>
 * A report is checked in two steps, because the two questions are different:
 * </p>
 * <ol>
 * <li><b>Is it XVRL?</b> - answered by {@code xvrl-1.0.xsd}, the schema of the format CVRL is a profile of.</li>
 * <li><b>Is it CVRL?</b> - answered by {@code sch/cvrl.sch}: the canonical pipeline steps a report is built from, the
 * extension vocabulary it may use, and the internal consistency the producer guarantees. Those constraints are not
 * expressible in XML Schema, because XVRL admits foreign attributes with {@code processContents="skip"} and because
 * most of them relate one part of the report to another.</li>
 * </ol>
 * <p>
 * The vocabulary itself is typed by {@code xsd/cvrl.xsd}, which states the value spaces of the extension attributes for
 * tooling and for the reader.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class CvrlProfile {

    /** XVRL namespace - CVRL is a profile of XVRL, so a CVRL report is an XVRL report. */
    public static final String NS_XVRL = "http://www.xproc.org/ns/xvrl";

    /** D1: draft namespace of the CVRL extension vocabulary. */
    public static final String NS_CVRL = "urn:conformatron:cvrl:draft";

    /** Classpath location of the schema typing the CVRL extension vocabulary. */
    public static final String CVRL_XSD_PATH = "/xsd/cvrl.xsd";

    /** Classpath location of the Schematron carrying the profile constraints. */
    public static final String CVRL_SCH_PATH = "/sch/cvrl.sch";

    /** Classpath location of the XVRL schema a CVRL report is validated against structurally. */
    public static final String XVRL_XSD_PATH = "/xsd/xvrl-1.0.xsd";

    private static final class SchemaHolder {

        static final Schema XVRL = SchemaResolver.createParsedSchema(resource(XVRL_XSD_PATH));
    }

    private CvrlProfile() {
        // static utility
    }

    private static URL resource(final String path) {
        final URL ret = CvrlProfile.class.getResource(path);
        if (ret == null) {
            throw new IllegalStateException("The CVRL profile is incomplete: '" + path + "' is not on the classpath");
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
     * @return the XVRL schema, parsed once. Never {@code null}.
     */
    public static @NonNull Schema getXvrlSchema() {
        return SchemaHolder.XVRL;
    }

    /**
     * @return the URI of the profile Schematron, which lives inside the jar of this module at runtime. Never
     *         {@code null}.
     */
    public static @NonNull URI getSchematronUri() {
        return uriOf(CVRL_SCH_PATH);
    }

    /**
     * The outcome of checking a report against the profile.
     *
     * @param schemaViolations what {@code xvrl-1.0.xsd} rejected; empty when the report is well formed XVRL
     * @param profile what the profile Schematron reported, see {@link AdHocValidationResult#isConformant()}
     */
    public record CvrlValidationResult(List<String> schemaViolations, AdHocValidationResult profile) {

        /**
         * @return {@code true} when the report is XVRL and satisfies the profile
         */
        public boolean isValid() {
            return this.schemaViolations.isEmpty() && this.profile.isConformant();
        }

        /**
         * @return every violation of either check, as readable text; empty when the report is valid
         */
        public @NonNull List<String> getViolations() {
            final List<String> ret = new ArrayList<>(this.schemaViolations);
            this.profile.detections().getAll().stream().filter(d -> d.getSeverity().isError())
                    .forEach(d -> ret.add(d.getCode() + ": " + d.getText().getDisplayTextLocaleIndependent()));
            return ret;
        }
    }

    /**
     * Checks the report against the XVRL schema and the profile Schematron, using the shared secured Saxon processor.
     *
     * @param name the name the report is reported under
     * @param report the serialized CVRL report
     * @return the outcome, never {@code null}
     */
    public static @NonNull CvrlValidationResult validate(final @NonNull String name, final byte @NonNull [] report) {
        return validate(ProcessorProvider.getProcessor(), name, report);
    }

    /**
     * Checks the report against the XVRL schema and the profile Schematron.
     *
     * @param processor the Saxon processor running the profile Schematron
     * @param name the name the report is reported under
     * @param report the serialized CVRL report
     * @return the outcome, never {@code null}
     */
    public static @NonNull CvrlValidationResult validate(final @NonNull Processor processor, final @NonNull String name,
            final byte @NonNull [] report) {
        final URI schematron = getSchematronUri();
        try {
            final AdHocValidationResult ret = new SchematronValidation(processor).validate(ReadResource.inMemory(Resource.of(name, report)),
                    schematron, UriHelper.isArchiveUri(schematron));
            return new CvrlValidationResult(validateAgainstXvrl(report), ret);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Validates the report against the XVRL schema, listing every violation rather than only the first - one structural
     * mistake usually produces several, and seeing them together is what makes them fixable.
     *
     * @param report the serialized CVRL report
     * @return the violations in document order; empty when the report is valid XVRL
     */
    public static @NonNull List<String> validateAgainstXvrl(final byte @NonNull [] report) {
        final List<String> violations = new ArrayList<>();
        final Validator validator = getXvrlSchema().newValidator();
        validator.setErrorHandler(new ErrorHandler() {

            @Override
            public void warning(final SAXParseException e) {
                // a warning does not make a document invalid
            }

            @Override
            public void error(final SAXParseException e) {
                violations.add("line " + e.getLineNumber() + ": " + e.getMessage());
            }

            @Override
            public void fatalError(final SAXParseException e) {
                violations.add("line " + e.getLineNumber() + " (fatal): " + e.getMessage());
            }
        });
        try {
            validator.validate(new StreamSource(new ByteArrayInputStream(report)));
        } catch (final Exception e) {
            violations.add("validation aborted: " + e.getMessage());
        }
        return violations;
    }
}
