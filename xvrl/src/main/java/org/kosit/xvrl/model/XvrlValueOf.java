package org.kosit.xvrl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;
import org.w3c.dom.Node;

/**
 * The XVRL {@code value-of} element that may occur inside an {@link XvrlMessage} or inside another {@link XvrlValueOf}.
 * It has mixed content and arbitrary foreign attributes, but none of the {@code common.attr} attributes.
 *
 * @author Philip Helger
 */
public final class XvrlValueOf extends AbstractXvrlObject {

    private final List<Object> content;

    private XvrlValueOf(final Builder builder) {
        super(builder);
        this.content = List.copyOf(builder.content);
    }

    /**
     * @return all content items in document order. Each item is either a {@link String}, a {@link Node} or a nested
     *         {@link XvrlValueOf}. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<Object> getContent() {
        return this.content;
    }

    /**
     * @return <code>true</code> if at least one content item is present.
     */
    public boolean hasContent() {
        return !this.content.isEmpty();
    }

    /**
     * @return the string representation of all content items, concatenated without a separator. Never <code>null</code>
     *         but maybe empty.
     */
    public String getContentText() {
        return this.content.stream().map(Object::toString).collect(Collectors.joining());
    }

    @Override
    public String toString() {
        return getContentText();
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
     * Builder for {@link XvrlValueOf}.
     */
    public static final class Builder extends AbstractBuilder<XvrlValueOf, Builder> {

        private final List<Object> content = new ArrayList<>();

        private Builder() {
        }

        private Builder(final XvrlValueOf src) {
            super(src);
            this.content.addAll(src.content);
        }

        public Builder addContent(final @Nullable String text) {
            if (text != null)
                this.content.add(text);
            return this;
        }

        public Builder addContent(final @Nullable Node node) {
            if (node != null)
                this.content.add(node);
            return this;
        }

        public Builder addContent(final @Nullable XvrlValueOf valueOf) {
            if (valueOf != null)
                this.content.add(valueOf);
            return this;
        }

        public Builder addContent(final @Nullable Builder valueOf) {
            return addContent(valueOf == null ? null : valueOf.build());
        }

        /**
         * Adds all provided content items, dispatching on the runtime type of every single item.
         *
         * @param items the items to add. May be <code>null</code> in which case nothing happens.
         * @return this for chaining
         * @throws IllegalArgumentException if one of the items is of a type that is not allowed as XVRL content
         */
        public Builder addAllContent(final @Nullable Iterable<?> items) {
            if (items != null)
                for (final Object item : items)
                    if (item != null) {
                        if (item instanceof String || item instanceof Node || item instanceof XvrlValueOf)
                            this.content.add(item);
                        else
                            throw new IllegalArgumentException("Unsupported XVRL content type " + item.getClass().getName());
                    }
            return this;
        }

        public Builder removeAllContent() {
            this.content.clear();
            return this;
        }

        @Override
        public XvrlValueOf build() {
            return new XvrlValueOf(this);
        }
    }
}
