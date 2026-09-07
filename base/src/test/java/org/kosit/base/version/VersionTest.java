package org.kosit.base.version;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

public class VersionTest {

    @Test
    public void constructors() {
        assertThat(new Version(1).getMajor()).isEqualTo(1);
        assertThat(new Version(1).getMinor()).isZero();
        assertThat(new Version(1).getMicro()).isZero();
        assertThat(new Version(1).getQualifier()).isNull();
        assertThat(new Version(1).hasQualifier()).isFalse();

        final Version v = new Version(1, 2, 3, "RC1");
        assertThat(v.getMajor()).isEqualTo(1);
        assertThat(v.getMinor()).isEqualTo(2);
        assertThat(v.getMicro()).isEqualTo(3);
        assertThat(v.getQualifier()).isEqualTo("RC1");
        assertThat(v.hasQualifier()).isTrue();
    }

    @Test
    public void anEmptyQualifierIsNoQualifier() {
        assertThat(new Version(1, 2, 3, "").getQualifier()).isNull();
        assertThat(new Version(1, 2, 3, "").hasQualifier()).isFalse();
    }

    @Test
    public void negativeNumbersAreRejected() {
        assertThatThrownBy(() -> new Version(-1)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Major");
        assertThatThrownBy(() -> new Version(1, -1)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Minor");
        assertThatThrownBy(() -> new Version(1, 2, -1)).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Micro");
    }

    @Test
    public void getAsString() {
        assertThat(new Version(1, 2, 3).getAsString()).isEqualTo("1.2.3");
        assertThat(new Version(1, 2, 0).getAsString()).isEqualTo("1.2");
        assertThat(new Version(1, 0, 0).getAsString()).isEqualTo("1");
        assertThat(new Version(0, 0, 0).getAsString()).isEqualTo(Version.DEFAULT_VERSION_STRING);
        assertThat(new Version(1, 0, 3).getAsString()).isEqualTo("1.0.3");
        assertThat(new Version(1, 0, 0, "RC1").getAsString()).isEqualTo("1.0.0.RC1");
    }

    @Test
    public void getAsStringWithZeroElements() {
        assertThat(new Version(1, 0, 0).getAsString(true)).isEqualTo("1.0.0");
        assertThat(new Version(0, 0, 0).getAsString(true)).isEqualTo("0.0.0");
        assertThat(new Version(1, 2, 0).getAsString(true)).isEqualTo("1.2.0");
    }

    @Test
    public void getAsStringWithAtLeastMajorAndMinor() {
        assertThat(new Version(1, 0, 0).getAsString(false, true)).isEqualTo("1.0");
        assertThat(new Version(0, 0, 0).getAsString(false, true)).isEqualTo("0.0");
        assertThat(new Version(1, 2, 3).getAsString(false, true)).isEqualTo("1.2.3");
    }

    @Test
    public void getAsStringStrict() {
        assertThat(new Version(1, 0, 0).getAsStringStrict()).isEqualTo("1");
        assertThat(new Version(1, 2, 0).getAsStringStrict()).isEqualTo("1.2");
        assertThat(new Version(1, 2, 3).getAsStringStrict()).isEqualTo("1.2.3");
        assertThat(new Version(1, 0, 3).getAsStringStrict()).isEqualTo("1.0.3");
        assertThat(new Version(0, 0, 0).getAsStringStrict()).isEqualTo("0");
        assertThat(new Version(1, 2, 3, "RC1").getAsStringStrict()).isEqualTo("1.2.3-RC1");
        assertThat(new Version(1, 0, 0, "SNAPSHOT").getAsStringStrict()).isEqualTo("1-SNAPSHOT");
    }

    @Test
    public void parseNumericParts() {
        assertThat(Version.parse("1")).isEqualTo(new Version(1));
        assertThat(Version.parse("1.2")).isEqualTo(new Version(1, 2));
        assertThat(Version.parse("1.2.3")).isEqualTo(new Version(1, 2, 3));
        assertThat(Version.parse("  1.2.3  ")).isEqualTo(new Version(1, 2, 3));
    }

    @Test
    public void parseEmptyGivesTheDefaultVersion() {
        assertThat(Version.parse(null)).isEqualTo(Version.DEFAULT_VERSION);
        assertThat(Version.parse("")).isEqualTo(Version.DEFAULT_VERSION);
        assertThat(Version.parse("   ")).isEqualTo(Version.DEFAULT_VERSION);
    }

    @Test
    public void parseQualifier() {
        assertThat(Version.parse("1.2.3.RC1")).isEqualTo(new Version(1, 2, 3, "RC1"));
        assertThat(Version.parse("1.2.3-RC1")).isEqualTo(new Version(1, 2, 3, "RC1"));
        assertThat(Version.parse("0-RC1")).isEqualTo(new Version(0, 0, 0, "RC1"));
        // a non numeric major part makes the whole String the qualifier
        assertThat(Version.parse("bla")).isEqualTo(new Version(0, 0, 0, "bla"));
        assertThat(Version.parse("1.bla")).isEqualTo(new Version(1, 0, 0, "bla"));
        assertThat(Version.parse("1.2.bla")).isEqualTo(new Version(1, 2, 0, "bla"));
    }

    @Test
    public void parseTakesANumericQualifierAsTheMicroVersion() {
        // documented difference to parseStrictOrNull
        assertThat(Version.parse("1.4-03")).isEqualTo(new Version(1, 4, 3));
        assertThat(Version.parseStrictOrNull("1.4-03")).isEqualTo(new Version(1, 4, 0, "03"));
    }

    @Test
    public void parseRejectsNegativeNumbers() {
        assertThatThrownBy(() -> Version.parse("-1")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void parseStrict() {
        assertThat(Version.parseStrictOrNull("1")).isEqualTo(new Version(1));
        assertThat(Version.parseStrictOrNull("1.0")).isEqualTo(new Version(1));
        assertThat(Version.parseStrictOrNull("1.0.0")).isEqualTo(new Version(1));
        assertThat(Version.parseStrictOrNull("1.2.3")).isEqualTo(new Version(1, 2, 3));
        assertThat(Version.parseStrictOrNull(" 1.2.3 ")).isEqualTo(new Version(1, 2, 3));
        assertThat(Version.parseStrictOrNull("1.2.3-RC1")).isEqualTo(new Version(1, 2, 3, "RC1"));
        // only the first separator introduces the qualifier
        assertThat(Version.parseStrictOrNull("1-a-b")).isEqualTo(new Version(1, 0, 0, "a-b"));
        assertThat(Version.parseStrictOrNull("0")).isEqualTo(Version.DEFAULT_VERSION);
    }

    @Test
    public void parseStrictRejectsInvalidInput() {
        assertThat(Version.parseStrictOrNull(null)).isNull();
        assertThat(Version.parseStrictOrNull("")).isNull();
        assertThat(Version.parseStrictOrNull("   ")).isNull();
        // superfluous leading zeroes
        assertThat(Version.parseStrictOrNull("1.04")).isNull();
        assertThat(Version.parseStrictOrNull("01")).isNull();
        // too many numeric parts
        assertThat(Version.parseStrictOrNull("1.2.3.4")).isNull();
        // empty qualifier or empty numeric part
        assertThat(Version.parseStrictOrNull("1.2-")).isNull();
        assertThat(Version.parseStrictOrNull("-bla")).isNull();
        // not numeric at all
        assertThat(Version.parseStrictOrNull("1.2.x")).isNull();
        assertThat(Version.parseStrictOrNull("bla")).isNull();
        // int overflow
        assertThat(Version.parseStrictOrNull("99999999999")).isNull();
    }

    @Test
    public void strictLayoutIsARoundTrip() {
        for (final Version v : new Version[] { new Version(0), new Version(1), new Version(1, 2), new Version(1, 2, 3),
                new Version(1, 0, 3), new Version(1, 2, 3, "RC1"), new Version(1, 0, 0, "SNAPSHOT"), new Version(1, 0, 0, "a-b") }) {
            assertThat(Version.parseStrictOrNull(v.getAsStringStrict())).as(v.getAsStringStrict()).isEqualTo(v);
        }
    }

    @Test
    public void compareTo() {
        assertThat(new Version(1, 2, 3)).isEqualByComparingTo(new Version(1, 2, 3));
        assertThat(new Version(1, 2, 3)).isLessThan(new Version(2, 0, 0));
        assertThat(new Version(1, 2, 3)).isLessThan(new Version(1, 3, 0));
        assertThat(new Version(1, 2, 3)).isLessThan(new Version(1, 2, 4));
        assertThat(new Version(2, 0, 0)).isGreaterThan(new Version(1, 9, 9));
    }

    @Test
    public void compareToWithQualifier() {
        // no qualifier is smaller than any qualifier
        assertThat(new Version(1, 2, 3)).isLessThan(new Version(1, 2, 3, "RC1"));
        assertThat(new Version(1, 2, 3, "RC1")).isGreaterThan(new Version(1, 2, 3));
        assertThat(new Version(1, 2, 3, "RC1")).isLessThan(new Version(1, 2, 3, "RC2"));
        assertThat(new Version(1, 2, 3, "RC1")).isEqualByComparingTo(new Version(1, 2, 3, "RC1"));
    }

    @Test
    public void equalsAndHashCode() {
        final Version v = new Version(1, 2, 3, "RC1");
        assertThat(v).isEqualTo(v);
        assertThat(v).isEqualTo(new Version(1, 2, 3, "RC1"));
        assertThat(v).hasSameHashCodeAs(new Version(1, 2, 3, "RC1"));
        assertThat(v).isNotEqualTo(new Version(1, 2, 3));
        assertThat(v).isNotEqualTo(new Version(1, 2, 4, "RC1"));
        assertThat(v).isNotEqualTo(null);
        assertThat(v).isNotEqualTo("1.2.3-RC1");
    }

    @Test
    public void asString() {
        assertThat(new Version(1, 2, 3, "RC1").toString()).contains("major=1").contains("minor=2").contains("micro=3")
                .contains("qualifier=RC1");
        assertThat(new Version(1, 2, 3).toString()).doesNotContain("qualifier");
    }
}
