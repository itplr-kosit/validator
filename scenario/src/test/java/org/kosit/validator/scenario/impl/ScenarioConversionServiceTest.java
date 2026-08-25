package org.kosit.validator.scenario.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.jaxb.JaxbConversionException;
import org.kosit.validator.scenario.model.CreateReportType;
import org.kosit.validator.scenario.model.ErrorLevelType;
import org.kosit.validator.scenario.model.ScenarioType;
import org.kosit.validator.scenario.model.Scenarios;

public class ScenarioConversionServiceTest {

    private static final URL SAMPLE = ScenarioConversionServiceTest.class.getResource("/sample-scenarios.xml");

    private ScenarioConversionService service;

    @BeforeEach
    public void setUp() {
        this.service = new ScenarioConversionService();
    }

    private Scenarios readSample() throws URISyntaxException {
        return this.service.readXml(SAMPLE.toURI(), Scenarios.class);
    }

    @Test
    public void readsSampleScenarios() throws URISyntaxException {
        final Scenarios scenarios = readSample();
        assertThat(scenarios).isNotNull();
        assertThat(scenarios.getFrameworkVersion()).isEqualTo("2.0.0");
        assertThat(scenarios.getName()).isEqualTo("Sample-TestSuite");
        assertThat(scenarios.getAuthor()).isEqualTo("QA");
        assertThat(scenarios.getScenario()).hasSize(2);

        final ScenarioType simple = scenarios.getScenario().get(0);
        assertThat(simple.getName()).isEqualTo("Simple");
        assertThat(simple.getMatch()).isEqualTo("/test:simple");
        assertThat(simple.getAcceptMatch()).isEqualTo("count(//test:rejected) = 0");
        assertThat(simple.getNamespace()).hasSize(1);
        assertThat(simple.getNamespace().get(0).getPrefix()).isEqualTo("test");
        assertThat(simple.getValidateWithXmlSchema().getResource()).hasSize(1);
        assertThat(simple.getValidateWithXmlSchema().getResource().get(0).getLocation()).isEqualTo("simple.xsd");
        assertThat(simple.getValidateWithSchematron()).hasSize(1);

        final CreateReportType report = simple.getCreateReport().get(0);
        assertThat(report.getId()).isEqualTo("Report for eInvoice");
        assertThat(report.getCustomLevel()).hasSize(1);
        assertThat(report.getCustomLevel().get(0).getLevel()).isEqualTo(ErrorLevelType.WARNING);
        assertThat(report.getCustomLevel().get(0).getValue()).containsExactly("BR-01", "BR-02");

        final ScenarioType schemaOnly = scenarios.getScenario().get(1);
        assertThat(schemaOnly.getName()).isEqualTo("SchemaOnly");
        assertThat(schemaOnly.getValidateWithSchematron()).isEmpty();
    }

    @Test
    public void writesScenariosToXml() throws URISyntaxException {
        final String xml = this.service.writeXml(readSample());
        assertThat(xml).contains("http://www.xoev.de/de/validator/framework/2/scenarios");
        assertThat(xml).contains("Sample-TestSuite");
        assertThat(xml).contains("/test:simple");
    }

    @Test
    public void roundTripsViaXml() throws URISyntaxException {
        final Scenarios original = readSample();
        final String xml = this.service.writeXml(original);

        final Scenarios parsed = this.service.readXml(xml, Scenarios.class);
        assertThat(parsed.getName()).isEqualTo(original.getName());
        assertThat(parsed.getFrameworkVersion()).isEqualTo(original.getFrameworkVersion());
        assertThat(parsed.getScenario()).hasSameSizeAs(original.getScenario());
        assertThat(parsed.getScenario().get(0).getMatch()).isEqualTo(original.getScenario().get(0).getMatch());
    }

    @Test
    public void rejectsDocumentViolatingTheSchema() {
        assertThatThrownBy(
                () -> this.service.readXml("<scenarios xmlns=\"http://www.xoev.de/de/validator/framework/2/scenarios\"/>", Scenarios.class))
                        .isInstanceOf(JaxbConversionException.class);
    }

    @Test
    public void schemaIsResolvableFromTheModuleItself() throws IOException {
        assertThat(SAMPLE).isNotNull();
        try ( var in = ScenarioConversionServiceTest.class.getResourceAsStream("/xsd/scenarios.xsd") ) {
            assertThat(in).isNotNull();
        }
    }
}
