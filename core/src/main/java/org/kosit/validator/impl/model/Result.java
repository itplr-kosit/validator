package org.kosit.validator.impl.model;

import java.util.Collection;
import java.util.Collections;

/**
 * A result object that holds the actual result and optionally various error objects.
 *
 * @param <T> the type of the result object
 * @param <E> the type of the error object
 */
public class Result<T, E> {

    private T object;

    private Collection<E> errors;

    /**
     * Creates a new result with errors.
     *
     * @param errors the errors
     */
    public Result(final Collection<E> errors) {
        this(null, errors);
    }

    /**
     * Creates a new result with a result object.
     *
     * @param o
     */
    public Result(final T o) {
        this(o, Collections.emptyList());
    }

    /**
     * Indicates whether the result is valid, i.e. without errors.
     *
     * @return true if successful
     */
    public boolean isValid() {
        return this.object != null && getErrors().isEmpty();
    }

    public Collection<E> getErrors() {
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

    public Result(final T object, final Collection<E> errors) {
        this.object = object;
        this.errors = errors;
    }

    public Result() {
    }
}
