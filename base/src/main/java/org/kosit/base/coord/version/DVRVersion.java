package org.kosit.base.coord.version;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.coord.DVRValidityHelper;
import org.kosit.base.string.StringHelper;
import org.kosit.base.version.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains the version of a DVR Coordinate. This can either be a static version or a pseudo version. This
 * version type has a specific kind of ordering, so that versions using a well known pre-release classifier are ordered
 * BEFORE the respective release versions. Example order:
 * <ol>
 * <li>1.0</li>
 * <li>1.1-SNAPSHOT</li>
 * <li>1.1</li>
 * <li>1.2</li>
 * <li>1.3-SNAPSHOT</li>
 * <li>1.3-alpha1</li>
 * <li>1.3-beta</li>
 * <li>1.3-milestone2</li>
 * <li>1.3-rc9</li>
 * <li>1.3-rc10</li>
 * <li>1.3</li>
 * <li>1.3-01</li>
 * </ol>
 * The complete ordering of the version classifiers of one and the same numeric version is:
 * <code>SNAPSHOT &lt; alpha &lt; beta &lt; milestone &lt; rc &lt; release (no classifier) &lt; any other
 * classifier</code>. The well known pre-release classifiers are matched case insensitively - see
 * {@link EDVRPreReleaseQualifier} for the details. Every other classifier keeps being compared as a String, and is
 * ordered after the release version.
 *
 * @author Philip Helger
 * @see EDVRPreReleaseQualifier
 */
public final class DVRVersion implements Comparable<DVRVersion> {

    /** Specific qualifier for "SNAPSHOT" versions */
    public static final String QUALIFIER_SNAPSHOT = "SNAPSHOT";

    /** Separator between major and minor and between minor and micro version */
    public static final char NUMERIC_VERSION_PART_SEPARATOR = '.';

    /** Separator between the classifier and the rest (if available) */
    public static final char DEFAULT_CLASSIFIER_SEPARATOR = '-';

    /**
     * Sort rank of a version without any qualifier - the final release. It is higher than the rank of all pre-release
     * qualifiers and lower than the rank of all unknown qualifiers.
     */
    private static final int RANK_RELEASE = EDVRPreReleaseQualifier.MAX_RANK + 1;

    /** Sort rank of a version with a qualifier that is not a well known pre-release qualifier */
    private static final int RANK_OTHER = RANK_RELEASE + 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(DVRVersion.class);

    private final @Nullable Version staticVersion;

    private final @Nullable IDVRPseudoVersion pseudoVersion;

    /**
     * Constructor - only invoked by the static factory methods below.
     *
     * @param staticVersion the static version. May be <code>null</code>.
     * @param pseudoVersion the pseudo version. May be <code>null</code>.
     */
    private DVRVersion(final @Nullable Version staticVersion, final @Nullable IDVRPseudoVersion pseudoVersion) {
        if (staticVersion == null && pseudoVersion == null) {
            throw new IllegalArgumentException("Either Static Version or Pseudo Version must be provided");
        }
        if (staticVersion != null && pseudoVersion != null) {
            throw new IllegalArgumentException("Only one of Static Version or Pseudo Version must be provided");
        }
        this.staticVersion = staticVersion;
        this.pseudoVersion = pseudoVersion;
    }

    /**
     * @return <code>true</code> if it is a static version, <code>false</code> if it is a pseudo version.
     * @see #isPseudoVersion()
     * @see #getStaticVersion()
     */
    public boolean isStaticVersion() {
        return this.staticVersion != null;
    }

    /**
     * @param qualifier the qualifier to check. May be <code>null</code>.
     * @return <code>true</code> if the qualifier is "SNAPSHOT". The check is case insensitive.
     */
    public static boolean isStaticSnapshotVersion(final @Nullable String qualifier) {
        return EDVRPreReleaseQualifier.getFromQualifierOrNull(qualifier) == EDVRPreReleaseQualifier.SNAPSHOT;
    }

    /**
     * @param ver the version to check. May be <code>null</code>.
     * @return <code>true</code> if the passed version has the qualifier "SNAPSHOT".
     */
    public static boolean isStaticSnapshotVersion(final @Nullable Version ver) {
        return ver != null && isStaticSnapshotVersion(ver.getQualifier());
    }

