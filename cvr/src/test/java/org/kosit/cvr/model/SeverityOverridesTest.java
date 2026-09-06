package org.kosit.cvr.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link SeverityOverrides} as the plain detection code to severity map it is. Reading the overrides out of a
 * scenario configuration is covered by {@code ScenarioSeverityOverridesTest} in {@code validator-core}.
 */
public class SeverityOverridesTest {

    @Test
    public void testEffectiveForReturnsTheOverriddenSeverity() {
        final SeverityOverrides overrides = SeverityOverrides.of(Map.of("BR-CL-10", CTStandardSeverity.NONE, "BR-CL-23",
                CTStandardSeverity.WARNING, "UBL-CR-646", CTStandardSeverity.ERROR));

        assertThat(overrides.effectiveFor("BR-CL-10")).isEqualTo(CTStandardSeverity.NONE);
        assertThat(overrides.effectiveFor("BR-CL-23")).isEqualTo(CTStandardSeverity.WARNING);
        assertThat(overrides.effectiveFor("UBL-CR-646")).isEqualTo(CTStandardSeverity.ERROR);
        // no override declared -> null, the declared severity stands
        assertThat(overrides.effectiveFor("BR-DE-01")).isNull();
        assertThat(overrides.effectiveFor(null)).isNull();
        assertThat(overrides.isEmpty()).isFalse();
    }

    @Test
    public void testEmptyAndNullYieldNone() {
        assertThat(SeverityOverrides.of(null)).isSameAs(SeverityOverrides.NONE);
        assertThat(SeverityOverrides.of(Map.of())).isSameAs(SeverityOverrides.NONE);
        assertThat(SeverityOverrides.NONE.isEmpty()).isTrue();
        assertThat(SeverityOverrides.NONE.effectiveFor("BR-CL-10")).isNull();
    }

    @Test
    public void testTheOverridesAreCopiedOnCreation() {
        final Map<String, CTStandardSeverity> source = new LinkedHashMap<>();
        source.put("BR-CL-10", CTStandardSeverity.NONE);
        final SeverityOverrides overrides = SeverityOverrides.of(source);
        source.put("BR-CL-23", CTStandardSeverity.WARNING);

        assertThat(overrides.effectiveFor("BR-CL-23")).isNull();
    }
}
