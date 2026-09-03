package org.conformatron.api.model.source;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;

/**
 * Special validation source for XML.
 */
public interface CTParsedValidationSourceXML extends CTParsedValidationSource {

    @Override
    default Object getParsedContent() {
        return getAsDom();
    }

    @Nullable
    Document getAsDom();
}
