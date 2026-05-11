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

package org.kosit.validator.api;

/**
 * Error object for providing errors from internal processing, e.g. schema validation errors.
 *
 * @author Andreas Penski
 */
public interface XmlError {

    /**
     * Returns the error message.
     *
     * @return The message itself
     */
    String getMessage();

    /**
     * Indicates the severity of the error message.
     *
     * @return The severity of the error.
     * @see Severity
     */
    Severity getSeverity();

    /**
     * Optionally returns a row number from which the error originates.
     *
     * @return The row number or <code>null</code>.
     */
    Integer getRowNumber();

    /**
     * Optionally returns a column number from which the error originates.
     *
     * @return The column number or <code>null</code>.
     */
    Integer getColumnNumber();

    enum Severity {
        SEVERITY_WARNING, SEVERITY_ERROR, SEVERITY_FATAL_ERROR;
    }

}
