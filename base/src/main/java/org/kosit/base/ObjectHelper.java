package org.kosit.base;

import java.util.Objects;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class ObjectHelper {

    public static <T> @NonNull T requireNonNull(final @Nullable T value, final String name) {
        return Objects.requireNonNull(value, name + " must not be null");
    }

    /**
     * Compare the two passed objects, handling <code>null</code> values correctly. A <code>null</code> value is always
     * smaller than a non-<code>null</code> value.
     *
     * @param <T> the type of the objects to compare. Both need to be of the same type.
     * @param left the first object to compare. May be <code>null</code>.
     * @param right the second object to compare. May be <code>null</code>.
     * @return 0 if both are equal or both are <code>null</code>, &lt; 0 or &gt; 0 otherwise.
     */
    public static <T extends Comparable<? super T>> int compare(final @Nullable T left, final @Nullable T right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return +1;
        }
        return left.compareTo(right);
    }

    @SuppressWarnings("unchecked")
    public static <SRCTYPE, DSTTYPE> DSTTYPE uncheckedCast(@Nullable final SRCTYPE aObject) {
        return (DSTTYPE) aObject;
    }

    private ObjectHelper() {
    }
}
