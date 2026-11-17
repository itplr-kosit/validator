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
package org.kosit.conformatron.detection;

import org.conformatron.api.model.detection.CTDetectionLocation;
import org.jspecify.annotations.Nullable;
import org.xml.sax.SAXParseException;

/**
 * Immutable implementation of {@link CTDetectionLocation}. Instances are created through the {@link Builder}, as all
 * fields are optional.
 *
 * @author Andreas Schmitz
 * @author Philip Helger
 */
public final class DetectionLocation implements CTDetectionLocation {

    private final String resourceId;

    private final int lineNumber;

    private final int columnNumber;

    private final String xpath;

    private DetectionLocation(final Builder builder) {
        this.resourceId = builder.resourceId;
        this.lineNumber = builder.lineNumber > 0 ? builder.lineNumber : ILLEGAL_NUMBER;
        this.columnNumber = builder.columnNumber > 0 ? builder.columnNumber : ILLEGAL_NUMBER;
        this.xpath = builder.xpath;
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

    /**
     * @return a new empty builder. Never <code>null</code>.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return a new builder prefilled with the state of this object. Never <code>null</code>.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for {@link DetectionLocation}.
     */
    public static final class Builder {

        private @Nullable String resourceId;

        private int lineNumber = ILLEGAL_NUMBER;

        private int columnNumber = ILLEGAL_NUMBER;

        private @Nullable String xpath;

        private Builder() {
        }

        private Builder(final DetectionLocation src) {
            this.resourceId = src.resourceId;
            this.lineNumber = src.lineNumber;
            this.columnNumber = src.columnNumber;
            this.xpath = src.xpath;
        }

        public Builder resourceId(final @Nullable String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        /**
         * @param lineNumber the 1-based line number. Anything &le; 0 means "no line number".
         * @return this for chaining
         */
        public Builder lineNumber(final int lineNumber) {
            this.lineNumber = lineNumber;
            return this;
        }

        /**
         * @param columnNumber the 1-based column number. Anything &le; 0 means "no column number".
         * @return this for chaining
         */
        public Builder columnNumber(final int columnNumber) {
            this.columnNumber = columnNumber;
            return this;
        }

        /**
         * Takes line and column number from the position the parser reported.
         *
         * @param e the parse exception. May not be <code>null</code>.
         * @return this for chaining
         */
        public Builder location(final SAXParseException e) {
            if (e == null)
                return this;
            return lineNumber(e.getLineNumber()).columnNumber(e.getColumnNumber());
        }

        public Builder xpath(final @Nullable String xpath) {
            this.xpath = xpath;
            return this;
        }

        /**
         * @return the immutable object created from the current builder state. Never <code>null</code>.
         */
        public DetectionLocation build() {
            return new DetectionLocation(this);
        }
    }
}
