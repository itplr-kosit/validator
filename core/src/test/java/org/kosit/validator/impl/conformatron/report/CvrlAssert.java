package org.kosit.validator.impl.conformatron.report;

import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.Locale;

import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.cvr.report.CvrProfile;

/**
 * Checks a generated report against the CVRL profile of {@code validator-cvr} - the XVRL schema for the structure, the
 * profile Schematron for everything that makes an XVRL report a CVRL report.
 * <p>
 * The ways to get this wrong are quiet ones: a required attribute we never knew about, an element in the wrong order,
 * an invented extension attribute, a digest that no longer matches the detections it summarises. Reading the schema and
 * the rules by hand finds those late; this finds them on every test run.
 * </p>
 */
final class CvrlAssert {

    private CvrlAssert() {
        // static utility
    }

    /**
     * Fails the calling test when the report violates the profile, listing every violation rather than only the first -
     * one structural mistake usually produces several, and seeing them together is what makes them fixable.
     *
     * @param name the name the report is reported under
     * @param cvr the serialized report
     */
    static void assertValid(final String name, final byte[] cvr) {
        try {
            final var cvrRes = ReadResource.inMemory(Resource.of(name, cvr));

            final var detections = CvrProfile.validate(cvrRes);
            if (detections.containsNoError()) {
                return;
            }
            fail("The report does not satisfy the CVRL profile:\n  "
                    + String.join("\n", detections.getAll().stream().map(x -> x.getAsString(Locale.ROOT)).toList()));
        } catch (final IOException ex) {
            fail("IOException in reading resource", ex);
        }
    }
}
