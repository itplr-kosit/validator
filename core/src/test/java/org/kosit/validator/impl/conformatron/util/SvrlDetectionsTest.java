package org.kosit.validator.impl.conformatron.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.conformatron.api.model.detection.ECTSeverity;
import org.conformatron.api.model.detection.CTDetectionList;
import org.junit.jupiter.api.Test;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutput;

/**
 * Tests the SVRL severity mapping of {@link SvrlDetections}: {@code @role} takes precedence, {@code @flag} is the
 * fallback (XRechnung rules carry the level in {@code @flag} — regression from the XRechnung E2E run).
 */
public class SvrlDetectionsTest {

    private static SchematronOutput svrlWith(final String id, final String role, final String flag) {
        final FailedAssert failedAssert = new FailedAssert();
        failedAssert.setId(id);
        failedAssert.setRole(role);
        failedAssert.setFlag(flag);
        final SchematronOutput svrl = new SchematronOutput();
        svrl.getActivePatternAndFiredRuleAndFailedAssert().add(failedAssert);
        return svrl;
    }

    private static ECTSeverity severityOf(final SchematronOutput svrl) {
        final CTDetectionList detections = SvrlDetections.toDetections(svrl, "test.xml");
        return (ECTSeverity) detections.getAll().get(0).getSeverity();
    }

    @Test
    public void testFlagIsTheFallbackForMissingRole() {
        assertThat(severityOf(svrlWith("BR-DE-TMP-32", null, "information"))).isEqualTo(ECTSeverity.INFO);
        assertThat(severityOf(svrlWith("X", null, "warning"))).isEqualTo(ECTSeverity.WARNING);
        assertThat(severityOf(svrlWith("X", null, "fatal"))).isEqualTo(ECTSeverity.FATAL_ERROR);
    }

    @Test
    public void testRoleTakesPrecedenceOverFlag() {
        assertThat(severityOf(svrlWith("X", "warning", "fatal"))).isEqualTo(ECTSeverity.WARNING);
    }

    @Test
    public void testUnclassifiedAssertDefaultsToError() {
        assertThat(severityOf(svrlWith("X", null, null))).isEqualTo(ECTSeverity.ERROR);
    }
}
