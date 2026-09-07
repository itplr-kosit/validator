package org.kosit.validator.impl;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.jaxb.JaxbConversionException;
import org.kosit.validator.scenario.v1.Scenario1Converter;
import org.kosit.validator.scenario.v1.Scenarios;
import org.kosit.validator.testdata.TestResources;
import org.kosit.validator.testdata.TestResources.Invalid;

/**
 * Simple test for testing the jaxb conversion service.
 *
 * @author apenski
 */
public class ConversionServiceTest {

    private Scenario1Converter converter;

    @BeforeEach
    public void setup() {
        this.converter = new Scenario1Converter();
    }

    @Test
    public void testMarshalNull() {
        assertThrows(NullPointerException.class, () -> this.converter.writeXml(null));
    }

    @Test
    public void testUnmarshal() {
        final Scenarios s = this.converter.readXml(TestResources.Simple.SCENARIOS);
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualToIgnoringCase("HTML-TestSuite");
    }

    @Test
    public void testUnmarshalWithSchema() {
        // since repository.createSchema(URI) forcibly resolves uri in repository path only, conversion to url is
        // neccesary
        final Scenarios s = this.converter.readXml(TestResources.Simple.SCENARIOS);
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualToIgnoringCase("HTML-TestSuite");
    }

    @Test
    public void testUnmarshalInvalidXml() {
        assertThrows(JaxbConversionException.class, () -> this.converter.readXml(Invalid.SCENARIOS));
    }

    @Test
    public void testUnmarshalIllFormed() {
        assertThrows(JaxbConversionException.class, () -> this.converter.readXml(Invalid.SCENARIOS_ILLFORMED));
    }

    @Test
    public void testUnmarshalEmpty() {
        assertThrows(NullPointerException.class, () -> this.converter.readXml((URI) null));
    }
}
