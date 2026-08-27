package org.kosit.base.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.conformatron.api.annotation.Nonnegative;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class StringHelper {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

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

    /**
     * @param cs the character sequence to check. May be <code>null</code>.
     * @return the length of the passed character sequence. 0 if it is <code>null</code>.
     */
    public static @Nonnegative int getLength(final @Nullable CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    /**
     * Take a concatenated String and return an array of all elements of the passed string, using the specified
     * separator char.
     *
     * @param sep the separator to use.
     * @param elements the concatenated String to convert. May be <code>null</code> or empty.
     * @return the exploded array. Never <code>null</code>. Empty if the passed string is <code>null</code> or empty.
     */
    public static @NonNull String[] getExplodedArray(final char sep, final @Nullable String elements) {
        return getExplodedArray(sep, elements, -1);
    }

    /**
     * Take a concatenated String and return an array of all elements of the passed string, using the specified
     * separator char.
     *
     * @param sep the separator to use.
     * @param elements the concatenated String to convert. May be <code>null</code> or empty.
     * @param maxItems the maximum number of items to explode. If the passed value is &le; 0 all items are used. If max
     *            items is 1, the passed string is returned as is. If max items is larger than the number of elements
     *            found, it has no effect.
     * @return the exploded array. Never <code>null</code>. Empty if the passed string is <code>null</code> or empty.
     */
    public static @NonNull String[] getExplodedArray(final char sep, final @Nullable String elements, final int maxItems) {
        if (maxItems == 1) {
            return new String[] { elements };
        }
        if (isEmpty(elements)) {
            return EMPTY_STRING_ARRAY;
        }

        int sepCount = 0;
        for (int i = 0; i < elements.length(); ++i) {
            if (elements.charAt(i) == sep) {
                ++sepCount;
            }
        }
        if (sepCount == 0) {
            // Separator not found
            return new String[] { elements };
        }

        final int maxResultElements = 1 + sepCount;
        final String[] ret = new String[maxItems < 1 ? maxResultElements : Math.min(maxResultElements, maxItems)];

        // Do not use String.split because it trims empty tokens from the end
        int startIndex = 0;
        int itemsAdded = 0;
        while (true) {
            final int matchIndex = elements.indexOf(sep, startIndex);
            if (matchIndex < 0) {
                break;
            }
            ret[itemsAdded++] = elements.substring(startIndex, matchIndex);
            // 1 == length of separator char
            startIndex = matchIndex + 1;
            if (maxItems > 0 && itemsAdded == maxItems - 1) {
                // Exactly one item is left: the rest of the string
                break;
            }
        }
        ret[itemsAdded] = elements.substring(startIndex);
        return ret;
    }

    /**
     * Take a concatenated String and return a {@link List} of all elements of the passed string, using the specified
     * separator char.
     *
     * @param sep the separator to use.
     * @param elements the concatenated String to convert. May be <code>null</code> or empty.
     * @return the exploded list. Never <code>null</code>. Empty if the passed string is <code>null</code> or empty.
     */
    public static @NonNull List<String> getExploded(final char sep, final @Nullable String elements) {
        return new ArrayList<>(Arrays.asList(getExplodedArray(sep, elements)));
    }

    /**
     * @param str the String to check. May be <code>null</code>.
     * @return <code>true</code> if the passed String can be parsed to an <code>int</code>.
     */
    public static boolean isInt(final @Nullable String str) {
        return parseIntObj(str) != null;
    }

    /**
     * Parse the given String as an <code>int</code>, without throwing an exception on invalid input.
     *
     * @param str the String to parse. May be <code>null</code>.
     * @param defaultValue the value to be returned if the String cannot be converted to a valid value.
     * @return the passed default value if the String does not represent a valid <code>int</code>.
     */
    public static int parseInt(final @Nullable String str, final int defaultValue) {
        final Integer ret = parseIntObj(str);
        return ret == null ? defaultValue : ret.intValue();
    }

    /**
     * Parse the given String as an {@link Integer}, without throwing an exception on invalid input.
     *
     * @param str the String to parse. May be <code>null</code>.
     * @return <code>null</code> if the String does not represent a valid <code>int</code>.
     */
    public static @Nullable Integer parseIntObj(final @Nullable String str) {
        if (isNotEmpty(str)) {
            try {
                return Integer.valueOf(str);
            } catch (final NumberFormatException ex) {
                // Fall through
            }
        }
        return null;
    }

    private StringHelper() {
    }

}
