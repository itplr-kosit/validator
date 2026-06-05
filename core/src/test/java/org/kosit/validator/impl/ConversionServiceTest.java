package org.kosit.validator.impl;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.jaxb.JaxbConversionException;
import org.kosit.validator.api.xsd.ValidatorSchemas;
import org.kosit.validator.impl.Helper.Invalid;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.model.scenarios.Scenarios;

/**
 * Simple test for testing the jaxb conversion service.
 * 
 * @author apenski
 */
public class ConversionServiceTest {

    private static final URI SCHEMA = URI.create(ValidatorSchemas.class.getResource(ValidatorSchemas.SCENARIOS_XSD_PATH).toExternalForm());

    private ScenariosConversionService service;

    private ContentRepository repository;

    @BeforeEach
    public void setup() {
        this.service = new ScenariosConversionService();
        this.repository = Simple.createContentRepository();
    }

    @Test
    public void testMarshalNull() {
        assertThrows(JaxbConversionException.class, () -> this.service.writeXml(null));
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
        assertThrows(JaxbConversionException.class, () -> this.service.readXml((URI) null, Scenarios.class));
    }

    @Test
    public void testUnmarshalUnknownType() {
        assertThrows(JaxbConversionException.class, () -> this.service.readXml(Simple.SCENARIOS, ScenariosConversionService.class));
    }

    @Test
    public void testUnmarshalWithoutType() {
        assertThrows(JaxbConversionException.class, () -> this.service.readXml(Simple.SCENARIOS, null));
    }

}
