package org.kosit.validator.scenario.generic;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.scenario.v2.Scenario2Converter;
import org.kosit.validator.scenario.v2.Scenario2Mapper;
import org.kosit.validator.scenario.v3.Scenario3Converter;
import org.kosit.validator.scenario.v3.Scenario3Mapper;

/**
 * Tests the conversion of both scenario configuration versions to and from the generic model.
 */
public class ScenarioMappingTest {

    private static final String V2_SAMPLE = "/sample-scenarios.xml";

    private static final String V3_PLAYGROUND = "/scenario-playground/scenarios-v3.xml";

    private final Scenario2Converter converter2 = new Scenario2Converter();

    private final Scenario3Converter converter3 = new Scenario3Converter();

    private org.kosit.validator.scenario.v2.Scenarios readV2() throws URISyntaxException {
        return this.converter2.readXml(ScenarioMappingTest.class.getResource(V2_SAMPLE).toURI());
    }

    private org.kosit.validator.scenario.v3.Scenarios readV3() throws URISyntaxException {
        return this.converter3.readXml(ScenarioMappingTest.class.getResource(V3_PLAYGROUND).toURI());
    }

    private static List<String> messagesOf(final List<SimpleError> errors, final CTStandardSeverity severity) {
        return errors.stream().filter(x -> x.getSeverity() == severity).map(SimpleError::getMessage).toList();
    }

    private static void addCoordinates(final ScenarioConfiguration config) {
        int index = 0;
        for (final Scenario scenario : config.getScenarios()) {
            scenario.setCoordinate(ScenarioCoordinate.of("org.example", "scenario-" + index, "1.0"));
            int resourceIndex = 0;
            final List<ScenarioResource> resources = new ArrayList<>(scenario.getXmlSchemas());
            scenario.getSchematrons().forEach(x -> resources.add(x.getResource()));
            scenario.getCreateReports().forEach(x -> resources.add(x.getResource()));
            for (final ScenarioResource resource : resources) {
                resource.setCoordinate(ScenarioCoordinate.of("org.example", "resource-" + index + "-" + resourceIndex, "1.0"));
                resourceIndex++;
            }
            index++;
        }
    }

    @Test
    public void readsVersion2IntoTheGenericModel() throws URISyntaxException {
        final ScenarioConfiguration config = Scenario2Mapper.toGeneric(readV2());
        assertThat(config.getName()).isEqualTo("Sample-TestSuite");
        assertThat(config.getAuthor()).isEqualTo("QA");
        assertThat(config.getFrameworkVersion()).isEqualTo("2.0.0");
        assertThat(config.getLastModificationDate()).hasToString("2017-08-08");
        assertThat(config.getValidFromDate()).isNull();
        assertThat(config.getDescription().getBlocks()).hasSize(1);
        assertThat(config.getDescription().getBlocks().get(0).getKind()).isEqualTo(EScenarioDescriptionBlockKind.PARAGRAPH);
        assertThat(config.getScenarios()).hasSize(2);

        final Scenario simple = config.getScenarios().get(0);
        assertThat(simple.getKind()).isEqualTo(EScenarioKind.XML);
        assertThat(simple.hasCoordinate()).isFalse();
        assertThat(simple.getName()).isEqualTo("Simple");
        assertThat(simple.getMatch()).isEqualTo("/test:simple");
        assertThat(simple.getAcceptMatch()).isEqualTo("count(//test:rejected) = 0");
        assertThat(simple.getNamespaces()).containsExactly(ScenarioNamespace.of("test", "http://validator.kosit.de/test-sample"));
        assertThat(simple.getXmlSchemas()).singleElement().satisfies(x -> assertThat(x.getLocation()).isEqualTo("simple.xsd"));
        assertThat(simple.getSchematrons()).hasSize(1);
        assertThat(simple.getCreateReports()).singleElement().satisfies(x -> {
            assertThat(x.getID()).isEqualTo("Report for eInvoice");
            assertThat(x.getCustomLevels()).singleElement().satisfies(y -> {
                assertThat(y.getLevel()).isEqualTo(EScenarioErrorLevel.WARNING);
                assertThat(y.getRuleIDs()).containsExactly("BR-01", "BR-02");
            });
        });
    }

