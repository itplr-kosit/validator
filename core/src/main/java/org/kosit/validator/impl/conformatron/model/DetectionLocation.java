/*
 * Copyright 2017-2026  Koordinierungsstelle für IT-Standards (KoSIT)
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
package org.kosit.validator.impl.conformatron.model;

import org.conformatron.api.model.detection.ICTDetectionLocation;

/**
 * Immutable implementation of {@link ICTDetectionLocation}.
 *
 * @author Andreas Schmitz
 */
public final class DetectionLocation implements ICTDetectionLocation {

    private final String resourceId;

    private final int lineNumber;

    private final int columnNumber;

    public DetectionLocation(final String resourceId, final int lineNumber, final int columnNumber) {
        this.resourceId = resourceId;
        this.lineNumber = lineNumber > 0 ? lineNumber : ILLEGAL_NUMBER;
        this.columnNumber = columnNumber > 0 ? columnNumber : ILLEGAL_NUMBER;
    }

    /**
     * Creates a location referencing a resource without line/column information.
     *
     * @param resourceId the resource identifier, may be null
     * @return a new location
     */
    public static DetectionLocation ofResource(final String resourceId) {
        return new DetectionLocation(resourceId, ILLEGAL_NUMBER, ILLEGAL_NUMBER);
    }

    @Override
    public String getResourceID() {
        return this.resourceId;
    }

    @Override
    public int getLineNumber() {
        return this.lineNumber;
    }

    @Override
    public int getColumnNumber() {
        return this.columnNumber;
    }
}