    /**
     * @return <code>true</code> if this is a static version, and if the qualifier is "SNAPSHOT".
     * @see #isStaticVersion()
     */
    public boolean isStaticSnapshotVersion() {
        return isStaticSnapshotVersion(this.staticVersion);
    }

    /**
     * @return the static version of this DVR version. Guaranteed to be non-<code>null</code> if
     *         {@link #isStaticVersion()} returns <code>true</code>.
     * @see #isStaticVersion()
     */
    public @Nullable Version getStaticVersion() {
        return this.staticVersion;
    }

    /**
     * @return <code>true</code> if it is a pseudo version, <code>false</code> if it is a static version.
     * @see #isStaticVersion()
     * @see #getPseudoVersion()
     */
    public boolean isPseudoVersion() {
        return this.pseudoVersion != null;
    }

    /**
     * @return the pseudo version of this DVR version. Guaranteed to be non-<code>null</code> if
     *         {@link #isPseudoVersion()} returns <code>true</code>.
     * @see #isPseudoVersion()
     */
    public @Nullable IDVRPseudoVersion getPseudoVersion() {
        return this.pseudoVersion;
    }

    private static @NonNull String getAsString(@NonNull final Version version, final char classifierSep, final boolean enforceAllNumbers,
            final boolean enforceAtLeastMinor) {
        // Different implementation than Version.getAsString (...)
        String ret = "";
        char sep = classifierSep;
        boolean must = enforceAllNumbers;

        // Start from the back: classifier
        if (version.hasQualifier()) {
            ret = version.getQualifier();
        }

        // Add micro version
        if (version.getMicro() > 0 || must) {
            if (!ret.isEmpty()) {
                ret = sep + ret;
            }
            ret = version.getMicro() + ret;
            // Change separator to number version separator
            sep = NUMERIC_VERSION_PART_SEPARATOR;
            must = true;
        }

        if (enforceAtLeastMinor) {
            must = true;
        }

        // Add minor version
        if (version.getMinor() > 0 || must) {
            if (!ret.isEmpty()) {
                ret = sep + ret;
            }
            ret = version.getMinor() + ret;
            // Change separator to number version separator
            sep = NUMERIC_VERSION_PART_SEPARATOR;
            must = true;
        }

        // Add major version
        // Avoid empty string
        if (version.getMajor() > 0 || must || ret.isEmpty()) {
            if (!ret.isEmpty()) {
                ret = sep + ret;
            }
            ret = version.getMajor() + ret;
        }

        return ret;
    }

    public static @NonNull String getAsString(@NonNull final Version version) {
        return getAsString(version, DEFAULT_CLASSIFIER_SEPARATOR, false, false);
    }

    public static @NonNull @Nonempty String getAsString(@NonNull final IDVRPseudoVersion pseudoVersion) {
        return pseudoVersion.getID();
    }

    /**
     * @return the unified String representation of the version. Never <code>null</code>.
     */
    public @NonNull String getAsString() {
        if (this.staticVersion != null) {
            return getAsString(this.staticVersion);
        }

        return getAsString(this.pseudoVersion);
    }

    private static int getQualifierRank(final @Nullable EDVRPreReleaseQualifier preRelease, final @Nullable String qualifier) {
        if (preRelease != null) {
            return preRelease.getRank();
        }
        return StringHelper.isEmpty(qualifier) ? RANK_RELEASE : RANK_OTHER;
    }

    /**
     * Compare the qualifiers of two versions that have the same major, minor and micro version.
     *
     * @param lhs the left hand side qualifier. May be <code>null</code>.
     * @param rhs the right hand side qualifier. May be <code>null</code>.
     * @return &lt; 0, 0 or &gt; 0
     */
    private static int compareQualifier(final @Nullable String lhs, final @Nullable String rhs) {
        final EDVRPreReleaseQualifier eLhs = EDVRPreReleaseQualifier.getFromQualifierOrNull(lhs);
        final EDVRPreReleaseQualifier eRhs = EDVRPreReleaseQualifier.getFromQualifierOrNull(rhs);

        // First the rank decides: SNAPSHOT < alpha < beta < milestone < rc < release < anything else
        int ret = Integer.compare(getQualifierRank(eLhs, lhs), getQualifierRank(eRhs, rhs));
        if (ret != 0) {
            return ret;
        }

        // Same rank - if both use the same pre-release qualifier, the trailing number decides. It must be compared
        // numerically, otherwise "rc9" would be sorted after "rc10"
        if (eLhs != null) {
            ret = Integer.compare(eLhs.getNumber(lhs), eRhs.getNumber(rhs));
            if (ret != 0) {
                return ret;
            }
        }

        // Both versions have no qualifier at all
        if (StringHelper.isEmpty(lhs) && StringHelper.isEmpty(rhs)) {
            return 0;
        }

        // Fallback to the String comparison, so that two different qualifiers never compare as equal - as in "RC1" vs.
        // "rc1" or "rc1" vs. "rc01". Returning 0 for them would make them indistinguishable in a sorted set or map.
        return ObjectHelper.compare(lhs, rhs);
    }

