package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;

/**
 * The XVRL {@code schema} element - the identification of a schema used during validation. It has mixed content and the
 * {@code common.attr} attributes.
 *
 * @author Philip Helger
 */
public final class XvrlSchema extends AbstractXvrlContentObject {

    private final @Nullable String href;

    private final @Nullable String schemaTypeNs;

    private final @Nullable String version;

    private XvrlSchema(final Builder builder) {
        super(builder);
        this.href = builder.href;
        this.schemaTypeNs = builder.schemaTypeNs;
        this.version = builder.version;
    }

    /**
     * @return the URI of the schema. May be <code>null</code>.
     */
    public @Nullable String getHref() {
        return this.href;
    }

    /**
     * @return the namespace URI identifying the schema language, hence the {@code schematypens} attribute. May be
     *         <code>null</code>.
     */
    public @Nullable String getSchemaTypeNs() {
        return this.schemaTypeNs;
    }

    /**
     * @return the version of the schema. May be <code>null</code>.
     */
    public @Nullable String getVersion() {
        return this.version;
    }

    @Override
    public String toString() {
        return "XvrlSchema[href=" + this.href + ", schematypens=" + this.schemaTypeNs + ", version=" + this.version + "]";
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
     * Builder for {@link XvrlSchema}.
     */
    public static final class Builder extends AbstractContentBuilder<XvrlSchema, Builder> {

        private @Nullable String href;

        private @Nullable String schemaTypeNs;

        private @Nullable String version;

        private Builder() {
        }

        private Builder(final XvrlSchema src) {
            super(src);
            this.href = src.href;
            this.schemaTypeNs = src.schemaTypeNs;
            this.version = src.version;
        }

        public Builder href(final @Nullable String href) {
            this.href = href;
            return this;
        }

        public Builder schemaTypeNs(final @Nullable String schemaTypeNs) {
            this.schemaTypeNs = schemaTypeNs;
            return this;
        }

        public Builder version(final @Nullable String version) {
            this.version = version;
            return this;
        }

        @Override
        public XvrlSchema build() {
            return new XvrlSchema(this);
        }
    }
}
