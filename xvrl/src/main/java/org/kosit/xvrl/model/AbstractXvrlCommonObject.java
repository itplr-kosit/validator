package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;
import org.kosit.base.xml.XmlHelper;

/**
 * Base class of all XVRL data model types that carry the {@code common.attr} attribute group of the XVRL schema, hence
 * {@code xml:lang}, {@code xml:id}, {@code xml:base} and {@code xpath-default-namespace}.
 *
 * @author Philip Helger
 */
public abstract class AbstractXvrlCommonObject extends AbstractXvrlObject {

    private final @Nullable String lang;

    private final @Nullable String id;

    private final @Nullable String base;

    private final @Nullable String xpathDefaultNamespace;

    protected AbstractXvrlCommonObject(final AbstractCommonBuilder<?, ?> builder) {
        super(builder);
        this.lang = builder.lang;
        this.id = builder.id;
        this.base = builder.base;
        this.xpathDefaultNamespace = builder.xpathDefaultNamespace;
    }

    /**
     * @return the value of {@code xml:lang}. May be <code>null</code>.
     */
    public final @Nullable String getLang() {
        return this.lang;
    }

    /**
     * @return the value of {@code xml:id}. May be <code>null</code>.
     */
    public final @Nullable String getID() {
        return this.id;
    }

    /**
     * @return the value of {@code xml:base}. May be <code>null</code>.
     */
    public final @Nullable String getBase() {
        return this.base;
    }

    /**
     * @return the value of {@code xpath-default-namespace}. May be <code>null</code>.
     */
    public final @Nullable String getXPathDefaultNamespace() {
        return this.xpathDefaultNamespace;
    }

    /**
     * Abstract base builder for all {@link AbstractXvrlCommonObject} implementations.
     *
     * @param <T> the data type created by this builder
     * @param <IMPLTYPE> the effective builder implementation type, for fluent chaining
     */
    public abstract static class AbstractCommonBuilder<T, IMPLTYPE extends AbstractCommonBuilder<T, IMPLTYPE>>
            extends AbstractBuilder<T, IMPLTYPE> {

        private @Nullable String lang;

        private @Nullable String id;

        private @Nullable String base;

        private @Nullable String xpathDefaultNamespace;

        protected AbstractCommonBuilder() {
        }

        protected AbstractCommonBuilder(final AbstractXvrlCommonObject src) {
            super(src);
            this.lang = src.lang;
            this.id = src.id;
            this.base = src.base;
            this.xpathDefaultNamespace = src.xpathDefaultNamespace;
        }

        /**
         * @param lang the value of {@code xml:lang}. May be <code>null</code>.
         * @return this for chaining
         */
        public final IMPLTYPE lang(final @Nullable String lang) {
            this.lang = lang;
            return thisAsT();
        }

        /**
         * Sets the value of {@code xml:id}. As the underlying XML Schema type is {@code xs:ID} the provided value is
         * converted to a valid {@code xs:NCName} before it is stored.
         *
         * @param id the value of {@code xml:id}. May be <code>null</code>.
         * @return this for chaining
         */
        public final IMPLTYPE id(final @Nullable String id) {
            this.id = XmlHelper.createValidNCName(id);
            return thisAsT();
        }

        /**
         * @param base the value of {@code xml:base}. May be <code>null</code>.
         * @return this for chaining
         */
        public final IMPLTYPE base(final @Nullable String base) {
            this.base = base;
            return thisAsT();
        }

        /**
         * @param xpathDefaultNamespace the value of {@code xpath-default-namespace}. May be <code>null</code>.
         * @return this for chaining
         */
        public final IMPLTYPE xpathDefaultNamespace(final @Nullable String xpathDefaultNamespace) {
            this.xpathDefaultNamespace = xpathDefaultNamespace;
            return thisAsT();
        }
    }
}