    private static int compareSemantically(@NonNull final Version lhs, @NonNull final Version rhs) {
        // The numeric version parts always win over the qualifier
        int ret = Integer.compare(lhs.getMajor(), rhs.getMajor());
        if (ret == 0) {
            ret = Integer.compare(lhs.getMinor(), rhs.getMinor());
            if (ret == 0) {
                ret = Integer.compare(lhs.getMicro(), rhs.getMicro());
                if (ret == 0) {
                    // Same numeric version - the qualifier decides
                    ret = compareQualifier(lhs.getQualifier(), rhs.getQualifier());
                }
            }
        }
        return ret;
    }

    private static @NonNull Version getWithoutQualifier(@NonNull final Version src) {
        return new Version(src.getMajor(), src.getMinor(), src.getMicro(), null);
    }

    /**
     * The static version comparison as it was in ph-diver up to v4.2.1: "SNAPSHOT" is the only pre-release qualifier,
     * it is matched case sensitively, and every other qualifier - including <code>rc</code>, <code>alpha</code>,
     * <code>beta</code> and <code>milestone</code> - is compared as a String and is ordered AFTER the respective
     * release version.
     *
     * @param lhs the left hand side version. May not be <code>null</code>.
     * @param rhs the right hand side version. May not be <code>null</code>.
     * @return &lt; 0, 0 or &gt; 0
     */
    private static int compareSemanticallyClassic(@NonNull final Version lhs, @NonNull final Version rhs) {
        // Deliberately using the case sensitive check here, and not isStaticSnapshotVersion, which is case insensitive
        if (QUALIFIER_SNAPSHOT.equals(lhs.getQualifier())) {
            if (QUALIFIER_SNAPSHOT.equals(rhs.getQualifier())) {
                // Lhs & Rhs are Snapshots
                return lhs.compareTo(rhs);
            }
            // Lhs is Snapshot
            final Version lhsClean = getWithoutQualifier(lhs);
            final int cmp = lhsClean.compareTo(rhs);
            if (cmp == 0) {
                // Snapshot comes before release
                return -1;
            }
            return cmp;
        }

        if (QUALIFIER_SNAPSHOT.equals(rhs.getQualifier())) {
            // Rhs is Snapshot
            final Version rhsClean = getWithoutQualifier(rhs);
            final int cmp = lhs.compareTo(rhsClean);
            if (cmp == 0) {
                // Snapshot comes before release
                return +1;
            }
            return cmp;
        }

        // No snapshot version contained
        return lhs.compareTo(rhs);
    }

    /**
     * Compare a static version with a pseudo version.
     *
     * @param staticVersion the static version. May not be <code>null</code>.
     * @param pseudoVersion the pseudo version. May not be <code>null</code>.
     * @return -1, 0 or +1
     */
    private static int compareWithPseudoVersion(@NonNull final Version staticVersion, @NonNull final IDVRPseudoVersion pseudoVersion) {
        // Change sign, due to calling order
        return -pseudoVersion.compareToVersion(staticVersion);
    }

