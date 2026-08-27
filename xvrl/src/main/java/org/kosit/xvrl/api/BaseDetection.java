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
        final var msgs = getMessages();
        return msgs.isEmpty() ? null : msgs.getFirst().getContent().stream().map(Object::toString).collect(Collectors.joining());
    }

    default XvrlLocationType getErrorLocation() {
        final var locs = getLocations();
        return locs.isEmpty() ? null : locs.getFirst();
    }

    default boolean hasErrors() {
        final var sev = getSeverity();
        return sev == XvrlSeverityType.ERROR || sev == XvrlSeverityType.FATAL_ERROR;
    }
}
