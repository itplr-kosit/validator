package org.kosit.validator.scenario.convert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.scenario.generic.ScenarioConfiguration;
import org.kosit.validator.scenario.v2.Scenario2Converter;
import org.kosit.validator.scenario.v2.Scenario2Mapper;
import org.kosit.validator.scenario.v3.Scenario3Mapper;

/**
 * What a configuration of version 2 is missing for version 3, and what the conversion proposes for it.
 */
public class CoordinateProposalsTest {

    private static final String V2_SAMPLE = "/sample-scenarios.xml";

    private static ScenarioConfiguration readV2() throws URISyntaxException {
        return Scenario2Mapper.toGeneric(new Scenario2Converter().readXml(CoordinateProposalsTest.class.getResource(V2_SAMPLE).toURI()));
    }

    @Test
    public void testOnlyWhatHasNoCoordinateIsProposedFor() throws URISyntaxException {
        final List<CoordinateProposal> proposals = CoordinateProposals.propose(readV2(), "de.example", "1.0.0");

        // the sample states the coordinate of the first scenario and of its schematron, and leaves off the rest.
        // Both scenarios validate with simple.xsd - that is one artifact, so it is proposed for once
        assertThat(proposals).extracting(CoordinateProposal::toString).containsExactly(
                "the artifact 'simple.xsd' -> de.example:simple-xsd:1.0.0", "the scenario 'SchemaOnly' -> de.example:schemaonly:1.0.0");
    }

    @Test
    public void testProposingChangesNothing() throws URISyntaxException {
        final ScenarioConfiguration configuration = readV2();
        final String before = configuration.toString();

        CoordinateProposals.propose(configuration, "de.example", "1.0.0");

        assertThat(configuration).hasToString(before);
    }

    @Test
    public void testAppliedProposalsMakeTheConfigurationConvertible() throws URISyntaxException {
        final ScenarioConfiguration configuration = readV2();
        // without the coordinates version 3 can not be written: it requires one per scenario and per resource
        assertThat(errorsOfConversionTo3(configuration)).hasSize(3).allMatch(e -> e.getMessage().contains("has no DVR coordinate"));

        final List<CoordinateProposal> applied = CoordinateProposals.apply(configuration, "de.example", "1.0.0");

        assertThat(applied).hasSize(2);
        // the shared artifact got one coordinate, and both scenarios point at it
        assertThat(configuration.getScenarios().get(1).getXmlSchemas().get(0).getCoordinate().getAsSingleID())
                .isEqualTo(configuration.getScenarios().get(0).getXmlSchemas().get(0).getCoordinate().getAsSingleID());
        assertThat(errorsOfConversionTo3(configuration)).isEmpty();
        // what the configuration already stated stays as it was
        assertThat(configuration.getScenarios().get(0).getCoordinate().getAsSingleID()).isEqualTo("org.kosit.validator.test:simple:1.0.0");
    }

    @Test
    public void testAppliedTwiceProposesNothingTheSecondTime() throws URISyntaxException {
        final ScenarioConfiguration configuration = readV2();

        assertThat(CoordinateProposals.apply(configuration, "de.example", "1.0.0")).hasSize(2);
        assertThat(CoordinateProposals.apply(configuration, "de.example", "1.0.0")).isEmpty();
    }

    @Test
    public void testAGroupIdThatIsNoCoordinatePartIsRejected() throws URISyntaxException {
        final ScenarioConfiguration configuration = readV2();

        assertThatThrownBy(() -> CoordinateProposals.propose(configuration, "de example", "1.0.0"))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("is not a valid coordinate part");
    }

    private static List<SimpleError> errorsOfConversionTo3(final ScenarioConfiguration configuration) {
        final List<SimpleError> findings = new ArrayList<>();
        Scenario3Mapper.fromGeneric(configuration, findings);
        return findings.stream().filter(e -> e.getSeverity() == CTStandardSeverity.ERROR).toList();
    }
}
