package org.kosit.validator.impl.conformatron.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.conformatron.api.model.detection.CTDetectionList;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;

/**
 * Tests the SVRL severity mapping of {@link SvrlDetections}: {@code @role} takes precedence, {@code @flag} is the
 * fallback (XRechnung rules carry the level in {@code @flag} — regression from the XRechnung E2E run).
 */
public class SvrlDetectionsTest {

    private static SchematronOutputType svrlWith(final String id, final String role, final String flag) {
        final FailedAssert failedAssert = new FailedAssert();
        failedAssert.setId(id);
        failedAssert.setRole(role);
        failedAssert.getFlag().add(flag);
        final SchematronOutputType svrl = new SchematronOutputType();
        svrl.getActivePatternOrActiveGroupAndFiredRule().add(failedAssert);
        return svrl;
    }

    private static CTStandardSeverity severityOf(final SchematronOutputType svrl) {
        final CTDetectionList detections = SvrlDetections.toDetections(svrl, "test.xml");
        return (CTStandardSeverity) detections.getAll().get(0).getSeverity();
    }

    @Test
    public void testFlagIsTheFallbackForMissingRole() {
        assertThat(severityOf(svrlWith("BR-DE-TMP-32", null, "information"))).isEqualTo(CTStandardSeverity.NONE);
        assertThat(severityOf(svrlWith("X", null, "warning"))).isEqualTo(CTStandardSeverity.WARNING);
        assertThat(severityOf(svrlWith("X", null, "fatal"))).isEqualTo(CTStandardSeverity.ERROR);
    }

    @Test
    public void testRoleTakesPrecedenceOverFlag() {
        assertThat(severityOf(svrlWith("X", "warning", "fatal"))).isEqualTo(CTStandardSeverity.WARNING);
    }

    @Test
    public void testUnclassifiedAssertDefaultsToError() {
        assertThat(severityOf(svrlWith("X", null, null))).isEqualTo(CTStandardSeverity.ERROR);
    }
}
