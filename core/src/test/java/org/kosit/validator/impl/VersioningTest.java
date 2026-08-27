package org.kosit.validator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.jaxb.JaxbConversionException;
import org.kosit.validator.scenario.v1.Scenario1ConversionService;
import org.kosit.validator.scenario.v1.Scenarios;

/**
 * Tests the versioning of scenario files aka configuration data.
 *
 * @author Andreas Penski
 */
public class VersioningTest {

    private static final URL BASE = VersioningTest.class.getResource("/examples/versioning/scenarios-base.xml");

    private static final URL INCREMENT = VersioningTest.class.getResource("/examples/versioning/scenarios-increment.xml");

    private static final URL NEW_FEATURE = VersioningTest.class.getResource("/examples/versioning/scenarios-newfeature.xml");

    private static final URL NEW_VERSION = VersioningTest.class.getResource("/examples/versioning/scenarios-newversion.xml");

    private Scenario1ConversionService service;

    @BeforeEach
    public void setup() {
        this.service = new Scenario1ConversionService();
    }

    @Test
    public void testBase() throws URISyntaxException {
        final Scenarios result = this.service.readXml(BASE.toURI());
        assertThat(result).isNotNull();
    }

    @Test
    public void testFrameworkIncrement() throws URISyntaxException {
        final Scenarios result = this.service.readXml(INCREMENT.toURI());
        assertThat(result).isNotNull();
    }

    @Test
    public void testNewFeature() {
        assertThrows(JaxbConversionException.class, () -> this.service.readXml(NEW_FEATURE.toURI()));
    }

    @Test
    public void testNewVersion() {
        assertThrows(JaxbConversionException.class, () -> this.service.readXml(NEW_VERSION.toURI()));
    }
}
