package org.kosit.validator.impl;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.MalformedURLException;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.jaxb.JaxbConversionException;
import org.kosit.validator.impl.TestHelper.Invalid;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.scenario.v2.Scenarios;
import org.kosit.validator.scenario.v2.Scenario2Converter;

/**
 * Simple test for testing the jaxb conversion service.
 * 
 * @author apenski
 */
public class ConversionServiceTest {

    private Scenario2Converter converter;

    private ContentRepository repository;

    @BeforeEach
    public void setup() {
        this.converter = new Scenario2Converter();
        this.repository = TestHelper.Simple.createContentRepository();
    }

    @Test
    public void testMarshalNull() {
        assertThrows(NullPointerException.class, () -> this.converter.writeXml(null));
    }

    @Test
    public void testUnmarshal() {
        final Scenarios s = this.converter.readXml(Simple.SCENARIOS);
        assertThat(s).isNotNull();
        assertThat(s.getName()).isEqualToIgnoringCase("HTML-TestSuite");
    }

    @Test
    public void testUnmarshalWithSchema() throws MalformedURLException {
        // since repository.createSchema(URI) forcibly resolves uri in repository path only, conversion to url is
        // neccesary
        final Scenarios s = this.converter.readXml(Simple.SCENARIOS);
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
