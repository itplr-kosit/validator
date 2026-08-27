package org.kosit.xvrl.api;

import java.util.List;
import java.util.stream.Collectors;

import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.xvrl.model.XvrlLocationType;
import org.kosit.xvrl.model.XvrlMessageType;
import org.kosit.xvrl.model.XvrlSeverityType;

public interface BaseDetection {

    List<XvrlMessageType> getMessages();

    List<XvrlLocationType> getLocations();

    XvrlSeverityType getSeverity();

    void setSeverity(XvrlSeverityType value);

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
        return getSeverity() == XvrlSeverityType.ERROR || getSeverity() == XvrlSeverityType.FATAL_ERROR;
    }
}
