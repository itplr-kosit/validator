package org.kosit.xvrl.api;

import java.util.List;

public interface BaseMessage {

    List<Object> getContent();

    default List<String> getMessageStrings() {
        return getContent().stream().map(Object::toString).toList();
    }
}