    @Test
    public void version2RoundTripIsLossless() throws URISyntaxException {
        final var original = readV2();
        final List<SimpleError> errors = new ArrayList<>();
        final var converted = Scenario2Mapper.fromGeneric(Scenario2Mapper.toGeneric(original), errors);

        assertThat(errors).isEmpty();
        assertThat(this.converter2.writeXml(converted)).isEqualTo(this.converter2.writeXml(original));
    }

    @Test
    public void readsVersion3IntoTheGenericModel() throws URISyntaxException {
        final ScenarioConfiguration config = Scenario3Mapper.toGeneric(readV3());
        assertThat(config.getName()).isEqualTo("Prüftool-Konfiguration Peppol BIS");
        assertThat(config.getAuthor()).isEqualTo("KoSIT");
        assertThat(config.getLastModificationDate()).hasToString("2026-05-20");
        assertThat(config.getValidFromDate()).hasToString("2026-09-20");
        assertThat(config.getFrameworkVersion()).isNull();
        // The configuration description is free text, the scenario descriptions use paragraphs
        assertThat(config.getDescription().getBlocks()).singleElement()
                .satisfies(x -> assertThat(x.getKind()).isEqualTo(EScenarioDescriptionBlockKind.TEXT));
        assertThat(config.getScenarios()).hasSize(2);

        final Scenario xml = config.getScenarios().get(0);
        assertThat(xml.getKind()).isEqualTo(EScenarioKind.XML);
        assertThat(xml.getCoordinate().getAsSingleID()).isEqualTo("org.peppol:invoice-bis3:2026.5");
        assertThat(xml.getDescription().getBlocks())
                .allSatisfy(x -> assertThat(x.getKind()).isEqualTo(EScenarioDescriptionBlockKind.PARAGRAPH));
        assertThat(xml.getXmlSchemas()).singleElement().satisfies(x -> {
            assertThat(x.getCoordinate().getAsSingleID()).isEqualTo("org.oasis:ubl-invoice:2.1");
            assertThat(x.getLocation()).isEqualTo("resources/ubl/2.1/xsd/maindoc/UBL-Invoice-2.1.xsd");
        });
        assertThat(xml.getSchematrons()).hasSize(3);
        assertThat(xml.getSchematrons().get(2).getResource().getCoordinate().getAsSingleID()).isEqualTo("org.example:doof:renzo");

        final Scenario pdf = config.getScenarios().get(1);
        assertThat(pdf.getKind()).isEqualTo(EScenarioKind.PDF);
        // The Factur-X version has no DVRCoordinate representation, but it is retained as it is
        assertThat(pdf.getCoordinate().getAsSingleID()).isEqualTo("fr.afie:factur-x:1.09.2");
        assertThat(pdf.getCoordinate().hasCoordinate()).isFalse();
        assertThat(pdf.getRequirements()).containsExactly(ScenarioRequirement.of("pdf-a3"),
                ScenarioRequirement.of("attachment-filename-factur-x"));
        assertThat(pdf.getXmlScenarioRef().getAsSingleID()).isEqualTo("org.cefact:cii:d22b");
    }

    @Test
    public void version3RoundTripKeepsEveryValue() throws URISyntaxException {
        final var original = readV3();
        final List<SimpleError> errors = new ArrayList<>();
        final var converted = Scenario3Mapper.fromGeneric(Scenario3Mapper.toGeneric(original), errors);

        // The unparseable Factur-X version is reported as a warning, but is written as it is
        assertThat(messagesOf(errors, CTStandardSeverity.ERROR)).isEmpty();
        assertThat(messagesOf(errors, CTStandardSeverity.WARNING)).singleElement(InstanceOfAssertFactories.STRING)
                .contains("fr.afie:factur-x:1.09.2");

        // The written document is schema valid and can be read back into an equal generic model
        final String xml = this.converter3.writeXml(converted);
        assertThat(Scenario3Mapper.toGeneric(this.converter3.readXml(xml))).hasToString(Scenario3Mapper.toGeneric(original).toString());
    }

