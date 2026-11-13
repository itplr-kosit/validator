package org.kosit.validator.scenario.convert;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kosit.validator.scenario.v2.Scenario2Converter;
import org.kosit.validator.scenario.v3.Scenario3Converter;
import org.kosit.validator.scenario.v3.Scenario3Mapper;

/**
 * Test class for class {@link ScenarioMigration}.
 */
public class ScenarioMigrationTest {

    private static Path resource(final String name) throws Exception {
        return Paths.get(ScenarioMigrationTest.class.getResource(name).toURI());
    }

    @Test
    public void missingCoordinatesAreProposedAndNothingIsConverted() throws Exception {
        // the sample leaves the coordinate of one artifact and of one scenario off
        final MigrationResult result = ScenarioMigration.toVersion3(resource("/sample-scenarios.xml"), null, "1.0.0", false);

        assertThat(result.outcome()).isEqualTo(EMigrationOutcome.COORDINATES_PROPOSED);
        assertThat(result.isConverted()).isFalse();
        assertThat(result.converted()).isNull();
        assertThat(result.proposals()).extracting(CoordinateProposal::toString).containsExactly(
                "the artifact 'simple.xsd' -> " + ScenarioMigration.PLACEHOLDER_GROUP_ID + ":simple-xsd:1.0.0",
                "the scenario 'SchemaOnly' -> " + ScenarioMigration.PLACEHOLDER_GROUP_ID + ":schemaonly:1.0.0");
    }

    @Test
    public void aGroupIdAloneDoesNotAcceptTheProposals() throws Exception {
        final MigrationResult result = ScenarioMigration.toVersion3(resource("/sample-scenarios.xml"), "de.example", "1.0.0", false);

        assertThat(result.outcome()).isEqualTo(EMigrationOutcome.COORDINATES_PROPOSED);
        assertThat(result.proposals()).hasSize(2);
    }

    @Test
    public void acceptedProposalsAreWrittenIn() throws Exception {
        final MigrationResult result = ScenarioMigration.toVersion3(resource("/sample-scenarios.xml"), "de.example", "1.0.0", true);

        assertThat(result.outcome()).isEqualTo(EMigrationOutcome.CONVERTED);
        assertThat(result.proposals()).hasSize(2);
        assertThat(result.rootNamespace()).isEqualTo(Scenario2Converter.NS_URI);
        // the result is a configuration of version 3 that reads back into the same declarations
        final String written = new Scenario3Converter().writeXml(result.converted());
        assertThat(Scenario3Mapper.toGeneric(new Scenario3Converter().readXml(written)).getScenarios()).hasSize(2);
        assertThat(written).contains("<artifactId>schemaonly</artifactId>").contains("<groupId>de.example</groupId>");
    }

    @Test
    public void aConfigurationOfVersion3IsLeftAlone() throws Exception {
        final MigrationResult result = ScenarioMigration.toVersion3(resource("/scenario-playground/scenarios-v3.xml"), "de.example",
                "1.0.0", true);

        assertThat(result.outcome()).isEqualTo(EMigrationOutcome.ALREADY_VERSION_3);
        assertThat(result.rootNamespace()).isEqualTo(Scenario3Converter.NS_URI);
        assertThat(result.converted()).isNull();
    }

    @Test
    public void anotherDocumentIsNoScenarioConfiguration(@TempDir final Path dir) throws Exception {
        final Path other = Files.writeString(dir.resolve("other.xml"), "<other xmlns=\"urn:example\"/>", StandardCharsets.UTF_8);

        final MigrationResult result = ScenarioMigration.toVersion3(other, "de.example", "1.0.0", true);

        assertThat(result.outcome()).isEqualTo(EMigrationOutcome.NO_SCENARIO_CONFIGURATION);
        assertThat(result.rootNamespace()).isEqualTo("urn:example");
    }

    @Test
    public void aFileThatIsNoXmlIsNoScenarioConfiguration(@TempDir final Path dir) throws Exception {
        final Path text = Files.writeString(dir.resolve("notes.txt"), "this is no XML", StandardCharsets.UTF_8);

        final MigrationResult result = ScenarioMigration.toVersion3(text, "de.example", "1.0.0", true);

        assertThat(result.outcome()).isEqualTo(EMigrationOutcome.NO_SCENARIO_CONFIGURATION);
        assertThat(result.rootNamespace()).isNull();
    }
}
