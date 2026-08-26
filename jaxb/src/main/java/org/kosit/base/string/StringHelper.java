package org.kosit.base.string;

import org.jspecify.annotations.Nullable;

public final class StringHelper {

    public static String normalizeBlankToNull(final @Nullable String s) {
        return s == null || s.isBlank() ? null : s;
    }

    public static long nvl(final @Nullable Long v) {
        return v == null ? 0L : v.longValue();
    }

    private StringHelper() {
    }

}
