package org.kosit.validator.impl.conformatron.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.kosit.cvr.model.SeverityOverrides;
import org.kosit.validator.scenario.generic.EScenarioErrorLevel;
import org.kosit.validator.scenario.generic.EScenarioKind;
import org.kosit.validator.scenario.generic.Scenario;
import org.kosit.validator.scenario.generic.ScenarioCustomErrorLevel;
import org.kosit.validator.scenario.generic.ScenarioResource;
import org.kosit.validator.scenario.generic.ScenarioSchematron;

/**
 * Tests {@link ScenarioSeverityOverrides}: the customLevel semantics carried over from 1.x (token lists, both
 * directions, level mapping into the severity model), declared with the rule set.
 */
public class ScenarioSeverityOverridesTest {

    private static Scenario declaration(final ScenarioCustomErrorLevel... levels) {
        final ScenarioSchematron schematron = new ScenarioSchematron(new ScenarioResource("rules").setLocation("rules.xsl"));
        for (final ScenarioCustomErrorLevel level : levels) {
            schematron.addCustomLevel(level);
        }
        return new Scenario(EScenarioKind.XML, "scenario").addSchematron(schematron);
    }

    private static ScenarioCustomErrorLevel level(final EScenarioErrorLevel level, final String... codes) {
        final ScenarioCustomErrorLevel custom = new ScenarioCustomErrorLevel(level);
        for (final String code : codes) {
            custom.addRuleID(code);
        }
        return custom;
    }

    @Test
    public void testLevelMappingCoversBothDirections() {
        final SeverityOverrides overrides = configurationOverrides();

        // downgrade: fatal/error rule demoted to information -> NONE
        assertThat(overrides.effectiveFor("BR-CL-10")).isEqualTo(CTStandardSeverity.NONE);
        // downgrade to warning
        assertThat(overrides.effectiveFor("BR-CL-23")).isEqualTo(CTStandardSeverity.WARNING);
        // upgrade: warning rule promoted to error (UBL-CR-646 / CII-SR-* pattern)
        assertThat(overrides.effectiveFor("UBL-CR-646")).isEqualTo(CTStandardSeverity.ERROR);
        // no override declared -> null, declared severity stands
        assertThat(overrides.effectiveFor("BR-DE-01")).isNull();
        assertThat(overrides.effectiveFor(null)).isNull();
    }

    @Test
    public void testTokenListsExpandToOneOverridePerCode() {
        final SeverityOverrides overrides = ScenarioSeverityOverrides
                .fromDeclaration(declaration(level(EScenarioErrorLevel.WARNING, "BR-CL-21", "BR-CL-23", "BR-CL-24")));

        assertThat(overrides.effectiveFor("BR-CL-21")).isEqualTo(CTStandardSeverity.WARNING);
        assertThat(overrides.effectiveFor("BR-CL-23")).isEqualTo(CTStandardSeverity.WARNING);
        assertThat(overrides.effectiveFor("BR-CL-24")).isEqualTo(CTStandardSeverity.WARNING);
    }

    @Test
    public void testOverridesOfAllRuleSetsAreCollected() {
        final Scenario scenario = declaration(level(EScenarioErrorLevel.WARNING, "BR-CL-23"));
        final ScenarioSchematron second = new ScenarioSchematron(new ScenarioResource("more").setLocation("more.xsl"));
        second.addCustomLevel(level(EScenarioErrorLevel.ERROR, "UBL-CR-646"));
        scenario.addSchematron(second);

        final SeverityOverrides overrides = ScenarioSeverityOverrides.fromDeclaration(scenario);

        assertThat(overrides.effectiveFor("BR-CL-23")).isEqualTo(CTStandardSeverity.WARNING);
        assertThat(overrides.effectiveFor("UBL-CR-646")).isEqualTo(CTStandardSeverity.ERROR);
    }

    @Test
    public void testScenarioWithoutOverridesYieldsNone() {
        assertThat(ScenarioSeverityOverrides.fromDeclaration(new Scenario(EScenarioKind.XML, "empty"))).isSameAs(SeverityOverrides.NONE);
        assertThat(ScenarioSeverityOverrides.fromDeclaration(null)).isSameAs(SeverityOverrides.NONE);
        assertThat(ScenarioSeverityOverrides.of(null)).isSameAs(SeverityOverrides.NONE);
        assertThat(SeverityOverrides.NONE.isEmpty()).isTrue();
    }

    private static SeverityOverrides configurationOverrides() {
        return ScenarioSeverityOverrides.fromDeclaration(declaration(level(EScenarioErrorLevel.INFORMATION, "BR-CL-10"),
                level(EScenarioErrorLevel.WARNING, "BR-CL-23"), level(EScenarioErrorLevel.ERROR, "UBL-CR-646")));
    }
}
