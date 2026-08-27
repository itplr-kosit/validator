package org.kosit.xvrl.api;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.error.SimpleError;
import org.kosit.xvrl.model.XvrlLocationType;
import org.kosit.xvrl.model.XvrlMessageType;
import org.kosit.xvrl.model.XvrlSchemaType;
import org.kosit.xvrl.model.XvrlSeverityType;

/**
 * XVRL utility methods
 * 
 * @author Philip Helger
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

    public static XvrlSchemaType createSchema(final String href, final String schemaTypeNs) {
        final XvrlSchemaType schema = new XvrlSchemaType();
        schema.setHref(href);
        schema.setSchematypens(schemaTypeNs);
        return schema;
    }

    public static XvrlLocationType createLocation(final SimpleError error) {
        final XvrlLocationType location = new XvrlLocationType();
        location.setLine(error.getLineNumberObj());
        location.setColumn(error.getColumnNumberObj());
        location.setXpath(null);
        return location;
    }

    public static XvrlMessageType createMessage(final String message) {
        final XvrlMessageType messageObject = new XvrlMessageType();
        messageObject.getContent().add(message);
        return messageObject;
    }

    private XvrlHelper() {
    }
}
