package org.kosit.validator.impl.conformatron.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.junit.jupiter.api.Test;
import org.kosit.validator.scenario.v2.CreateReportType;
import org.kosit.validator.scenario.v2.CustomErrorLevel;
import org.kosit.validator.scenario.v2.ErrorLevelType;
import org.kosit.validator.scenario.v2.ScenarioType;

/**
 * Tests {@link SeverityOverrides}: the customLevel semantics carried over from 1.x (token lists,
 * both directions, level mapping into the severity model).
 */
public class SeverityOverridesTest
{

  private static ScenarioType configuration (final CustomErrorLevel... levels)
  {
    final CreateReportType report = new CreateReportType ();
    for (final CustomErrorLevel level : levels)
    {
      report.getCustomLevel ().add (level);
    }
    final ScenarioType scenario = new ScenarioType ();
    scenario.getCreateReport ().add (report);
    return scenario;
  }

  private static CustomErrorLevel level (final ErrorLevelType level, final String... codes)
  {
    final CustomErrorLevel custom = new CustomErrorLevel ();
    custom.setLevel (level);
    for (final String code : codes)
    {
      custom.getValue ().add (code);
    }
    return custom;
  }

  @Test
  public void testLevelMappingCoversBothDirections ()
  {
    final SeverityOverrides overrides = configurationOverrides ();

    // downgrade: fatal/error rule demoted to information -> NONE
    assertThat (overrides.effectiveFor ("BR-CL-10")).isEqualTo (CTStandardSeverity.NONE);
    // downgrade to warning
    assertThat (overrides.effectiveFor ("BR-CL-23")).isEqualTo (CTStandardSeverity.WARNING);
    // upgrade: warning rule promoted to error (UBL-CR-646 / CII-SR-* pattern)
    assertThat (overrides.effectiveFor ("UBL-CR-646")).isEqualTo (CTStandardSeverity.ERROR);
    // no override declared -> null, declared severity stands
    assertThat (overrides.effectiveFor ("BR-DE-01")).isNull ();
    assertThat (overrides.effectiveFor (null)).isNull ();
  }

  @Test
  public void testTokenListsExpandToOneOverridePerCode ()
  {
    final SeverityOverrides overrides = SeverityOverrides.fromConfiguration (configuration (level (ErrorLevelType.WARNING,
                                                                                                   "BR-CL-21",
                                                                                                   "BR-CL-23",
                                                                                                   "BR-CL-24")));

    assertThat (overrides.effectiveFor ("BR-CL-21")).isEqualTo (CTStandardSeverity.WARNING);
    assertThat (overrides.effectiveFor ("BR-CL-23")).isEqualTo (CTStandardSeverity.WARNING);
    assertThat (overrides.effectiveFor ("BR-CL-24")).isEqualTo (CTStandardSeverity.WARNING);
  }

  @Test
  public void testScenarioWithoutOverridesYieldsNone ()
  {
    assertThat (SeverityOverrides.fromConfiguration (new ScenarioType ())).isSameAs (SeverityOverrides.NONE);
    assertThat (SeverityOverrides.fromConfiguration (null)).isSameAs (SeverityOverrides.NONE);
    assertThat (SeverityOverrides.of (null)).isSameAs (SeverityOverrides.NONE);
    assertThat (SeverityOverrides.NONE.isEmpty ()).isTrue ();
  }

  private static SeverityOverrides configurationOverrides ()
  {
    return SeverityOverrides.fromConfiguration (configuration (level (ErrorLevelType.INFORMATION, "BR-CL-10"),
                                                               level (ErrorLevelType.WARNING, "BR-CL-23"),
                                                               level (ErrorLevelType.ERROR, "UBL-CR-646")));
  }
}
