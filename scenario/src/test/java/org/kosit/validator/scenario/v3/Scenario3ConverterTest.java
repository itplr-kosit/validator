package org.kosit.validator.scenario.v3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.jaxb.JaxbConversionException;

public class Scenario3ConverterTest {

    private static final URL PLAYGROUND = Scenario3ConverterTest.class.getResource("/scenario-playground/scenarios-v3.xml");

    private Scenario3Converter converter;

    @BeforeEach
    public void setUp() {
        this.converter = new Scenario3Converter();
    }

    private @NonNull Scenarios readPlayground() throws URISyntaxException {
        return this.converter.readXml(PLAYGROUND.toURI());
    }

    @Test
    public void readsThePlaygroundScenarios() throws URISyntaxException {
        final Scenarios scenarios = readPlayground();
        assertThat(scenarios.getName()).isEqualTo("Prüftool-Konfiguration Peppol BIS");
        assertThat(scenarios.getAuthor()).isEqualTo("KoSIT");
        assertThat(scenarios.getLastModificationDate().toString()).isEqualTo("2026-05-20");
        assertThat(scenarios.getValidFromDate().toString()).isEqualTo("2026-09-20");
        assertThat(scenarios.getFrameworkVersion()).isNull();
        assertThat(scenarios.getScenarioXmlOrScenarioPdf()).hasSize(2);

        final ScenarioXmlType xml = (ScenarioXmlType) scenarios.getScenarioXmlOrScenarioPdf().get(0);
        assertThat(xml.getGroupId()).isEqualTo("org.peppol");
        assertThat(xml.getArtifactId()).isEqualTo("invoice-bis3");
        assertThat(xml.getVersion()).isEqualTo("2026.5");
        assertThat(xml.getClassifier()).isNull();
        assertThat(xml.getName()).isEqualTo("Peppol Billing BIS 3 - UBL Invoice");
        assertThat(xml.getNamespace()).hasSize(2);
        assertThat(xml.getMatch()).startsWith("/invoice:Invoice[");
        assertThat(xml.getValidateWithXmlSchema().getResource()).hasSize(1);
        assertThat(xml.getValidateWithXmlSchema().getResource().get(0).getGroupId()).isEqualTo("org.oasis");
        assertThat(xml.getValidateWithSchematron()).hasSize(3);
        assertThat(xml.getValidateWithSchematron().get(2).getResource().getVersion()).isEqualTo("renzo");

        final ScenarioPdfType pdf = (ScenarioPdfType) scenarios.getScenarioXmlOrScenarioPdf().get(1);
        assertThat(pdf.getGroupId()).isEqualTo("fr.afie");
        assertThat(pdf.getVersion()).isEqualTo("1.09.2");
        assertThat(pdf.getRequirements().getRequirement()).hasSize(2);
        assertThat(pdf.getRequirements().getRequirement().get(0).getId()).isEqualTo("pdf-a3");
        assertThat(pdf.getXmlScenarioRef().getId()).isEqualTo("org.cefact:cii:d22b");
    }

    @Test
    public void readsTheMixedContentDescriptions() throws URISyntaxException {
        final Scenarios scenarios = readPlayground();
        // The configuration description is free text
        assertThat(scenarios.getDescription().getContent()).hasSize(1);
        assertThat((String) scenarios.getDescription().getContent().get(0)).contains("Peppol BIS 3.0.21");

        // The XML scenario description consists of "p" elements plus the whitespace in between
        final ScenarioXmlType xml = (ScenarioXmlType) scenarios.getScenarioXmlOrScenarioPdf().get(0);
        assertThat(xml.getDescription().getContent()).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    public void roundTripsViaXml() throws URISyntaxException {
        final Scenarios original = readPlayground();
        final String xml = this.converter.writeXml(original);
        assertThat(xml).contains(Scenario3Converter.NAMESPACE_URI).contains("<scenarioPdf>").contains("1.09.2");

        final Scenarios parsed = this.converter.readXml(xml);
        assertThat(parsed.getName()).isEqualTo(original.getName());
        assertThat(parsed.getScenarioXmlOrScenarioPdf()).hasSameSizeAs(original.getScenarioXmlOrScenarioPdf());
        assertThat(((ScenarioXmlType) parsed.getScenarioXmlOrScenarioPdf().get(0)).getMatch())
                .isEqualTo(((ScenarioXmlType) original.getScenarioXmlOrScenarioPdf().get(0)).getMatch());
        assertThat(((ScenarioPdfType) parsed.getScenarioXmlOrScenarioPdf().get(1)).getXmlScenarioRef().getId())
                .isEqualTo("org.cefact:cii:d22b");
    }

    @Test
    public void rejectsDocumentViolatingTheSchema() {
        assertThatThrownBy(() -> this.converter.readXml("<scenarios xmlns=\"" + Scenario3Converter.NAMESPACE_URI + "\"/>"))
                .isInstanceOf(JaxbConversionException.class);
    }

    @Test
    public void schemaIsResolvableFromTheModuleItself() throws IOException {
        assertThat(PLAYGROUND).isNotNull();
        try ( var in = Scenario3ConverterTest.class.getResourceAsStream(Scenario3Converter.SCENARIOS_V3_XSD_PATH) ) {
            assertNotNull(in);
        }
    }
}
