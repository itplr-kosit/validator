package org.kosit.base.coord.version;

import java.util.Objects;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.kosit.base.ObjectHelper;
import org.kosit.base.string.StringHelper;
import org.kosit.base.version.Version;

/**
 * Default implementation of {@link IDVRPseudoVersion}.
 *
 * @author Philip Helger
 */
public class DVRPseudoVersion implements IDVRPseudoVersion {

    private final String id;

    private final IDVRPseudoVersionComparable comparable;

    public DVRPseudoVersion(@NonNull @Nonempty final String id, @NonNull final IDVRPseudoVersionComparable comparable) {
        if (StringHelper.isEmpty(id)) {
            throw new IllegalArgumentException("ID must not be empty");
        }
        this.id = id;
        this.comparable = ObjectHelper.requireNonNull(comparable, "Comparable");
    }

    public final @NonNull @Nonempty String getID() {
        return this.id;
    }

    /**
     * @return the comparable object provided in the constructor. Never <code>null</code>.
     */
    public final @NonNull IDVRPseudoVersionComparable getPseudoVersionComparable() {
        return this.comparable;
    }

    public int compareToPseudoVersion(@NonNull final IDVRPseudoVersion otherPseudoVersion) {
        ObjectHelper.requireNonNull(otherPseudoVersion, "OtherPseudoVersion");

        // The same pseudo version is always identical
        if (this.id.equals(otherPseudoVersion.getID())) {
            return 0;
        }

        // Pass to handler
        return this.comparable.compareToPseudoVersion(otherPseudoVersion);
    }

    public int compareToVersion(@NonNull final Version staticVersion) {
        ObjectHelper.requireNonNull(staticVersion, "StaticVersion");

        // Pass to handler
        return this.comparable.compareToVersion(staticVersion);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        final DVRPseudoVersion rhs = (DVRPseudoVersion) o;
        return this.id.equals(rhs.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "DVRPseudoVersion[id=" + this.id + "; comparable=" + this.comparable + "]";
    }
}
