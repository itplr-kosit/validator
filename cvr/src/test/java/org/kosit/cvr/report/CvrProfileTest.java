package org.kosit.cvr.report;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.xvrl.impl.XvrlConverter;
import org.kost.validator.api.error.DetailedValidationResult;

/**
 * Tests the CVRL profile against hand written reports: the two shapes a run can produce (a cancelled and a completed
 * one) have to pass, and every rule has to catch the mistake it is there for. The fixtures are deliberately minimal -
 * each invalid one differs from a valid report in exactly the one respect its rule is about.
 */
public class CvrProfileTest {

    private static byte[] read(final String name) {
        try ( final InputStream input = CvrProfileTest.class.getResourceAsStream("/cvrl/" + name) ) {
            if (input == null) {
                throw new IllegalStateException("Test fixture '" + name + "' is not on the test classpath");
            }
            return input.readAllBytes();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static DetailedValidationResult validate(final String name) {
        return CvrProfile.validate(ReadResource.inMemoryUnchecked(Resource.of(name, read(name))));
    }

    @Test
    public void testTheProfileArtifactsAreOnTheClasspath() {
        assertThat(XvrlConverter.getXvrlSchema()).isNotNull();
        assertThat(CvrProfile.class.getResource(CvrProfile.CVR_XSD_PATH)).isNotNull();
        assertThat(CvrProfile.createCvrSchemaSource()).isNotNull();
        assertThat(CvrProfile.getCvrSchematronUri()).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({ "valid-cancelled.xml", "valid-completed.xml" })
    public void testAValidReportPassesBothChecks(final String fixture) {
        final DetailedValidationResult result = validate(fixture);

        assertThat(result.schemaDetections().isEmpty()).isTrue();
        assertThat(result.schematronDetections().getAllErrors()).isEmpty();
        assertThat(result.containsNoError()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({ "invalid-cancelled-but-conformant.xml, cancelled-is-not-conformant",
            "invalid-completed-without-conformance.xml, completed-has-conformance", "invalid-unknown-creator.xml, step-canonical-creator",
            "invalid-steps-out-of-order.xml, steps-in-pipeline-order", "invalid-digest-count-mismatch.xml, digest-error-count-matches",
            "invalid-unknown-cvrl-attribute.xml, known-cvr-attribute",
            "invalid-dom-payload-with-source-encoding.xml, payload-source-encoding",
            "invalid-schema-outside-apply-rules.xml, schema-only-on-apply-rules", "invalid-hash-outside-context.xml, hash-in-context" })
    public void testAProfileViolationIsCaughtByItsOwnRule(final String filename, final String assertionId) {
        final DetailedValidationResult result = validate(filename);

        // the fixtures are valid XVRL - what they violate is the profile, not the format
        assertThat(result.schemaDetections().isEmpty()).isTrue();
        assertThat(result.containsNoError()).isFalse();
        assertThat(result.schematronDetections().getAll()).extracting("code").contains(assertionId);
    }

    @Test
    public void testAReportThatIsNotXvrlIsRejectedByTheSchema() {
        final String notXvrl = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><nonsense/>";
        final DetailedValidationResult result = CvrProfile.validate(ReadResource.inMemoryUnchecked(Resource.utf8("not-xvrl.xml", notXvrl)));

        assertThat(result.schemaDetections().isEmpty()).isFalse();
        assertThat(result.containsNoError()).isFalse();
        // the schema already settled it, so the profile Schematron never ran
        assertThat(result.schematronDetections()).isNull();
    }
}
