package org.kosit.xvrl.api;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.xvrl.model.XvrlSeverityType;

/**
 * XVRL utility methods
 * 
 * @author Philip Helger
 *
 */
public final class XvrlHelper {

    public static @NonNull XvrlSeverityType translate(final @Nullable CTStandardSeverity severity) {
        if (severity == null)
            return XvrlSeverityType.UNSPECIFIED;

        return switch (severity) {
            case NONE -> XvrlSeverityType.UNSPECIFIED;
            case WARNING -> XvrlSeverityType.WARNING;
            case ERROR -> XvrlSeverityType.ERROR;
        };
    }

    private XvrlHelper() {
    }
}
