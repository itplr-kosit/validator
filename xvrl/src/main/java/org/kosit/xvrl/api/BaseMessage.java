package org.kosit.xvrl.api;

import java.util.List;
import java.util.stream.Collectors;

public interface BaseMessage {

    List<Object> getContent();

    default List<String> getMessageStrings() {
        return getContent().stream().map(Object::toString).collect(Collectors.toList());
    }
}
