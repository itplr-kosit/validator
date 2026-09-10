package org.kosit.validator.impl.conformatron.report;

import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.util.Locale;

import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.cvr.report.CvrProfile;

/**
 * Checks a generated report against the CVR profile of {@code validator-cvr} - the XVRL schema for the structure, the
 * profile Schematron for everything that makes an XVRL report a CVR report.
 * <p>
 * The ways to get this wrong are quiet ones: a required attribute we never knew about, an element in the wrong order,
 * an invented extension attribute, a digest that no longer matches the detections it summarises. Reading the schema and
 * the rules by hand finds those late; this finds them on every test run.
 * </p>
 */
public final class CvrAssert {

    private CvrAssert() {
        // static utility
    }

    /**
     * Fails the calling test when the report violates the profile, listing every violation rather than only the first -
     * one structural mistake usually produces several, and seeing them together is what makes them fixable.
     *
     * @param name the name the report is reported under
     * @param cvr the serialized report
     */
    public static void assertValidCvr(final String name, final byte[] cvr) {
        try {
            final var cvrRes = ReadResource.inMemory(Resource.of(name, cvr));

            final var valResult = CvrProfile.validate(cvrRes);
            if (valResult.containsNoError()) {
                return;
            }
            fail("The report does not satisfy the CVR profile:\n  "
                    + String.join("\n", valResult.getMergedDetections().stream().map(x -> x.getAsString(Locale.ROOT)).toList()));
        } catch (final IOException ex) {
            fail("IOException in reading resource", ex);
        }
    }
}
