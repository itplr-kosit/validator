package org.kosit.xvrl.api;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.error.SimpleError;
import org.kosit.xvrl.model.XvrlLocation;
import org.kosit.xvrl.model.XvrlMessage;
import org.kosit.xvrl.model.XvrlSchema;
import org.kosit.xvrl.model.XvrlSeverity;

/**
 * XVRL utility methods
 * 
 * @author Philip Helger
 */
public final class XvrlHelper {

    public static @NonNull XvrlSeverity translate(final @Nullable CTStandardSeverity severity) {
        if (severity == null)
            return XvrlSeverity.UNSPECIFIED;

        return switch (severity) {
            case NONE -> XvrlSeverity.UNSPECIFIED;
            case WARNING -> XvrlSeverity.WARNING;
            case ERROR -> XvrlSeverity.ERROR;
        };
    }

    public static XvrlSchema createSchema(final @Nullable String href, final @Nullable String schemaTypeNs) {
        return XvrlSchema.builder().href(href).schemaTypeNs(schemaTypeNs).build();
    }

    public static XvrlLocation createLocation(final SimpleError error) {
        return XvrlLocation.builder().line(error.getLineNumberObj()).column(error.getColumnNumberObj()).build();
    }

    public static XvrlMessage createMessage(final @Nullable String message) {
        return XvrlMessage.builder(message).build();
    }

    private XvrlHelper() {
    }
}
