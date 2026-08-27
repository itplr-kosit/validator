package org.kosit.base.string;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import org.conformatron.api.annotation.Nonnegative;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class StringHelper {

    public static boolean isEmpty(final String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(final String s) {
        return s != null && !s.isEmpty();
    }

    public static boolean isBlank(final String s) {
        return s == null || s.isBlank();
    }

    public static boolean isNotBlank(final String s) {
        return s != null && !s.isBlank();
    }

    public static @Nullable String emptyToNull(final @Nullable String value) {
        return emptyToDefault(value, null);
    }

    public static @Nullable String emptyToDefault(final @Nullable String value, @Nullable final String _default) {
        return isEmpty(value) ? _default : value;
    }

    public static @Nullable String blankToNull(final @Nullable String value) {
        return blankToDefault(value, null);
    }

    public static @Nullable String blankToDefault(final @Nullable String value, @Nullable final String _default) {
        return isBlank(value) ? _default : value;
    }

    public static boolean equalsNullable(final @Nullable String left, final @Nullable String right) {
        return left == null ? right == null : left.equals(right);
    }

    public static long nvl(final @Nullable Long v) {
        return v == null ? 0L : v.longValue();
    }

    /**
     * Returns padding using the specified delimiter repeated to a given length.
     *
     * <pre>
     * repeat('e', 0)  = ""
     * repeat('e', 3)  = "eee"
     * repeat('e', -2) = ""
     * </pre>
     *
     *
     * @param repeat character to repeat.
     * @param count number of times to repeat char, negative treated as zero.
     * @return String with repeated character.
     */
    public static String repeat(final char repeat, final @Nonnegative int count) {
        if (count <= 0) {
            return "";
        }
        final char[] c = new char[count];
        Arrays.fill(c, repeat);
        return new String(c);
    }

    /**
     * Left pad a String with spaces (' ').
     *
     * <p>
     * The String is padded to the size of {@code size}.
     * </p>
     *
     * <pre>
     * leftPad(null, *)   = null
     * leftPad("", 3)     = "   "
     * leftPad("bat", 3)  = "bat"
     * leftPad("bat", 5)  = "  bat"
     * leftPad("bat", 1)  = "bat"
     * leftPad("bat", -1) = "bat"
     * </pre>
     *
     * @param str the String to pad out, may be null.
     * @param size the size to pad to.
     * @return left padded String or original String if no padding is necessary, {@code null} if null String input.
     */
    public static String leftPad(final @Nullable String str, final @Nonnegative int size) {
        return leftPad(str, size, ' ');
    }

    /**
     * Left pad a String with a specified character.
     *
     * <p>
     * Pad to a size of {@code size}.
     * </p>
     *
     * <pre>
     * leftPad(null, *, *)     = null
     * leftPad("", 3, 'z')     = "zzz"
     * leftPad("bat", 3, 'z')  = "bat"
     * leftPad("bat", 5, 'z')  = "zzbat"
     * leftPad("bat", 1, 'z')  = "bat"
     * leftPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str the String to pad out, may be null.
     * @param size the size to pad to.
     * @param padChar the character to pad with.
     * @return left padded String or original String if no padding is necessary, {@code null} if null String input.
     */
    public static String leftPad(final @Nullable String str, final @Nonnegative int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            // returns original String when possible
            return str;
        }
        return repeat(padChar, pads).concat(str);
    }

    /**
     * Centers a String in a larger String of size {@code size} using the space character (' ').
     *
     * <p>
     * If the size is less than the String length, the original String is returned. A {@code null} String returns
     * {@code null}. A negative size is treated as zero.
     * </p>
     *
     * <p>
     * Equivalent to {@code center(str, size, " ")}.
     * </p>
     *
     * <pre>
     * center(null, *)   = null
     * center("", 4)     = "    "
     * center("ab", -1)  = "ab"
     * center("ab", 4)   = " ab "
     * center("abcd", 2) = "abcd"
     * center("a", 4)    = " a  "
     * </pre>
     *
     * @param str the String to center, may be null.
     * @param size the int size of new String, negative treated as zero.
     * @return centered String, {@code null} if null String input.
     */
    public static String center(final String str, final int size) {
        return center(str, size, ' ');
    }

    /**
     * Centers a String in a larger String of size {@code size}. Uses a supplied character as the value to pad the
     * String with.
     *
     * <p>
     * If the size is less than the String length, the String is returned. A {@code null} String returns {@code null}. A
     * negative size is treated as zero.
     * </p>
     *
     * <pre>
     * center(null, *, *)     = null
     * center("", 4, ' ')     = "    "
     * center("ab", -1, ' ')  = "ab"
     * center("ab", 4, ' ')   = " ab "
     * center("abcd", 2, ' ') = "abcd"
     * center("a", 4, ' ')    = " a  "
     * center("a", 4, 'y')    = "yayy"
     * </pre>
     *
     * @param str the String to center, may be null.
     * @param size the int size of new String, negative treated as zero.
     * @param padChar the character to pad the new String with.
     * @return centered String, {@code null} if null String input.
     */
    public static String center(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int strLen = str.length();
        final int pads = size - strLen;
        if (pads <= 0) {
            // returns original String when possible
            return str;
        }
        return rightPad(leftPad(str, strLen + pads / 2, padChar), size, padChar);
    }

    /**
     * Right pad a String with spaces (' ').
     *
     * <p>
     * The String is padded to the size of {@code size}.
     * </p>
     *
     * <pre>
     * rightPad(null, *)   = null
     * rightPad("", 3)     = "   "
     * rightPad("bat", 3)  = "bat"
     * rightPad("bat", 5)  = "bat  "
     * rightPad("bat", 1)  = "bat"
     * rightPad("bat", -1) = "bat"
     * </pre>
     *
     * @param str the String to pad out, may be null.
     * @param size the size to pad to.
     * @return right padded String or original String if no padding is necessary, {@code null} if null String input.
     */
    public static String rightPad(final @Nullable String str, final @Nonnegative int size) {
        return rightPad(str, size, ' ');
    }

    /**
     * Right pad a String with a specified character.
     *
     * <p>
     * The String is padded to the size of {@code size}.
     * </p>
     *
     * <pre>
     * rightPad(null, *, *)     = null
     * rightPad("", 3, 'z')     = "zzz"
     * rightPad("bat", 3, 'z')  = "bat"
     * rightPad("bat", 5, 'z')  = "batzz"
     * rightPad("bat", 1, 'z')  = "bat"
     * rightPad("bat", -1, 'z') = "bat"
     * </pre>
     *
     * @param str the String to pad out, may be null.
     * @param size the size to pad to.
     * @param padChar the character to pad with.
     * @return right padded String or original String if no padding is necessary, {@code null} if null String input.
     */
    public static String rightPad(final @Nullable String str, final @Nonnegative int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            // returns original String when possible
            return str;
        }
        return str.concat(repeat(padChar, pads));
    }

    public static @NonNull String randomString(final @Nonnegative int charCount) {
        final char[] c = new char[charCount];
        final var rand = ThreadLocalRandom.current();
        for (int i = 0; i < charCount; ++i) {
            // Set each char in a loop
            while (true) {
                final int current = rand.nextInt(Character.MIN_VALUE, Character.MAX_VALUE);
                if (Character.isLetterOrDigit(current)) {
                    c[i] = (char) current;
                    break;
                }
            }
        }
        return new String(c);
    }

    private StringHelper() {
    }

}
