package org.kosit.base.coord.version;

import org.jspecify.annotations.NonNull;
import org.kosit.base.version.Version;

/**
 * Helper interface to ensure that versions and pseudo versions can be kept in strict order.
 *
 * @author Philip Helger
 */
public interface IDVRPseudoVersionComparable {

    /**
     * Compare this object to the provided pseudo version.
     *
     * @param otherPseudoVersion the pseudo version to compare to. Never <code>null</code>.
     * @return a value &lt; 0 if this is &lt; other version; value 0 if this = other version; value &gt; 0 if this is
     *         &gt; other version.
     */
    int compareToPseudoVersion(@NonNull IDVRPseudoVersion otherPseudoVersion);

    /**
     * Compare this object to the provided static version.
     *
     * @param otherStaticVersion the static version to compare to. Never <code>null</code>.
     * @return a value &lt; 0 if this is &lt; other version; value 0 if this = other version; value &gt; 0 if this is
     *         &gt; other version.
     */
    int compareToVersion(@NonNull Version otherStaticVersion);
}
