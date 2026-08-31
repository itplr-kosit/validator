package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;

/**
 * The XVRL {@code location} element - a pointer into the validated document.
 *
 * @author Philip Helger
 */
public final class XvrlLocation extends AbstractXvrlObject {

    private final @Nullable String xpathDefaultNamespace;

    private final @Nullable String xpath;

    private final @Nullable String jsonPointer;

    private final @Nullable String jsonPath;

    private final @Nullable String href;

    private final @Nullable Long line;

    private final @Nullable Long column;

    private final @Nullable Long octetPosition;

    private XvrlLocation(final Builder builder) {
        super(builder);
        this.xpathDefaultNamespace = builder.xpathDefaultNamespace;
        this.xpath = builder.xpath;
        this.jsonPointer = builder.jsonPointer;
        this.jsonPath = builder.jsonPath;
        this.href = builder.href;
        this.line = builder.line;
        this.column = builder.column;
        this.octetPosition = builder.octetPosition;
    }

    /**
     * @return the value of {@code xpath-default-namespace}. May be <code>null</code>.
     */
    public @Nullable String getXPathDefaultNamespace() {
        return this.xpathDefaultNamespace;
    }

    /**
     * @return the XPath expression pointing to the location. May be <code>null</code>.
     */
    public @Nullable String getXPath() {
        return this.xpath;
    }

    /**
     * @return the JSON pointer pointing to the location. May be <code>null</code>.
     */
    public @Nullable String getJsonPointer() {
        return this.jsonPointer;
    }

    /**
     * @return the JSON path pointing to the location. May be <code>null</code>.
     */
    public @Nullable String getJsonPath() {
        return this.jsonPath;
    }

    /**
     * @return the URI of the document this location refers to. May be <code>null</code>.
     */
    public @Nullable String getHref() {
        return this.href;
    }

    /**
     * @return the 1-based line number. May be <code>null</code>.
     */
    public @Nullable Long getLine() {
        return this.line;
    }

    /**
     * @return the 1-based column number. May be <code>null</code>.
     */
    public @Nullable Long getColumn() {
        return this.column;
    }

    /**
     * @return the 1-based octet position. May be <code>null</code>.
     */
    public @Nullable Long getOctetPosition() {
        return this.octetPosition;
    }

    @Override
    public String toString() {
        return "XvrlLocation[xpath=" + this.xpath + ", href=" + this.href + ", line=" + this.line + ", column=" + this.column + "]";
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
     * Builder for {@link XvrlLocation}.
     */
    public static final class Builder extends AbstractBuilder<XvrlLocation, Builder> {

        private @Nullable String xpathDefaultNamespace;

        private @Nullable String xpath;

        private @Nullable String jsonPointer;

        private @Nullable String jsonPath;

        private @Nullable String href;

        private @Nullable Long line;

        private @Nullable Long column;

        private @Nullable Long octetPosition;

        private Builder() {
        }

        private Builder(final XvrlLocation src) {
            super(src);
            this.xpathDefaultNamespace = src.xpathDefaultNamespace;
            this.xpath = src.xpath;
            this.jsonPointer = src.jsonPointer;
            this.jsonPath = src.jsonPath;
            this.href = src.href;
            this.line = src.line;
            this.column = src.column;
            this.octetPosition = src.octetPosition;
        }

        public Builder xpathDefaultNamespace(final @Nullable String xpathDefaultNamespace) {
            this.xpathDefaultNamespace = xpathDefaultNamespace;
            return this;
        }

        public Builder xpath(final @Nullable String xpath) {
            this.xpath = xpath;
            return this;
        }

        public Builder jsonPointer(final @Nullable String jsonPointer) {
            this.jsonPointer = jsonPointer;
            return this;
        }

        public Builder jsonPath(final @Nullable String jsonPath) {
            this.jsonPath = jsonPath;
            return this;
        }

        public Builder href(final @Nullable String href) {
            this.href = href;
            return this;
        }

        public Builder line(final @Nullable Long line) {
            this.line = line;
            return this;
        }

        public Builder column(final @Nullable Long column) {
            this.column = column;
            return this;
        }

        public Builder octetPosition(final @Nullable Long octetPosition) {
            this.octetPosition = octetPosition;
            return this;
        }

        @Override
        public XvrlLocation build() {
            return new XvrlLocation(this);
        }
    }
}
