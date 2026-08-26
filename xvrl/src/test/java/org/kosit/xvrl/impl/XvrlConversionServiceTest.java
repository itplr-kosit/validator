package org.kosit.xvrl.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.xvrl.model.ObjectFactory;
import org.kosit.xvrl.model.XVRLDetectionType;
import org.kosit.xvrl.model.XVRLReportType;
import org.kosit.xvrl.model.XVRLReportsType;

public class XvrlConversionServiceTest {

    private static final String SAMPLE = "/sample-report.xml";

    private XvrlConversionService service;

    @BeforeEach
    public void setUp() {
        this.service = new XvrlConversionService();
    }

    @Test
    public void readsSampleXvrlReport() {
        final XVRLReportsType summary = readSample();
        assertThat(summary).isNotNull();
        assertThat(summary.getMetadata()).isNotNull();
        assertThat(summary.getReports()).hasSize(2);

        final XVRLReportType schemaReport = summary.getReports().get(0);
        assertThat(schemaReport.getDigest().getValid()).isEqualTo("false");
        assertThat(schemaReport.getDigest().getErrorCount()).isEqualTo(1L);
        assertThat(schemaReport.getDetection()).hasSize(2);

        final XVRLDetectionType firstDetection = schemaReport.getDetection().get(0);
        assertThat(firstDetection.getSeverity()).isEqualTo(XVRLDetectionType.Severity.ERROR);
        assertThat(firstDetection.getCode()).isEqualTo("cvc-complex-type.2.4.a");
        assertThat(schemaReport.getAllErrors()).contains("Required element 'missing' is not present.");
    }

    @Test
    public void writesXvrlReportToXml() {
        final XVRLReportsType summary = readSample();
        final String xml = this.service.writeXml(new ObjectFactory().createReports(summary));
        assertThat(xml).contains("<reports xmlns=\"http://www.xproc.org/ns/xvrl\">");
        assertThat(xml).contains("xvrl-sample-validator");
        assertThat(xml).contains("Required element 'missing' is not present.");
    }

    @Test
    public void roundTripsViaXml() {
        final XVRLReportsType original = readSample();
        final String xml = this.service.writeXml(new ObjectFactory().createReports(original));

        final XVRLReportsType parsed;
        try ( InputStream in = new java.io.ByteArrayInputStream(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)) ) {
            parsed = this.service.readXml(in, XVRLReportsType.class);
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
        assertThatThrownBy(() -> this.service.readXml((URI) null, XVRLReportsType.class)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void readNullTypeThrows() {
        final URL url = getClass().getResource(SAMPLE);
        assertThat(url).as("sample-report.xml must be on the test classpath").isNotNull();
        final URI sampleUri = URI.create(url.toString());
        assertThatThrownBy(() -> this.service.readXml(sampleUri, null)).isInstanceOf(NullPointerException.class);
    }

    private XVRLReportsType readSample() {
        final URL url = getClass().getResource(SAMPLE);
        assertThat(url).as("sample-report.xml must be on the test classpath").isNotNull();
        return this.service.readXml(URI.create(url.toString()), XVRLReportsType.class);
    }
}
