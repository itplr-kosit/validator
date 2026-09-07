package org.kost.validator.api.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionLocation;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;

import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.trans.XmlProcessingIncident;

public class CollectingSaxonErrorReporterTest {

    private static final String RESOURCE_ID = "test.xml";

    private static XmlProcessingIncident createIncident() {
        return new XmlProcessingIncident("Type error", "XPTY0004", new Loc("system.xml", 12, 4));
    }

    @Test
    public void toDetectionForError() {
        final CTDetection detection = CollectingSaxonErrorReporter.toDetection(createIncident());

        assertThat(detection.getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(detection.getCode()).contains("XPTY0004");
        assertThat(detection.getLocation().getResourceId()).isEqualTo("system.xml");
        assertThat(detection.getLocation().getLineNumber()).isEqualTo(12);
        assertThat(detection.getLocation().getColumnNumber()).isEqualTo(4);
        assertThat(detection.getText().getDisplayText(Locale.ROOT)).isEqualTo("Type error");
    }

    @Test
    public void toDetectionForWarning() {
        final XmlProcessingError warning = createIncident().asWarning();
        final CTDetection detection = CollectingSaxonErrorReporter.toDetection(warning);

        assertThat(detection.getSeverity()).isEqualTo(CTStandardSeverity.WARNING);
    }

    @Test
    public void toDetectionWithoutErrorCodeAndLocation() {
        final XmlProcessingIncident incident = new XmlProcessingIncident("Something went wrong");
        final CTDetection detection = CollectingSaxonErrorReporter.toDetection(incident);

        assertThat(detection.getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(detection.getCode()).isNull();
        assertThat(detection.getLocation().getResourceId()).isNull();
        assertThat(detection.getLocation().getLineNumber()).isEqualTo(CTDetectionLocation.ILLEGAL_NUMBER);
        assertThat(detection.getText().getDisplayText(Locale.ROOT)).isEqualTo("Something went wrong");
    }

    @Test
    public void toDetectionKeepsTheCause() {
        final IOException cause = new IOException("Stream closed");
        final XmlProcessingIncident incident = createIncident();
        incident.setCause(cause);

        assertThat(CollectingSaxonErrorReporter.toDetection(incident).getLinkedException()).isSameAs(cause);
    }

    @Test
    public void reportCollectsEveryError() {
        final List<CTDetection> errors = new ArrayList<>();
        final CollectingSaxonErrorReporter reporter = new CollectingSaxonErrorReporter(RESOURCE_ID, errors);
        reporter.report(createIncident());
        reporter.report(createIncident().asWarning());

        assertThat(errors).hasSize(2);
        assertThat(errors.get(0).getSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(errors.get(1).getSeverity()).isEqualTo(CTStandardSeverity.WARNING);
        assertThat(reporter.resourceId()).isEqualTo(RESOURCE_ID);
        assertThat(reporter.errors()).isSameAs(errors);
    }
}
