package org.kosit.xvrl.api;

import java.util.List;
import java.util.stream.Collectors;

import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.xvrl.model.XvrlDetectionType;
import org.kosit.xvrl.model.XvrlLocationType;
import org.kosit.xvrl.model.XvrlMessageType;

public interface BaseDetection {

    List<XvrlMessageType> getMessages();

    List<XvrlLocationType> getLocations();

    XvrlDetectionType.Severity getSeverity();

    void setSeverity(XvrlDetectionType.Severity value);

    @ReturnsImmutableObject
    default List<String> getAllMessages() {
        return getMessages().stream().flatMap(message -> message.getMessageStrings().stream()).toList();
    }

    default String getErrorMessage() {
        if (getMessages().isEmpty()) {
            return null;
        }
        return getMessages().get(0).getContent().stream().map(Object::toString).collect(Collectors.joining());
    }

    default XvrlLocationType getErrorLocation() {
        if (getLocations().isEmpty()) {
            return null;
        }
        return getLocations().get(0);
    }

    default boolean hasErrors() {
        return getSeverity() == XvrlDetectionType.Severity.ERROR || getSeverity() == XvrlDetectionType.Severity.FATAL_ERROR;
    }
}
