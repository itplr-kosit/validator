/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.kosit.validationtool.impl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A result object that holds the actual result and optionally various error objects.
 *
 * @param <T> the type of the result object
 * @param <E> the type of the error object
 */
public class Result<T, E> {

    private T object;

    private List<E> errors = new ArrayList<>();

    /**
     * Creates a new result with errors
     *
     * @param errors the errors
     */
    public Result(final List<E> errors) {
        this(null, errors);
    }

    /**
     * Creates a new result with a result object
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
        return object != null && errors.isEmpty();
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

    public List<E> getErrors() {
        return this.errors;
    }

    public Result(final T object, final List<E> errors) {
        this.object = object;
        this.errors = errors;
    }

    public Result() {
    }
}
