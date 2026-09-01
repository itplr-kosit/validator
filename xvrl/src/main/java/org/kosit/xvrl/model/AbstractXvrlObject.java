package org.kosit.xvrl.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;

/**
 * Base class of all XVRL data model types that carry arbitrary foreign attributes - the {@code anyother.attr} attribute
 * group of the XVRL schema.
 *
 * @author Philip Helger
 */
public abstract class AbstractXvrlObject {

    private final Map<QName, String> otherAttributes;

    protected AbstractXvrlObject(final AbstractBuilder<?, ?> builder) {
        this.otherAttributes = Collections.unmodifiableMap(new LinkedHashMap<>(builder.otherAttributes));
    }

    /**
     * @return All foreign attributes of this object, in the order in which they were added. Never <code>null</code> but
     *         maybe empty.
     */
    @ReturnsImmutableObject
    public final Map<QName, String> getOtherAttributes() {
        return this.otherAttributes;
    }

    /**
     * @param name the name of the foreign attribute to retrieve. May be <code>null</code>.
     * @return the value of the foreign attribute or <code>null</code> if it is not present.
     */
    public final @Nullable String getOtherAttribute(final @Nullable QName name) {
        return name == null ? null : this.otherAttributes.get(name);
    }

    /**
     * Abstract base builder for all {@link AbstractXvrlObject} implementations.
     *
     * @param <T> the data type created by this builder
     * @param <IMPLTYPE> the effective builder implementation type, for fluent chaining
     */
    public abstract static class AbstractBuilder<T, IMPLTYPE extends AbstractBuilder<T, IMPLTYPE>> {

        private final Map<QName, String> otherAttributes = new LinkedHashMap<>();

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(final AbstractXvrlObject src) {
            this.otherAttributes.putAll(src.otherAttributes);
        }

        @SuppressWarnings("unchecked")
        protected final IMPLTYPE thisAsT() {
            return (IMPLTYPE) this;
        }

        /**
         * Sets a single foreign attribute. A <code>null</code> value removes a previously set attribute.
         *
         * @param name the attribute name. May be <code>null</code> in which case nothing happens.
         * @param value the attribute value. May be <code>null</code>.
         * @return this for chaining
         */
        public final IMPLTYPE otherAttribute(final @Nullable QName name, final @Nullable String value) {
            if (name != null) {
                if (value == null)
                    this.otherAttributes.remove(name);
                else
                    this.otherAttributes.put(name, value);
            }
            return thisAsT();
        }

        /**
         * Replaces all foreign attributes with the provided ones.
         *
         * @param values the new foreign attributes. May be <code>null</code> to remove all of them.
         * @return this for chaining
         */
        public final IMPLTYPE otherAttributes(final @Nullable Map<QName, String> values) {
            this.otherAttributes.clear();
            return addOtherAttributes(values);
        }

        /**
         * Adds all provided foreign attributes, keeping the already contained ones.
         *
         * @param values the foreign attributes to add. May be <code>null</code>.
         * @return this for chaining
         */
        public final IMPLTYPE addOtherAttributes(final @Nullable Map<QName, String> values) {
            if (values != null)
                for (final Map.Entry<QName, String> entry : values.entrySet())
                    otherAttribute(entry.getKey(), entry.getValue());
            return thisAsT();
        }

        /**
         * @param name the name of the foreign attribute to retrieve. May be <code>null</code>.
         * @return the value of the foreign attribute set so far or <code>null</code> if it is not present.
         */
        public final @Nullable String getOtherAttribute(final @Nullable QName name) {
            return name == null ? null : this.otherAttributes.get(name);
        }

        /**
         * @return the immutable object created from the current builder state. Never <code>null</code>.
         */
        public abstract T build();
    }
}
