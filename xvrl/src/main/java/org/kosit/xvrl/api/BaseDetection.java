package org.kosit.xvrl.api;

import java.util.List;
import java.util.stream.Collectors;

import org.kosit.xvrl.model.Location;
import org.kosit.xvrl.model.XVRLDetection;
import org.kosit.xvrl.model.XVRLMessage;

public interface BaseDetection {

    List<XVRLMessage> getMessages();

    List<Location> getLocations();

    XVRLDetection.Severity getSeverity();

    void setSeverity(XVRLDetection.Severity value);

    default List<String> getAllMessages() {
        return getMessages().stream().flatMap(message -> message.getMessageStrings().stream()).toList();
    }

    default String getErrorMessage() {
        if (getMessages().isEmpty()) {
            return null;
        }
        return getMessages().get(0).getContent().stream().map(Object::toString).collect(Collectors.joining());
    }

    default Location getErrorLocation() {
        if (getLocations().isEmpty()) {
            return null;
        }
        return getLocations().get(0);
    }

    default boolean hasErrors() {
        return getSeverity() == XVRLDetection.Severity.ERROR || getSeverity() == XVRLDetection.Severity.FATAL_ERROR;
    }
}
