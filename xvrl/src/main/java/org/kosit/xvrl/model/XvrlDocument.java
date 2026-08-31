package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;

/**
 * The XVRL {@code document} element - the identification of a validated document. It has mixed content and the
 * {@code common.attr} attributes.
 *
 * @author Philip Helger
 */
public final class XvrlDocument extends AbstractXvrlContentObject {

    private final @Nullable String href;

    private XvrlDocument(final Builder builder) {
        super(builder);
        this.href = builder.href;
    }

    /**
     * @return the URI of the document. May be <code>null</code>.
     */
    public @Nullable String getHref() {
        return this.href;
    }

    @Override
    public String toString() {
        return "XvrlDocument[href=" + this.href + "]";
    }

    /**
     * @return a new empty builder. Never <code>null</code>.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a prefilled builder for the provided document URI.
     *
     * @param href the URI of the document. May be <code>null</code>.
     * @return a new builder. Never <code>null</code>.
     */
    public static Builder builder(final @Nullable String href) {
        return new Builder().href(href);
    }

    /**
     * @return a new builder prefilled with the state of this object. Never <code>null</code>.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for {@link XvrlDocument}.
     */
    public static final class Builder extends AbstractContentBuilder<XvrlDocument, Builder> {

        private @Nullable String href;

        private Builder() {
        }

        private Builder(final XvrlDocument src) {
            super(src);
            this.href = src.href;
        }

        public Builder href(final @Nullable String href) {
            this.href = href;
            return this;
        }

        @Override
        public XvrlDocument build() {
            return new XvrlDocument(this);
        }
    }
}
