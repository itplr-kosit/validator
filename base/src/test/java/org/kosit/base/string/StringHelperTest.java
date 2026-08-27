package org.kosit.base.string;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import org.junit.jupiter.api.Test;

public class StringHelperTest {

    @Test
    public void isEmptyAndIsNotEmpty() {
        assertThat(StringHelper.isEmpty(null)).isTrue();
        assertThat(StringHelper.isEmpty("")).isTrue();
        assertThat(StringHelper.isEmpty(" ")).isFalse();
        assertThat(StringHelper.isEmpty("a")).isFalse();

        assertThat(StringHelper.isNotEmpty(null)).isFalse();
        assertThat(StringHelper.isNotEmpty("")).isFalse();
        assertThat(StringHelper.isNotEmpty(" ")).isTrue();
        assertThat(StringHelper.isNotEmpty("a")).isTrue();
    }

    @Test
    public void isBlankAndIsNotBlank() {
        assertThat(StringHelper.isBlank(null)).isTrue();
        assertThat(StringHelper.isBlank("")).isTrue();
        assertThat(StringHelper.isBlank(" \t\n")).isTrue();
        assertThat(StringHelper.isBlank("a")).isFalse();

        assertThat(StringHelper.isNotBlank(null)).isFalse();
        assertThat(StringHelper.isNotBlank("")).isFalse();
        assertThat(StringHelper.isNotBlank(" \t\n")).isFalse();
        assertThat(StringHelper.isNotBlank("a")).isTrue();
    }

    @Test
    public void emptyToNullAndEmptyToDefault() {
        assertThat(StringHelper.emptyToNull(null)).isNull();
        assertThat(StringHelper.emptyToNull("")).isNull();
        assertThat(StringHelper.emptyToNull(" ")).isEqualTo(" ");
        assertThat(StringHelper.emptyToNull("a")).isEqualTo("a");

        assertThat(StringHelper.emptyToDefault(null, "def")).isEqualTo("def");
        assertThat(StringHelper.emptyToDefault("", "def")).isEqualTo("def");
        assertThat(StringHelper.emptyToDefault(" ", "def")).isEqualTo(" ");
        assertThat(StringHelper.emptyToDefault("a", "def")).isEqualTo("a");
        assertThat(StringHelper.emptyToDefault("", null)).isNull();
    }

    @Test
    public void blankToNullAndBlankToDefault() {
        assertThat(StringHelper.blankToNull(null)).isNull();
        assertThat(StringHelper.blankToNull("")).isNull();
        assertThat(StringHelper.blankToNull(" \t")).isNull();
        assertThat(StringHelper.blankToNull("a")).isEqualTo("a");

        assertThat(StringHelper.blankToDefault(null, "def")).isEqualTo("def");
        assertThat(StringHelper.blankToDefault("", "def")).isEqualTo("def");
        assertThat(StringHelper.blankToDefault(" \t", "def")).isEqualTo("def");
        assertThat(StringHelper.blankToDefault("a", "def")).isEqualTo("a");
        assertThat(StringHelper.blankToDefault(" ", null)).isNull();
    }

    @Test
    public void equalsNullable() {
        assertThat(Objects.equals(null, null)).isTrue();
        assertThat(Objects.equals(null, "a")).isFalse();
        assertThat(Objects.equals("a", null)).isFalse();
        assertThat(Objects.equals("a", "a")).isTrue();
        assertThat(Objects.equals("a", new String("a"))).isTrue();
        assertThat(Objects.equals("a", "A")).isFalse();
        assertThat(Objects.equals("", "")).isTrue();
    }

    @Test
    public void normalizeBlankToNull() {
        assertThat(StringHelper.blankToNull(null)).isNull();
        assertThat(StringHelper.blankToNull("")).isNull();
        assertThat(StringHelper.blankToNull(" \t\n")).isNull();
        assertThat(StringHelper.blankToNull(" a ")).isEqualTo(" a ");
    }

    @Test
    public void nvl() {
        assertThat(StringHelper.nvl(null)).isZero();
        assertThat(StringHelper.nvl(Long.valueOf(0))).isZero();
        assertThat(StringHelper.nvl(Long.valueOf(42))).isEqualTo(42L);
        assertThat(StringHelper.nvl(Long.valueOf(-7))).isEqualTo(-7L);
    }

