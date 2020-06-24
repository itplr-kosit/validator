package de.kosit.validationtool.config;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.model.Result;

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
    Result<T, String> build(ContentRepository repository);
}