    @Test
    public void version3ToVersion2ReportsEveryLoss() throws URISyntaxException {
        final ScenarioConfiguration config = Scenario3Mapper.toGeneric(readV3());
        final List<SimpleError> errors = new ArrayList<>();
        Scenario2Mapper.fromGeneric(config, errors);

        assertThat(messagesOf(errors, CTStandardSeverity.ERROR)).singleElement(InstanceOfAssertFactories.STRING)
                .contains("framework version");
        assertThat(messagesOf(errors, CTStandardSeverity.WARNING)).singleElement(InstanceOfAssertFactories.STRING)
                .contains("Dropping the pdf scenario 'Factur-X basic'");
        assertThat(messagesOf(errors, CTStandardSeverity.NONE)).hasSize(2)
                .anySatisfy(x -> assertThat(x).contains("Dropping the valid from date '2026-09-20'"))
                .anySatisfy(x -> assertThat(x).contains("Dropping the DVR coordinates of 1 scenario(s) and of 4 resource(s)"));
    }

    @Test
    public void version3ToVersion2WritesAValidDocument() throws URISyntaxException {
        final ScenarioConfiguration config = Scenario3Mapper.toGeneric(readV3());
        // Version 2 requires the framework version, version 3 does not have it in the playground file
        config.setFrameworkVersion("1.0.0");

        final List<SimpleError> errors = new ArrayList<>();
        final var converted = Scenario2Mapper.fromGeneric(config, errors);
        assertThat(messagesOf(errors, CTStandardSeverity.ERROR)).isEmpty();

        // writeXml validates against scenarios-v2.xsd
        final String xml = this.converter2.writeXml(converted);
        assertThat(xml).contains(Scenario2Converter.NAMESPACE_URI).contains("Peppol Billing BIS 3 - UBL Invoice")
                .doesNotContain("Factur-X basic").doesNotContain("groupId");
        assertThat(converted.getScenario()).hasSize(1);
    }

    @Test
    public void version2ToVersion3ReportsTheMissingCoordinates() throws URISyntaxException {
        final ScenarioConfiguration config = Scenario2Mapper.toGeneric(readV2());
        final List<SimpleError> errors = new ArrayList<>();
        Scenario3Mapper.fromGeneric(config, errors);

        // 2 scenarios plus 2 XML Schemas, 1 Schematron and 1 report resource
        assertThat(messagesOf(errors, CTStandardSeverity.ERROR)).hasSize(6)
                .allSatisfy(x -> assertThat(x).contains("has no DVR coordinate"));
    }

    @Test
    public void version2ToVersion3WritesAValidDocumentOnceTheCoordinatesAreSet() throws URISyntaxException {
        final ScenarioConfiguration config = Scenario2Mapper.toGeneric(readV2());
        addCoordinates(config);

        final List<SimpleError> errors = new ArrayList<>();
        final var converted = Scenario3Mapper.fromGeneric(config, errors);
        assertThat(errors).isEmpty();

        // writeXml validates against scenarios-v3.xsd
        final String xml = this.converter3.writeXml(converted);
        assertThat(xml).contains(Scenario3Converter.NAMESPACE_URI).contains("<groupId>org.example</groupId>").contains("<scenarioXml>")
                .doesNotContain("<scenarioPdf>");
    }

    @Test
    public void convertsTheDescriptionBlockKindsIntoBothVersions() {
        final ScenarioConfiguration config = new ScenarioConfiguration("Description test");
        config.setFrameworkVersion("1.0.0").setLastModificationDate(LocalDate.of(2026, 8, 28));
        config.setDescription(new ScenarioDescription().addText("free text").addParagraph("a paragraph")
                .addOrderedList(List.of("first", "second")).addUnorderedList(List.of("one", "two")));
        final Scenario scenario = new Scenario(EScenarioKind.XML, "Only for the schema").setMatch("/foo")
                .setCoordinate(ScenarioCoordinate.of("org.example", "scenario", "1.0"));
        scenario.addXmlSchema(
                new ScenarioResource("Schema").setLocation("foo.xsd").setCoordinate(ScenarioCoordinate.of("org.example", "schema", "1.0")));
        config.addScenario(scenario);

        // Version 2 has no free text, so it becomes a paragraph
        final String xml1 = new Scenario2Converter().writeXml(Scenario2Mapper.fromGeneric(config));
        assertThat(xml1).contains("<p>free text</p>").contains("<p>a paragraph</p>").contains("<li>first</li>").contains("<li>one</li>");

        // Version 3 keeps the free text as it is
        final String xml2 = new Scenario3Converter().writeXml(Scenario3Mapper.fromGeneric(config));
        assertThat(xml2).contains("free text<p>a paragraph</p>").contains("<li>first</li>").contains("<li>one</li>");
    }
}
