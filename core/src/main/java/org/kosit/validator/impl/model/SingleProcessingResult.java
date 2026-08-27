package org.kosit.validator.impl.model;

import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * A result object that holds the actual result and optionally various error objects.
 *
 * @param <T> the type of the result object
 * @param <E> the type of the error object
 */
public class SingleProcessingResult<T, E> {

    private T object;

    private List<E> errors;

    /**
     * Creates a new result with a result object.
     *
     * @param o
     */
    public SingleProcessingResult(final @Nullable T o) {
        this(o, Collections.emptyList());
    }

    /**
     * Creates a new result with errors.
     *
     * @param errors the errors
     */
    public SingleProcessingResult(final @Nullable List<E> errors) {
        this(null, errors);
    }

    public SingleProcessingResult(final @Nullable T object, final @Nullable List<E> errors) {
        this.object = object;
        this.errors = errors;
    }

    /**
     * Indicates whether the result is valid, i.e. without errors.
     *
     * @return true if successful
     */
    public boolean isValid() {
        return this.object != null && getErrors().isEmpty();
    }

    public List<E> getErrors() {
        return this.errors == null ? Collections.emptyList() : this.errors;
    }

    /**
     * Indicates whether the result is not valid, i.e. errors have been collected.
     *
     * @return true if errors are present.
     */
    public boolean isInvalid() {
        return !isValid();
    }

    public T getObject() {
        return this.object;
    }
}
