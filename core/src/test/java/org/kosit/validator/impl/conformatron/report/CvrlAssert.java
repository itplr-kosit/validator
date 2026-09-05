package org.kosit.validator.impl.conformatron.report;

import static org.assertj.core.api.Assertions.fail;

import java.util.List;

import org.kosit.cvr.report.CvrlProfile;
import org.kosit.cvr.report.CvrlProfile.CvrlValidationResult;

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
     * @param cvrl the serialized report
     */
    static void assertValid(final String name, final byte[] cvrl) {
        final CvrlValidationResult result = CvrlProfile.validate(name, cvrl);
        if (result.isValid()) {
            return;
        }
        final List<String> violations = result.getViolations();
        fail("The report does not satisfy the CVRL profile:%n  %s", String.join(String.format("%n  "), violations));
    }
}
