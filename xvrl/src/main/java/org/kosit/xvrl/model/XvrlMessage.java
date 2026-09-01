package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Node;

/**
 * The XVRL {@code message} element - the human readable text of a detection. It has mixed content and the
 * {@code common.attr} attributes. Besides character data and foreign elements the content may contain nested
 * {@link XvrlValueOf} objects.
 *
 * @author Philip Helger
 */
public final class XvrlMessage extends AbstractXvrlContentObject {

    private XvrlMessage(final Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "XvrlMessage[" + getContentText() + "]";
    }

    /**
     * @return a new empty builder. Never <code>null</code>.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a prefilled builder with the provided text as the only content item.
     *
     * @param text the message text. May be <code>null</code>.
     * @return a new builder. Never <code>null</code>.
     */
    public static Builder builder(final @Nullable String text) {
        return new Builder().addContent(text);
    }

    /**
     * @return a new builder prefilled with the state of this object. Never <code>null</code>.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for {@link XvrlMessage}.
     */
    public static final class Builder extends AbstractContentBuilder<XvrlMessage, Builder> {

        private Builder() {
        }

        private Builder(final XvrlMessage src) {
            super(src);
        }

        /**
         * Adds a nested {@code value-of} element to the content.
         *
         * @param valueOf the element to add. May be <code>null</code> in which case nothing happens.
         * @return this for chaining
         */
        public Builder addContent(final @Nullable XvrlValueOf valueOf) {
            if (valueOf != null)
                addContentItem(valueOf);
            return this;
        }

        /**
         * Adds a nested {@code value-of} element to the content.
         *
         * @param valueOf the element to add. May be <code>null</code> in which case nothing happens.
         * @return this for chaining
         */
        public Builder addContent(final XvrlValueOf.@Nullable Builder valueOf) {
            return addContent(valueOf == null ? null : valueOf.build());
        }

        @Override
        protected void addUntypedContent(final Object item) {
            if (item instanceof String || item instanceof Node || item instanceof XvrlValueOf)
                addContentItem(item);
            else
                throw new IllegalArgumentException("Unsupported XVRL content type " + item.getClass().getName());
        }

        @Override
        public XvrlMessage build() {
            return new XvrlMessage(this);
        }
    }
}
