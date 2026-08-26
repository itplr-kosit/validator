package org.kosit.xvrl.api;

import java.util.List;
import java.util.stream.Collectors;

import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.xvrl.model.XVRLDetectionType;
import org.kosit.xvrl.model.XVRLLocationType;
import org.kosit.xvrl.model.XVRLMessageType;

public interface BaseDetection {

    List<XVRLMessageType> getMessages();

    List<XVRLLocationType> getLocations();

    XVRLDetectionType.Severity getSeverity();

    void setSeverity(XVRLDetectionType.Severity value);

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

    default XVRLLocationType getErrorLocation() {
        if (getLocations().isEmpty()) {
            return null;
        }
        return getLocations().get(0);
    }

    default boolean hasErrors() {
        return getSeverity() == XVRLDetectionType.Severity.ERROR || getSeverity() == XVRLDetectionType.Severity.FATAL_ERROR;
    }
}
