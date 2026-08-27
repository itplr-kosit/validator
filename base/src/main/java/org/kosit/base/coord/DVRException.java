package org.kosit.base.coord;

import org.jspecify.annotations.NonNull;

/**
 * A base exception for DVR handling.
 *
 * @author Philip Helger
 */
public class DVRException extends Exception {

    public DVRException(@NonNull final String msg) {
        super(msg);
    }
}
