package org.kosit.validator.impl;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.jaxb.JaxbConversionException;
import org.kosit.validator.impl.TestHelper.Invalid;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.scenario.v1.Scenario1ConversionService;
import org.kosit.validator.scenario.v1.ScenarioSchemas;
import org.kosit.validator.scenario.v1.Scenarios;

/**
 * Simple test for testing the jaxb conversion service.
 * 
 * @author apenski
 */
public class ConversionServiceTest {

    private static final URI SCHEMA = URI.create(ScenarioSchemas.class.getResource(ScenarioSchemas.SCENARIOS_V1_XSD_PATH).toExternalForm());

    private Scenario1ConversionService service;

    private ContentRepository repository;

    @BeforeEach
    public void setup() {
        this.service = new Scenario1ConversionService();
        this.repository = Simple.createContentRepository();
    }

    @Test
    public void testMarshalNull() {
        assertThrows(NullPointerException.class, () -> this.service.writeXml(null));
    }

    @Test
    public void testMarshalUnknown() {
        assertThrows(JaxbConversionException.class, () -> this.service.writeXml(new Serializable() {
        }));
    }

    @Test
    public void testUnmarshal() {
        final Scenarios s = this.service.readXml(Simple.SCENARIOS, Scenarios.class);
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualToIgnoringCase("HTML-TestSuite");
    }

    @Test
    public void testUnmarshalWithSchema() throws MalformedURLException {
        // since repository.createSchema(URI) forcibly resolves uri in repository path only, conversion to url is
        // neccesary
        final Scenarios s = this.service.withSchema(this.repository.createSchema(SCHEMA.toURL())).readXml(Simple.SCENARIOS,
                Scenarios.class);
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualToIgnoringCase("HTML-TestSuite");
    }

    @Test
    public void testUnmarshalInvalidXml() {
        assertThrows(JaxbConversionException.class,
                () -> this.service.withSchema(this.repository.createSchema(SCHEMA.toURL())).readXml(Invalid.SCENARIOS, Scenarios.class));
    }

    @Test
    public void testUnmarshalIllFormed() {
        assertThrows(JaxbConversionException.class, () -> this.service.withSchema(this.repository.createSchema(SCHEMA.toURL()))
                .readXml(Invalid.SCENARIOS_ILLFORMED, Scenarios.class));
    }

    @Test
    public void testUnmarshalEmpty() {
        assertThrows(NullPointerException.class, () -> this.service.readXml((URI) null, Scenarios.class));
    }

    @Test
    public void testUnmarshalUnknownType() {
        assertThrows(JaxbConversionException.class, () -> this.service.readXml(Simple.SCENARIOS, Scenario1ConversionService.class));
    }

    @Test
    public void testUnmarshalWithoutType() {
        assertThrows(NullPointerException.class, () -> this.service.readXml(Simple.SCENARIOS, null));
    }

}
