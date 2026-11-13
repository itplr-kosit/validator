package org.kosit.validator.scenario.convert;

import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.scenario.v3.Scenarios;

/**
 * The result of a migration of a scenario configuration to version 3.
 *
 * @param outcome how the migration ended. May not be <code>null</code>.
 * @param rootNamespace the namespace of the root element of the source. May be <code>null</code> if the source has
 *            none, or is no XML at all.
 * @param proposals the coordinates proposed for what the source leaves without one, whether they were written in or
 *            not. May not be <code>null</code>, but may be empty.
 * @param findings what the mapping to version 3 reported, errors and warnings. May not be <code>null</code>, but may be
 *            empty.
 * @param converted the configuration of version 3. Only present for {@link EMigrationOutcome#CONVERTED}, otherwise
 *            <code>null</code>.
 * @author Andreas Schmitz
 */
public record MigrationResult(@NonNull EMigrationOutcome outcome, @Nullable String rootNamespace,
        @NonNull List<CoordinateProposal> proposals, @NonNull List<SimpleError> findings, @Nullable Scenarios converted) {

    public MigrationResult {
        proposals = List.copyOf(proposals);
        findings = List.copyOf(findings);
    }

    /**
     * @return <code>true</code> if the configuration was converted, so that {@link #converted()} is present.
     */
    public boolean isConverted() {
        return this.outcome == EMigrationOutcome.CONVERTED;
    }
}
