package org.kosit.base;

import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ObjectHelper {

    public static <T> @NonNull T requireNonNull(final @Nullable T value, final String name) {
        return Objects.requireNonNull(value, name + " must not be null");
    }

    private ObjectHelper() {
    }
}
