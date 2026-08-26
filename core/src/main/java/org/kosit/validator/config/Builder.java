package org.kosit.validator.config;

import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.model.SingleProcessingResult;

/**
 * Internal interface for creating object builders.
 * 
 * @author Andreas Penski
 */
interface Builder<T> {

    /**
     * Creates an object based on artifacts provided via a defined {@link ContentRepository}.
     * 
     * @param repository the {@link ContentRepository}
     * @return the result of building the object
     */
    SingleProcessingResult<T, String> build(ContentRepository repository);
}