    /**
     * The shared comparison logic. Only the comparison of two static versions differs between the current and the
     * classic ordering - the handling of pseudo versions is identical.
     *
     * @param rhs the version to compare to. May not be <code>null</code>.
     * @param staticVersionComparator the comparison to be used if both sides are static versions. May not be
     *            <code>null</code>.
     * @return &lt; 0, 0 or &gt; 0
     */
    private int compareTo(@NonNull final DVRVersion rhs, @NonNull final ToIntBiFunction<Version, Version> staticVersionComparator) {
        if (isStaticVersion()) {
            if (rhs.isStaticVersion()) {
                return staticVersionComparator.applyAsInt(this.staticVersion, rhs.staticVersion);
            }
            return compareWithPseudoVersion(this.staticVersion, rhs.pseudoVersion);
        }

        // this is a pseudo version
        if (rhs.isStaticVersion()) {
            // Invert result
            return -compareWithPseudoVersion(rhs.staticVersion, this.pseudoVersion);
        }

        // Both are pseudo versions
        return this.pseudoVersion.compareToPseudoVersion(rhs.pseudoVersion);
    }

    /**
     * Compare this version to the provided one, using the ordering as it was in ph-diver up to v4.2.1: "SNAPSHOT" is
     * the only known pre-release qualifier, it is matched case sensitively, and every other qualifier - including
     * <code>rc</code>, <code>alpha</code>, <code>beta</code> and <code>milestone</code> - is compared as a String and
     * is ordered AFTER the respective release version. So <code>1.0.0</code> is considered older than
     * <code>1.0.0-rc1</code>.
     * <p>
     * This method is provided for backwards compatibility only. Use {@link #compareTo(DVRVersion)} unless the old
     * ordering must be preserved.
     *
     * @param rhs the version to compare to. May not be <code>null</code>.
     * @return &lt; 0, 0 or &gt; 0
     * @see #compareTo(DVRVersion)
     */
    public int compareToClassic(@NonNull final DVRVersion rhs) {
        ObjectHelper.requireNonNull(rhs, "Rhs");
        return compareTo(rhs, DVRVersion::compareSemanticallyClassic);
    }

    public int compareTo(@NonNull final DVRVersion rhs) {
        ObjectHelper.requireNonNull(rhs, "Rhs");
        return compareTo(rhs, DVRVersion::compareSemantically);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final DVRVersion rhs = (DVRVersion) o;
        return Objects.equals(this.staticVersion, rhs.staticVersion) && Objects.equals(this.pseudoVersion, rhs.pseudoVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.staticVersion, this.pseudoVersion);
    }

    @Override
    public String toString() {
        return "DVRVersion[" + (this.staticVersion != null ? "staticVersion=" + this.staticVersion : "pseudoVersion=" + this.pseudoVersion)
                + "]";
    }

    public static @NonNull DVRVersion of(@NonNull final Version version) {
        ObjectHelper.requireNonNull(version, "Version");
        return new DVRVersion(version, null);
    }

    public static @NonNull DVRVersion of(@NonNull final IDVRPseudoVersion pseudoVersion) {
        ObjectHelper.requireNonNull(pseudoVersion, "PseudoVersion");
        return new DVRVersion(null, pseudoVersion);
    }

    /**
     * @return a new {@link DVRVersion} using the pseudo version "latest".
     */
    public static @NonNull DVRVersion latest() {
        return of(DVRPseudoVersionRegistry.LATEST);
    }

    /**
     * @return a new {@link DVRVersion} using the pseudo version "latest-release".
     */
    public static @NonNull DVRVersion latestRelease() {
        return of(DVRPseudoVersionRegistry.LATEST_RELEASE);
    }