    @Test
    public void repeat() {
        assertThat(StringHelper.repeat('e', 0)).isEmpty();
        assertThat(StringHelper.repeat('e', 3)).isEqualTo("eee");
        assertThat(StringHelper.repeat('e', -2)).isEmpty();
        assertThat(StringHelper.repeat(' ', 2)).isEqualTo("  ");
    }

    @Test
    public void leftPadWithSpaces() {
        assertThat(StringHelper.leftPad(null, 3)).isNull();
        assertThat(StringHelper.leftPad("", 3)).isEqualTo("   ");
        assertThat(StringHelper.leftPad("bat", 3)).isEqualTo("bat");
        assertThat(StringHelper.leftPad("bat", 5)).isEqualTo("  bat");
        assertThat(StringHelper.leftPad("bat", 1)).isEqualTo("bat");
        assertThat(StringHelper.leftPad("bat", -1)).isEqualTo("bat");
    }

    @Test
    public void leftPadWithChar() {
        assertThat(StringHelper.leftPad(null, 3, 'z')).isNull();
        assertThat(StringHelper.leftPad("", 3, 'z')).isEqualTo("zzz");
        assertThat(StringHelper.leftPad("bat", 3, 'z')).isEqualTo("bat");
        assertThat(StringHelper.leftPad("bat", 5, 'z')).isEqualTo("zzbat");
        assertThat(StringHelper.leftPad("bat", 1, 'z')).isEqualTo("bat");
        assertThat(StringHelper.leftPad("bat", -1, 'z')).isEqualTo("bat");
    }

    @Test
    public void rightPadWithSpaces() {
        assertThat(StringHelper.rightPad(null, 3)).isNull();
        assertThat(StringHelper.rightPad("", 3)).isEqualTo("   ");
        assertThat(StringHelper.rightPad("bat", 3)).isEqualTo("bat");
        assertThat(StringHelper.rightPad("bat", 5)).isEqualTo("bat  ");
        assertThat(StringHelper.rightPad("bat", 1)).isEqualTo("bat");
        assertThat(StringHelper.rightPad("bat", -1)).isEqualTo("bat");
    }

    @Test
    public void rightPadWithChar() {
        assertThat(StringHelper.rightPad(null, 3, 'z')).isNull();
        assertThat(StringHelper.rightPad("", 3, 'z')).isEqualTo("zzz");
        assertThat(StringHelper.rightPad("bat", 3, 'z')).isEqualTo("bat");
        assertThat(StringHelper.rightPad("bat", 5, 'z')).isEqualTo("batzz");
        assertThat(StringHelper.rightPad("bat", 1, 'z')).isEqualTo("bat");
        assertThat(StringHelper.rightPad("bat", -1, 'z')).isEqualTo("bat");
    }

    @Test
    public void centerWithSpaces() {
        assertThat(StringHelper.center(null, 4)).isNull();
        assertThat(StringHelper.center("", 4)).isEqualTo("    ");
        assertThat(StringHelper.center("ab", -1)).isEqualTo("ab");
        assertThat(StringHelper.center("ab", 4)).isEqualTo(" ab ");
        assertThat(StringHelper.center("abcd", 2)).isEqualTo("abcd");
        assertThat(StringHelper.center("a", 4)).isEqualTo(" a  ");
    }

    @Test
    public void centerWithChar() {
        assertThat(StringHelper.center(null, 4, 'y')).isNull();
        assertThat(StringHelper.center("", 4, ' ')).isEqualTo("    ");
        assertThat(StringHelper.center("ab", -1, ' ')).isEqualTo("ab");
        assertThat(StringHelper.center("ab", 4, ' ')).isEqualTo(" ab ");
        assertThat(StringHelper.center("abcd", 2, ' ')).isEqualTo("abcd");
        assertThat(StringHelper.center("a", 4, ' ')).isEqualTo(" a  ");
        assertThat(StringHelper.center("a", 4, 'y')).isEqualTo("yayy");
    }

    @Test
    public void randomString() {
        assertThat(StringHelper.randomString(0)).isEmpty();
        for (int len = 1; len <= 20; ++len) {
            final String s = StringHelper.randomString(len);
            assertThat(s).hasSize(len);
            for (final char c : s.toCharArray()) {
                assertThat(Character.isLetterOrDigit(c)).as("'" + c + "' is a letter or digit").isTrue();
            }
        }
        // Two subsequent calls of a reasonable length should differ
        assertThat(StringHelper.randomString(32)).isNotEqualTo(StringHelper.randomString(32));
    }
}
