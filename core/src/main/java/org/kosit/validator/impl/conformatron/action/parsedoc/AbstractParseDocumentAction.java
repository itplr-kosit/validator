package org.kosit.validator.impl.conformatron.action.parsedoc;

import org.conformatron.api.model.action.CTAction;
import org.conformatron.api.model.action.CTActionType;

public abstract class AbstractParseDocumentAction implements CTAction {

    public final CTActionType getType() {
        return CTActionType.PARSE_DOCUMENT;
    }
}
