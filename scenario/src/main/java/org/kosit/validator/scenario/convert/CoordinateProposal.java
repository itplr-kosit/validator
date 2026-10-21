package org.kosit.validator.scenario.convert;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.kosit.validator.scenario.generic.ScenarioCoordinate;

/**
 * A coordinate the conversion proposes for something that has none. Scenario configuration version 2 may leave the
 * coordinate off, version 3 requires it, so converting an old configuration has to fill the gaps - and the coordinate
 * is what the report identifies the scenario and its artifacts by, so it is a proposal for a human to confirm and never
 * a value the conversion quietly settles on.
 *
 * @param subject what the coordinate is proposed for, as a reader of the configuration recognizes it, e.g.
 *            {@code "the scenario 'EN16931 XRechnung (UBL Invoice)'"}
 * @param coordinate the proposed coordinate
 * @author Andreas Schmitz
 */
public record CoordinateProposal(@NonNull @Nonempty String subject, @NonNull ScenarioCoordinate coordinate) {

    @Override
    public String toString() {
        return this.subject + " -> " + this.coordinate.getAsSingleID();
    }
}
