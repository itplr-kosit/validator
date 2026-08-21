package org.kosit.validator.impl.conformatron.action.parsedoc;

import org.conformatron.api.model.action.ECTActionType;
import org.conformatron.api.model.action.CTAction;

public abstract class AbstractParseDocumentAction implements CTAction {
    public final ECTActionType getType() {
        return ECTActionType.PARSE_DOCUMENT;
    }
}
