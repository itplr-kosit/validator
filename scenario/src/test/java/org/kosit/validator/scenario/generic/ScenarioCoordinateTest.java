package org.kosit.validator.scenario.generic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.kosit.base.coord.DVRCoordinate;
import org.kosit.base.coord.DVRCoordinateException;
import org.kosit.base.coord.DVRException;

public class ScenarioCoordinateTest {

    @Test
    public void keepsTheRawPartsAndOffersTheParsedCoordinate() throws DVRException {
        final ScenarioCoordinate coordinate = ScenarioCoordinate.of("org.peppol", "invoice-bis3", "2026.5");
        assertThat(coordinate.getGroupID()).isEqualTo("org.peppol");
        assertThat(coordinate.getArtifactID()).isEqualTo("invoice-bis3");
        assertThat(coordinate.getVersion()).isEqualTo("2026.5");
        assertThat(coordinate.getClassifier()).isNull();
        assertThat(coordinate.hasClassifier()).isFalse();
        assertThat(coordinate.getAsSingleID()).isEqualTo("org.peppol:invoice-bis3:2026.5");

        assertThat(coordinate.hasCoordinate()).isTrue();
        assertThat(coordinate.getCoordinateError()).isNull();
        assertThat(coordinate.getCoordinateOrThrow().getAsSingleID()).isEqualTo("org.peppol:invoice-bis3:2026.5");
    }

    @Test
    public void keepsAVersionThatIsNoValidDvrVersion() {
        // Factur-X 1.09.2 - DVRVersion rejects the superfluous leading zero
        final ScenarioCoordinate coordinate = ScenarioCoordinate.of("fr.afie", "factur-x", "1.09.2");
        assertThat(coordinate.getVersion()).isEqualTo("1.09.2");
        assertThat(coordinate.getAsSingleID()).isEqualTo("fr.afie:factur-x:1.09.2");

        assertThat(coordinate.hasCoordinate()).isFalse();
        assertThat(coordinate.getCoordinate()).isNull();
        assertThat(coordinate.getCoordinateError()).contains("1.09.2");
        assertThatThrownBy(coordinate::getCoordinateOrThrow).isInstanceOf(DVRCoordinateException.class);
    }

    @Test
    public void keepsTheVersionThatDvrCoordinateWouldNormalize() throws DVRException {
        final ScenarioCoordinate coordinate = ScenarioCoordinate.of("org.example", "doof", "1.0.0");
        // The raw version survives...
        assertThat(coordinate.getVersion()).isEqualTo("1.0.0");
        assertThat(coordinate.getAsSingleID()).isEqualTo("org.example:doof:1.0.0");
        // ... whereas DVRCoordinate normalizes it
        assertThat(coordinate.getCoordinateOrThrow().getVersionString()).isEqualTo("1");
    }

    @Test
    public void supportsAClassifier() {
        final ScenarioCoordinate coordinate = ScenarioCoordinate.of("org.example", "doof", "1.2", "test");
        assertThat(coordinate.hasClassifier()).isTrue();
        assertThat(coordinate.getClassifier()).isEqualTo("test");
        assertThat(coordinate.getAsSingleID()).isEqualTo("org.example:doof:1.2:test");
    }

    @Test
    public void roundTripsViaTheSingleId() throws DVRException {
        for (final String singleID : new String[] { "org.cefact:cii:d22b", "fr.afie:factur-x:1.09.2", "a:b:1.2:c" }) {
            assertThat(ScenarioCoordinate.parseOrThrow(singleID).getAsSingleID()).isEqualTo(singleID);
        }
    }

    @Test
    public void rejectsAnInvalidSingleId() {
        assertThat(ScenarioCoordinate.parseOrNull("org.cefact:cii")).isNull();
        assertThat(ScenarioCoordinate.parseOrNull("a:b:c:d:e")).isNull();
        assertThat(ScenarioCoordinate.parseOrNull(null)).isNull();
        assertThatThrownBy(() -> ScenarioCoordinate.parseOrThrow("org.cefact:cii")).isInstanceOf(DVRCoordinateException.class);
    }

    @Test
    public void takesOverAnExistingDvrCoordinate() throws DVRException {
        final ScenarioCoordinate coordinate = ScenarioCoordinate.of(DVRCoordinate.create("org.peppol", "en16931-ubl", "2026.5"));
        assertThat(coordinate.getAsSingleID()).isEqualTo("org.peppol:en16931-ubl:2026.5");
        assertThat(coordinate.hasCoordinate()).isTrue();
    }

    @Test
    public void isAValueObject() {
        assertThat(ScenarioCoordinate.of("a", "b", "1.0")).isEqualTo(ScenarioCoordinate.of("a", "b", "1.0"))
                .hasSameHashCodeAs(ScenarioCoordinate.of("a", "b", "1.0"));
        assertThat(ScenarioCoordinate.of("a", "b", "1.0")).isNotEqualTo(ScenarioCoordinate.of("a", "b", "1.0.0"));
    }
}
