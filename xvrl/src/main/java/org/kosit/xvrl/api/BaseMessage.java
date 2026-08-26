package org.kosit.xvrl.api;

import java.util.List;

import org.kosit.base.annotation.ReturnsImmutableObject;

public interface BaseMessage {

    List<Object> getContent();

    @ReturnsImmutableObject
    default List<String> getMessageStrings() {
        return getContent().stream().map(Object::toString).toList();
    }
}
