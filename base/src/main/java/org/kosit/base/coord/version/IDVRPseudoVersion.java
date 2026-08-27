package org.kosit.base.coord.version;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;

/**
 * Base read-only interface for a pseudo version.
 *
 * @author Philip Helger
 */
public interface IDVRPseudoVersion extends IDVRPseudoVersionComparable {

    /**
     * @return the unique ID of this pseudo version. Neither <code>null</code> nor empty.
     */
    @NonNull
    @Nonempty
    String getID();
}
