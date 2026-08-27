package org.kosit.base.version;

import java.util.Objects;

import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.annotation.Nonnegative;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.string.StringHelper;

/**
 * This class represents a single version object. It supports 4 elements: major version (integer), minor version
 * (integer), micro version (integer) and a qualifier (string).
 *
 * @author Philip Helger
 */
public class Version implements Comparable<Version> {

    /** Default version if nothing is specified. */
    public static final String DEFAULT_VERSION_STRING = "0";

    /** The version used if an empty String is parsed. */
    public static final Version DEFAULT_VERSION = new Version(0, 0, 0, null);

    /** Default value for printing zero elements in getAsString. */
    public static final boolean DEFAULT_PRINT_ZERO_ELEMENTS = false;

    /** The character that separates the qualifier from the numeric version parts in the strict layout. */
    public static final char STRICT_QUALIFIER_SEPARATOR = '-';

    /** The character that separates the numeric version parts from each other. */
    public static final char NUMERIC_PART_SEPARATOR = '.';

    private final int major;

    private final int minor;

    private final int micro;

    private final @Nullable String qualifier;

    /**
     * Create a new version with major version only.
     *
     * @param major the major version.
     * @throws IllegalArgumentException if the parameter is &lt; 0.
     */
    public Version(final @Nonnegative int major) {
        this(major, 0, 0, null);
    }

    /**
     * Create a new version with major and minor version only.
     *
     * @param major the major version.
     * @param minor the minor version.
     * @throws IllegalArgumentException if any of the parameters is &lt; 0.
     */
    public Version(final @Nonnegative int major, final @Nonnegative int minor) {
        this(major, minor, 0, null);
    }

    /**
     * Create a new version with major, minor and micro version number. The qualifier remains <code>null</code>.
     *
     * @param major the major version.
     * @param minor the minor version.
     * @param micro the micro version.
     * @throws IllegalArgumentException if any of the parameters is &lt; 0.
     */
    public Version(final @Nonnegative int major, final @Nonnegative int minor, final @Nonnegative int micro) {
        this(major, minor, micro, null);
    }

    /**
     * Create a new version with 3 integer values and a qualifier.
     *
     * @param major the major version.
     * @param minor the minor version.
     * @param micro the micro version.
     * @param qualifier the version qualifier. May be <code>null</code>.
     * @throws IllegalArgumentException if any of the numeric parameters is &lt; 0.
     */
    public Version(final @Nonnegative int major, final @Nonnegative int minor, final @Nonnegative int micro,
            final @Nullable String qualifier) {
        if (major < 0) {
            throw new IllegalArgumentException("Major must be >= 0 but is " + major);
        }
        if (minor < 0) {
            throw new IllegalArgumentException("Minor must be >= 0 but is " + minor);
        }
        if (micro < 0) {
            throw new IllegalArgumentException("Micro must be >= 0 but is " + micro);
        }
        this.major = major;
        this.minor = minor;
        this.micro = micro;
        this.qualifier = StringHelper.isEmpty(qualifier) ? null : qualifier;
    }

    /**
     * @return the major version number. Always &ge; 0.
     */
    public final @Nonnegative int getMajor() {
        return this.major;
    }

    /**
     * @return the minor version number. Always &ge; 0.
     */
    public final @Nonnegative int getMinor() {
        return this.minor;
    }

    /**
     * @return the micro version number. Always &ge; 0.
     */
    public final @Nonnegative int getMicro() {
        return this.micro;
    }

    /**
     * @return the version qualifier String. May be <code>null</code>.
     */
    public final @Nullable String getQualifier() {
        return this.qualifier;
    }

    /**
     * @return <code>true</code> if a qualifier is present, <code>false</code> otherwise.
     */
    public final boolean hasQualifier() {
        return StringHelper.isNotEmpty(this.qualifier);
    }