    /**
     * Parse the provided version to a static {@link Version} object.
     *
     * @param version the version to parse. May be <code>null</code>.
     * @return <code>null</code> if the provided version is not a valid static version.
     * @see #isValidStaticVersion(String)
     */
    private static @Nullable Version parseStaticVersionOrNull(final @Nullable String version) {
        // Must not be empty
        if (StringHelper.isEmpty(version)) {
            return null;
        }

        // Must follow the DVR Coordinate constraints
        if (!DVRValidityHelper.isValidCoordinateVersion(version)) {
            return null;
        }

        // Parse to Version object
        final Version parsedVersion = Version.parse(version);
        if (parsedVersion == null) {
            return null;
        }

        // Check if the parsing result equals the original in a way
        // This section clearly would win the price for ugly coding - but the positive effect on consistency is even
        // more valuable :)
        final Set<String> possibleVersions = new LinkedHashSet<>();
        // Check different separators
        for (final char classifierSep : "-.".toCharArray()) {
            for (final boolean enforceAllNumbers : new boolean[] { true, false }) {
                for (final boolean enforceMinor : new boolean[] { true, false }) {
                    final String text = getAsString(parsedVersion, classifierSep, enforceAllNumbers, enforceMinor);
                    if (version.equals(text)) {
                        // We found a match
                        return parsedVersion;
                    }
                    possibleVersions.add(text);
                }
            }
        }

        // Fallback to the strict layout "major[.minor[.micro]][-classifier]".
        // Version.parse takes a purely numeric version classifier as the micro version number instead, so e.g.
        // "1.4-03" is not covered by the check above, even though it is the string representation of "1.4.0-03".
        // This is deliberately only a fallback, because for a version like "1.4-1.2.3" both interpretations are
        // possible, and the one established above must win to stay backwards compatible.
        final Version strictVersion = Version.parseStrictOrNull(version);
        if (strictVersion != null) {
            return strictVersion;
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'" + version + "' is none of "
                    + possibleVersions.stream().map(x -> "'" + x + "'").reduce((a, b) -> a + " or " + b).orElse(""));
        }

        // Nope, invalid version
        return null;
    }

    /**
     * Checks if the provided version is a valid static version.
     * <ul>
     * <li>1.0.0</li>
     * <li>1.0</li>
     * <li>1</li>
     * <li>1.0.0-SNAPSHOT</li>
     * <li>1.0-SNAPSHOT</li>
     * <li>1-SNAPSHOT</li>
     * <li>1.0.0.SNAPSHOT</li>
     * <li>1.0.SNAPSHOT</li>
     * <li>1.SNAPSHOT</li>
     * <li>1.4.0-03</li>
     * <li>1.4-03</li>
     * </ul>
     *
     * @param version the version to check. May be <code>null</code>.
     * @return <code>true</code> if the version is a valid static version, <code>false</code> if not.
     */
    public static boolean isValidStaticVersion(final @Nullable String version) {
        return parseStaticVersionOrNull(version) != null;
    }

    public static @NonNull DVRVersion parseOrThrow(final @Nullable String version) throws DVRVersionException {
        if (StringHelper.isEmpty(version)) {
            throw new DVRVersionException("DVR Version string must not be empty");
        }

        // Check pseudo version first
        final IDVRPseudoVersion pseudoVersion = DVRPseudoVersionRegistry.getInstance().getFromIDOrNull(version);
        if (pseudoVersion != null) {
            return of(pseudoVersion);
        }

        final Version staticVersion = parseStaticVersionOrNull(version);
        if (staticVersion != null) {
            return of(staticVersion);
        }

        throw new DVRVersionException("Failed to parse '" + version + "' to a DVR Version");
    }

    public static @Nullable DVRVersion parseOrNull(final @Nullable String version) {
        try {
            return parseOrThrow(version);
        } catch (final DVRVersionException | RuntimeException ex) {
            LOGGER.warn(ex.getMessage());
            return null;
        }
    }

    /**
     * Create a {@link Predicate} that can be used to filter static DVR versions. The returned predicate may be used as
     * a filter when iterating over entries. This method is only meant to work with static versions and does not
     * consider pseudo versions.
     *
     * @param versionsToIgnore an optional set of specific versions to ignore. This may be handy to explicitly rule out
     *            illegal versions. May be <code>null</code> or empty to indicate that no version should be ignored.
     * @param includeSnapshots <code>true</code> if SNAPSHOT versions should be allowed by the resulting predicate.
     * @return never <code>null</code>.
     */
    public static @NonNull Predicate<DVRVersion> getStaticVersionAcceptor(final @Nullable Set<String> versionsToIgnore,
            final boolean includeSnapshots) {
        if (versionsToIgnore == null || versionsToIgnore.isEmpty()) {
            if (includeSnapshots) {
                // We take all
                return _ -> true;
            }

            // We take everything except static snapshot versions
            return x -> !x.isStaticSnapshotVersion();
        }

        // We have something to ignore
        if (includeSnapshots) {
            // We take all, except for the ignored versions
            return x -> !versionsToIgnore.contains(x.getAsString());
        }

        // We take all except static snapshot versions and except for the ignored versions
        return x -> !x.isStaticSnapshotVersion() && !versionsToIgnore.contains(x.getAsString());
    }
}
