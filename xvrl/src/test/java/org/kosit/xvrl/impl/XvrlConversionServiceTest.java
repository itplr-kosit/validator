package org.kosit.xvrl.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.xvrl.model.XVRLDetection;
import org.kosit.xvrl.model.XVRLReport;
import org.kosit.xvrl.model.XVRLReportSummary;

public class XvrlConversionServiceTest {

    private static final String SAMPLE = "/sample-report.xml";

    private XvrlConversionService service;

    @BeforeEach
    public void setUp() {
        this.service = new XvrlConversionService();
    }

    @Test
    public void readsSampleXvrlReport() {
        final XVRLReportSummary summary = readSample();
        assertThat(summary).isNotNull();
        assertThat(summary.getMetadata()).isNotNull();
        assertThat(summary.getReports()).hasSize(2);

        final XVRLReport schemaReport = summary.getReports().get(0);
        assertThat(schemaReport.getDigest().getValid()).isEqualTo("false");
        assertThat(schemaReport.getDigest().getErrorCount()).isEqualTo(1L);
        assertThat(schemaReport.getDetection()).hasSize(2);

        final XVRLDetection firstDetection = schemaReport.getDetection().get(0);
        assertThat(firstDetection.getSeverity()).isEqualTo(XVRLDetection.Severity.ERROR);
        assertThat(firstDetection.getCode()).isEqualTo("cvc-complex-type.2.4.a");
        assertThat(schemaReport.getAllErrors()).contains("Required element 'missing' is not present.");
    }

    @Test
    public void writesXvrlReportToXml() {
        final XVRLReportSummary summary = readSample();
        final String xml = this.service.writeXml(summary);
        assertThat(xml).contains("http://www.xproc.org/ns/xvrl");
        assertThat(xml).contains("xvrl-sample-validator");
        assertThat(xml).contains("Required element 'missing' is not present.");
    }

    @Test
    public void roundTripsViaXml() {
        final XVRLReportSummary original = readSample();
        final String xml = this.service.writeXml(original);

        final XVRLReportSummary parsed;
        try ( InputStream in = new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)) ) {
            parsed = this.service.readXml(in, XVRLReportSummary.class);
        } catch (final IOException e) {
            throw new AssertionError(e);
        }

        assertThat(parsed.getReports()).hasSize(original.getReports().size());
        assertThat(parsed.getReports().get(0).getDigest().getErrorCount())
                .isEqualTo(original.getReports().get(0).getDigest().getErrorCount());
    }

    @Test
    public void writeNullThrows() {
        assertThatThrownBy(() -> this.service.writeXml(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void readNullUriThrows() {
        assertThatThrownBy(() -> this.service.readXml((URI) null, XVRLReportSummary.class)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void readNullTypeThrows() {
        final URL url = getClass().getResource(SAMPLE);
        assertThat(url).as("sample-report.xml must be on the test classpath").isNotNull();
        final URI sampleUri = URI.create(url.toString());
        assertThatThrownBy(() -> this.service.readXml(sampleUri, null)).isInstanceOf(NullPointerException.class);
    }

    private XVRLReportSummary readSample() {
        final URL url = getClass().getResource(SAMPLE);
        assertThat(url).as("sample-report.xml must be on the test classpath").isNotNull();
        return this.service.readXml(URI.create(url.toString()), XVRLReportSummary.class);
    }
}
