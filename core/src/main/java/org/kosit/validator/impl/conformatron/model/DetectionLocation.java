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

import org.conformatron.api.model.detection.CTDetectionLocation;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.xml.sax.SAXParseException;

/**
 * Immutable implementation of {@link CTDetectionLocation}.
 *
 * @author Andreas Schmitz
 * @author Philip Helger
 */
public final class DetectionLocation implements CTDetectionLocation {

    private final String resourceId;

    private final int lineNumber;

    private final int columnNumber;

    private final String xpath;

    /**
     * Creates a location referencing a resource without line/column information.
     *
     * @param resourceId the resource identifier, may be null
     * @return a new location
     */
    public static DetectionLocation of(final @Nullable String resourceId) {
        return new DetectionLocation(resourceId, ILLEGAL_NUMBER, ILLEGAL_NUMBER);
    }

    @NonNull
    public static DetectionLocation of(final @Nullable String resourceId, final @NonNull SAXParseException e) {
        return new DetectionLocation(resourceId, e.getLineNumber(), e.getColumnNumber());
    }

    /**
     * Creates a location that points at a node inside the resource. Rule engines report where a finding applies as an
     * XPath expression; keeping it here rather than in the message text is what lets a report consumer jump to the spot
     * instead of parsing prose.
     *
     * @param resourceId the resource identifier, may be null
     * @param xpath the XPath expression selecting the node the finding applies to, may be null
     * @return a new location
     */
    @NonNull
    public static DetectionLocation ofXPath(final @Nullable String resourceId, final @Nullable String xpath) {
        return new DetectionLocation(resourceId, ILLEGAL_NUMBER, ILLEGAL_NUMBER, xpath);
    }

    public DetectionLocation(final @Nullable String resourceId, final int lineNumber, final int columnNumber) {
        this(resourceId, lineNumber, columnNumber, null);
    }

    public DetectionLocation(final @Nullable String resourceId, final int lineNumber, final int columnNumber,
            final @Nullable String xpath) {
        this.resourceId = resourceId;
        this.lineNumber = lineNumber > 0 ? lineNumber : ILLEGAL_NUMBER;
        this.columnNumber = columnNumber > 0 ? columnNumber : ILLEGAL_NUMBER;
        this.xpath = xpath;
    }

    /**
     * @return The XPath expression selecting the node this detection applies to. May be <code>null</code>.
     */
    public @Nullable String getXPath() {
        return this.xpath;
    }

    public boolean hasXPath() {
        return this.xpath != null && !this.xpath.isBlank();
    }

    @Override
    @Nullable
    public String getResourceId() {
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
