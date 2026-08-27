package org.kosit.base.coord.version;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

import org.conformatron.api.annotation.Nonnegative;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.version.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for all known {@link IDVRPseudoVersion} instances. This class is not thread safe.
 *
 * @author Philip Helger
 */
public class DVRPseudoVersionRegistry implements IDVRPseudoVersionRegistry {

    /**
     * Oldest indicates the very first (oldest) version.
     */
    public static final IDVRPseudoVersion OLDEST = new DVRPseudoVersion("oldest", new IDVRPseudoVersionComparable() {

        public int compareToPseudoVersion(@NonNull final IDVRPseudoVersion otherPseudoVersion) {
            // OLDEST is always smaller
            return -1;
        }

        public int compareToVersion(@NonNull final Version staticVersion) {
            // OLDEST is always smaller
            return -1;
        }

        @Override
        public String toString() {
            return "OLDEST";
        }
    });

    /**
     * Latest indicates the very latest version (including snapshot).
     */
    public static final IDVRPseudoVersion LATEST = new DVRPseudoVersion("latest", new IDVRPseudoVersionComparable() {

        public int compareToPseudoVersion(@NonNull final IDVRPseudoVersion otherPseudoVersion) {
            // LATEST is always greater
            return +1;
        }

        public int compareToVersion(@NonNull final Version staticVersion) {
            // LATEST is always greater
            return +1;
        }

        @Override
        public String toString() {
            return "LATEST";
        }
    });

    /**
     * Latest release indicates the very latest version (excluding snapshot).
     */
    public static final IDVRPseudoVersion LATEST_RELEASE;

    // Inside the static block for best formatting :)
    static {
        LATEST_RELEASE = new DVRPseudoVersion("latest-release", new IDVRPseudoVersionComparable() {

            public int compareToPseudoVersion(@NonNull final IDVRPseudoVersion otherPseudoVersion) {
                // We are before LATEST
                if (otherPseudoVersion.equals(LATEST)) {
                    return -1;
                }

                // LATEST_RELEASE is always greater than the rest
                return +1;
            }

            public int compareToVersion(@NonNull final Version staticVersion) {
                // LATEST_RELEASE is always greater
                return +1;
            }

            @Override
            public String toString() {
                return "LATEST_RELEASE";
            }
        });
    }

    private static final class SingletonHolder {

        static final DVRPseudoVersionRegistry INSTANCE = new DVRPseudoVersionRegistry();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DVRPseudoVersionRegistry.class);

    private final Map<String, IDVRPseudoVersion> map = new HashMap<>();

    private DVRPseudoVersionRegistry() {
        reinitialize(false);
    }

    public static @NonNull DVRPseudoVersionRegistry getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private void reinitialize(final boolean log) {
        if (log) {
            LOGGER.info("Reinitializing the DVRPseudoVersionRegistry");
        }

        // Remove existing
        this.map.clear();

        // Register all again
        for (final IDVRPseudoVersionRegistrarSPI spi : ServiceLoader.load(IDVRPseudoVersionRegistrarSPI.class)) {
            spi.registerPseudoVersions(this);
        }

        if (log) {
            LOGGER.info("Finished reinitializing the DVRPseudoVersionRegistry with " + this.map.size() + " entries");
        }
    }

    /**
     * Remove all existing registrations and re-run the SPI search.
     */
    public final void reinitialize() {
        reinitialize(true);
    }

    public boolean registerPseudoVersion(@NonNull final IDVRPseudoVersion pseudoVersion) {
        ObjectHelper.requireNonNull(pseudoVersion, "PseudoVersion");

        final String key = pseudoVersion.getID();
        if (this.map.containsKey(key)) {
            LOGGER.error("Another pseudo version with ID '" + key + "' is already registered");
            return false;
        }
        this.map.put(key, pseudoVersion);
        return true;
    }

    public @Nullable IDVRPseudoVersion getFromIDOrNull(final @Nullable String id) {
        return this.map.get(id);
    }

    @Nonnegative
    final int size() {
        return this.map.size();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final DVRPseudoVersionRegistry rhs = (DVRPseudoVersionRegistry) o;
        return this.map.equals(rhs.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.map);
    }

    @Override
    public String toString() {
        return "DVRPseudoVersionRegistry[map=" + this.map + "]";
    }
}