    /**
     * Compares two Version objects.
     *
     * @param rhs the version to compare to. May not be <code>null</code>.
     * @return &lt; 0 if this is less than rhs; &gt; 0 if this is greater than rhs, and 0 if they are equal.
     */
    public int compareTo(@NonNull final Version rhs) {
        // compare major version
        int ret = this.major - rhs.major;
        if (ret == 0) {
            // compare minor version
            ret = this.minor - rhs.minor;
            if (ret == 0) {
                // compare micro version
                ret = this.micro - rhs.micro;
                if (ret == 0) {
                    // check qualifier
                    if (this.qualifier != null) {
                        if (rhs.qualifier != null) {
                            // convert to -1/0/+1
                            ret = Integer.signum(this.qualifier.compareTo(rhs.qualifier));
                        } else {
                            ret = +1;
                        }
                    } else if (rhs.qualifier != null) {
                        // only this qualifier == null
                        ret = -1;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Get the String representation of the version number using the default setting for printing zero elements.
     *
     * @return never <code>null</code>.
     */
    public @NonNull String getAsString() {
        return getAsString(DEFAULT_PRINT_ZERO_ELEMENTS);
    }

    /**
     * Get the String representation of the version number.
     *
     * @param printZeroElements if <code>true</code> then trailing zeroes are printed, otherwise they are not.
     * @return never <code>null</code>.
     */
    public @NonNull String getAsString(final boolean printZeroElements) {
        return getAsString(printZeroElements, false);
    }

    /**
     * Get the String representation of the version number.
     *
     * @param printZeroElements if <code>true</code> then trailing zeroes are printed, otherwise they are not.
     * @param printAtLeastMajorAndMinor <code>true</code> if the major and the minor part should always be printed,
     *            independent of their value.
     * @return never <code>null</code>.
     */
    public @NonNull String getAsString(final boolean printZeroElements, final boolean printAtLeastMajorAndMinor) {
        // Build from back to front
        final StringBuilder sb = new StringBuilder(this.qualifier != null ? this.qualifier : "");
        if (this.micro > 0 || sb.length() > 0 || printZeroElements) {
            // Micro version
            if (sb.length() > 0) {
                sb.insert(0, NUMERIC_PART_SEPARATOR);
            }
            sb.insert(0, this.micro);
        }
        if (printAtLeastMajorAndMinor || this.minor > 0 || sb.length() > 0 || printZeroElements) {
            // Minor version
            if (sb.length() > 0) {
                sb.insert(0, NUMERIC_PART_SEPARATOR);
            }
            sb.insert(0, this.minor);
        }
        if (printAtLeastMajorAndMinor || this.major > 0 || sb.length() > 0 || printZeroElements) {
            // Major version
            if (sb.length() > 0) {
                sb.insert(0, NUMERIC_PART_SEPARATOR);
            }
            sb.insert(0, this.major);
        }
        return sb.length() > 0 ? sb.toString() : DEFAULT_VERSION_STRING;
    }

    /**
     * Get the String representation of the version number using the strict layout
     * <code>major[.minor[.micro]][-qualifier]</code>. Trailing zero elements are omitted, but the major version is
     * always printed. The qualifier - if present - is always separated with a <code>-</code> character.
     * <p>
     * The result of this method can always be read back with {@link #parseStrictOrNull(String)}, contrary to the
     * combination of {@link #getAsString()} and {@link #parse(String)}.
     *
     * @return never <code>null</code> nor empty.
     * @see #parseStrictOrNull(String)
     */
    public @NonNull @Nonempty String getAsStringStrict() {
        final StringBuilder sb = new StringBuilder().append(this.major);
        if (this.minor > 0 || this.micro > 0) {
            sb.append(NUMERIC_PART_SEPARATOR).append(this.minor);
            if (this.micro > 0) {
                sb.append(NUMERIC_PART_SEPARATOR).append(this.micro);
            }
        }
        if (StringHelper.isNotEmpty(this.qualifier)) {
            sb.append(STRICT_QUALIFIER_SEPARATOR).append(this.qualifier);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final Version rhs = (Version) o;
        return this.major == rhs.major && this.minor == rhs.minor && this.micro == rhs.micro
                && Objects.equals(this.qualifier, rhs.qualifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.major), Integer.valueOf(this.minor), Integer.valueOf(this.micro), this.qualifier);
    }

    @Override
    public String toString() {
        return "Version[major=" + this.major + "; minor=" + this.minor + "; micro=" + this.micro
                + (this.qualifier != null ? "; qualifier=" + this.qualifier : "") + "]";
    }

    private static @NonNull String[] extSplit(@NonNull final String s) {
        final String[] dotParts = StringHelper.getExplodedArray(NUMERIC_PART_SEPARATOR, s, 2);
        if (dotParts.length == 2) {
            // Dots always take precedence
            return dotParts;
        }

        if (StringHelper.isInt(dotParts[0])) {
            // If it is numeric, use the dot parts anyway (e.g. for "5" or "-1")
            return dotParts;
        }

        final String[] dashParts = StringHelper.getExplodedArray(STRICT_QUALIFIER_SEPARATOR, s, 2);
        if (dashParts.length == 1) {
            // Neither dot nor dash present
            return dotParts;
        }

        // More matches for dash split! (e.g. "0-RC1")
        return dashParts;
    }

    /**
     * Construct a version object from a String.<br>
     * EBNF:<br>
     * version ::= major( '.' minor ( '.' micro ( ( '.' | '-' ) qualifier )? )? )? <br>
     * major ::= number<br>
     * minor ::= number<br>
     * micro ::= number<br>
     * qualifier ::= .+
     *
     * @param versionString the version String to be interpreted as a version. May be <code>null</code>.
     * @return the parsed {@link Version} object. Never <code>null</code>.
     * @throws IllegalArgumentException if any of the parsed numeric parts is &lt; 0.
     */
    public static @NonNull Version parse(final @Nullable String versionString) {
        final String s = versionString == null ? "" : versionString.trim();
        if (s.isEmpty()) {
            return DEFAULT_VERSION;
        }

        // Complex parsing
        Integer minorObj = null;
        Integer microObj = null;
        String qualifier;
        boolean done = false;

        // Extract major version number
        String[] parts = extSplit(s);
        final Integer majorObj = StringHelper.parseIntObj(parts[0]);
        if (majorObj == null && StringHelper.isNotEmpty(parts[0])) {
            // Major version is not numeric, so everything is the qualifier
            qualifier = s;
            done = true;
        } else {
            qualifier = null;
        }

        String rest = !done && parts.length > 1 ? parts[1] : null;
        if (StringHelper.isNotEmpty(rest)) {
            // Parse minor version number part
            parts = extSplit(rest);
            minorObj = StringHelper.parseIntObj(parts[0]);
            if (minorObj == null && StringHelper.isNotEmpty(parts[0])) {
                // Minor version is not numeric, so everything is the qualifier
                qualifier = rest;
                done = true;
            }

            rest = !done && parts.length > 1 ? parts[1] : null;
            if (StringHelper.isNotEmpty(rest)) {
                // Parse micro version number part
                parts = extSplit(rest);
                microObj = StringHelper.parseIntObj(parts[0]);
                if (microObj == null && StringHelper.isNotEmpty(parts[0])) {
                    // Micro version is not numeric, so everything is the qualifier
                    qualifier = rest;
                    done = true;
                }

                if (!done && parts.length > 1) {
                    // Some qualifier left!
                    qualifier = parts[1];
                }
            }
        }

        final int major = majorObj == null ? 0 : majorObj.intValue();
        final int minor = minorObj == null ? 0 : minorObj.intValue();
        final int micro = microObj == null ? 0 : microObj.intValue();
        return new Version(major, minor, micro, StringHelper.isEmpty(qualifier) ? null : qualifier);
    }

    /**
     * Check if the provided String is a numeric version part, meaning it consists of digits only and has no superfluous
     * leading zeroes. The latter is required so that the parsing is the exact inverse of {@link #getAsStringStrict()}.
     *
     * @param s the String to check. May be <code>null</code>.
     * @return <code>true</code> if it is a valid numeric version part.
     */
    private static boolean isStrictNumericPart(final @Nullable String s) {
        final int len = StringHelper.getLength(s);
        if (len == 0) {
            return false;
        }

        // No leading zero, except for "0" itself
        if (len > 1 && s.charAt(0) == '0') {
            return false;
        }

        for (int i = 0; i < len; ++i) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Construct a version object from a String using a strict layout. This is the exact inverse of
     * {@link #getAsStringStrict()}.<br>
     * EBNF:<br>
     * version ::= major ( '.' minor ( '.' micro )? )? ( '-' qualifier )? <br>
     * major ::= number<br>
     * minor ::= number<br>
     * micro ::= number<br>
     * qualifier ::= .+
     * <p>
     * Contrary to {@link #parse(String)} the qualifier is always introduced by the first <code>-</code> character. That
     * way a purely numeric qualifier is retained as such, whereas {@link #parse(String)} takes it as the micro version
     * number instead - e.g. <code>1.4-03</code> is parsed to <code>1.4.0-03</code> by this method but to
     * <code>1.4.3</code> by {@link #parse(String)}.
     * <p>
     * Additionally this method is strict about its input and returns <code>null</code> instead of silently falling back
     * to a default value. Numeric version parts must not have superfluous leading zeroes, so that <code>1.04</code> is
     * rejected instead of being read as <code>1.4</code>. Trailing zero elements are accepted though, so
     * <code>1</code>, <code>1.0</code> and <code>1.0.0</code> all lead to the same version.
     *
     * @param versionString the version String to be interpreted as a version. May be <code>null</code>.
     * @return <code>null</code> if the provided String does not match the layout above.
     * @see #getAsStringStrict()
     */
    public static @Nullable Version parseStrictOrNull(final @Nullable String versionString) {
        if (versionString == null) {
            return null;
        }

        final String s = versionString.trim();
        if (s.isEmpty()) {
            return null;
        }

        // Split off the qualifier at the first separator - the qualifier itself may contain further separators
        final String numbers;
        final String qualifier;
        final int sepIdx = s.indexOf(STRICT_QUALIFIER_SEPARATOR);
        if (sepIdx < 0) {
            numbers = s;
            qualifier = null;
        } else {
            numbers = s.substring(0, sepIdx);
            qualifier = s.substring(sepIdx + 1);
            // Neither "1.2-" nor "-bla" are valid
            if (numbers.isEmpty() || qualifier.isEmpty()) {
                return null;
            }
        }

        final String[] parts = StringHelper.getExplodedArray(NUMERIC_PART_SEPARATOR, numbers);
        if (parts.length == 0 || parts.length > 3) {
            return null;
        }

        for (final String part : parts) {
            if (!isStrictNumericPart(part)) {
                return null;
            }
        }

        // Returns null on overflow, so that no negative number can arise
        final Integer major = StringHelper.parseIntObj(parts[0]);
        final Integer minor = parts.length > 1 ? StringHelper.parseIntObj(parts[1]) : Integer.valueOf(0);
        final Integer micro = parts.length > 2 ? StringHelper.parseIntObj(parts[2]) : Integer.valueOf(0);
        if (major == null || minor == null || micro == null) {
            return null;
        }

        // Trailing zero elements are accepted but not canonical, so "1", "1.0" and "1.0.0" all lead to the same Version
        return new Version(major.intValue(), minor.intValue(), micro.intValue(), qualifier);
    }
}
