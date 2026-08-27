package org.kosit.base.coord;

import org.jspecify.annotations.NonNull;

/**
 * A specific exception for DVR Coordinate handling.
 *
 * @author Philip Helger
 */
public class DVRCoordinateException extends DVRException {

    public DVRCoordinateException(@NonNull final String msg) {
        super(msg);
    }
}
