package org.kosit.xvrl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;
import org.w3c.dom.Node;

/**
 * Base class of all XVRL data model types with mixed content, hence character data interleaved with foreign elements.
 *
 * <p>
 * The content is stored as a list of {@link Object} to preserve the order in which character data and elements appear.
 * The elements of that list are restricted to the types accepted by the {@code addContent} methods of the builder -
 * {@link String} for character data and {@link Node} for foreign elements. {@link XvrlMessage} and {@link XvrlValueOf}
 * additionally accept nested {@link XvrlValueOf} objects.
 *
 * @author Philip Helger
 */
public abstract class AbstractXvrlContentObject extends AbstractXvrlCommonObject {

    private final List<Object> content;

    protected AbstractXvrlContentObject(final AbstractContentBuilder<?, ?> builder) {
        super(builder);
        this.content = List.copyOf(builder.content);
    }

    /**
     * @return all content items in document order. Each item is either a {@link String}, a {@link Node} or an
     *         {@link XvrlValueOf}. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public final List<Object> getContent() {
        return this.content;
    }

    /**
     * @return <code>true</code> if at least one content item is present.
     */
    public final boolean hasContent() {
        return !this.content.isEmpty();
    }

    /**
     * @return the string representation of every content item. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public final List<String> getContentStrings() {
        return this.content.stream().map(Object::toString).toList();
    }

    /**
     * @return the string representation of all content items, concatenated without a separator. Never <code>null</code>
     *         but maybe empty.
     */
    public final String getContentText() {
        return this.content.stream().map(Object::toString).collect(Collectors.joining());
    }

    /**
     * Abstract base builder for all {@link AbstractXvrlContentObject} implementations.
     *
     * @param <T> the data type created by this builder
     * @param <IMPLTYPE> the effective builder implementation type, for fluent chaining
     */
    public abstract static class AbstractContentBuilder<T, IMPLTYPE extends AbstractContentBuilder<T, IMPLTYPE>>
            extends AbstractCommonBuilder<T, IMPLTYPE> {

        private final List<Object> content = new ArrayList<>();

        protected AbstractContentBuilder() {
        }

        protected AbstractContentBuilder(final AbstractXvrlContentObject src) {
            super(src);
            this.content.addAll(src.content);
        }

        protected final void addContentItem(final Object item) {
            this.content.add(item);
        }

        /**
         * Adds a piece of character data to the content.
         *
         * @param text the text to add. May be <code>null</code> in which case nothing happens.
         * @return this for chaining
         */
        public final IMPLTYPE addContent(final @Nullable String text) {
            if (text != null)
                addContentItem(text);
            return thisAsT();
        }

        /**
         * Adds a foreign element to the content.
         *
         * @param node the element to add. May be <code>null</code> in which case nothing happens.
         * @return this for chaining
         */
        public final IMPLTYPE addContent(final @Nullable Node node) {
            if (node != null)
                addContentItem(node);
            return thisAsT();
        }

        /**
         * Adds all provided content items, dispatching on the runtime type of every single item.
         *
         * @param items the items to add. May be <code>null</code> in which case nothing happens.
         * @return this for chaining
         * @throws IllegalArgumentException if one of the items is of a type that is not allowed as XVRL content
         */
        public final IMPLTYPE addAllContent(final @Nullable Iterable<?> items) {
            if (items != null)
                for (final Object item : items)
                    if (item != null)
                        addUntypedContent(item);
            return thisAsT();
        }

        /**
         * Adds a single content item of an unknown static type. Subclasses that accept additional content types
         * override this method.
         *
         * @param item the item to add. Never <code>null</code>.
         * @throws IllegalArgumentException if the item is of a type that is not allowed as XVRL content
         */
        protected void addUntypedContent(final Object item) {
            if (item instanceof String || item instanceof Node)
                addContentItem(item);
            else
                throw new IllegalArgumentException("Unsupported XVRL content type " + item.getClass().getName());
        }

        /**
         * Removes all content items.
         *
         * @return this for chaining
         */
        public final IMPLTYPE removeAllContent() {
            this.content.clear();
            return thisAsT();
        }
    }
}
