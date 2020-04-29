package de.kosit.validationtool.config;

import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.model.Result;

/**
 * @author Andreas Penski
 */
public interface Builder<T> {

    Result<T, String> build(ContentRepository repository);
}
