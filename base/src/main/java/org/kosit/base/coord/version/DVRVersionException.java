package org.kosit.base.coord.version;

import org.jspecify.annotations.NonNull;
import org.kosit.base.coord.DVRException;

/**
 * A specific exception for DVR version handling.
 *
 * @author Philip Helger
 */
public class DVRVersionException extends DVRException {

    public DVRVersionException(@NonNull final String msg) {
        super(msg);
    }
}
