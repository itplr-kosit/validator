package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;

/**
 * The XVRL {@code validator} element - the identification of the software that produced a report. It has mixed content
 * and the {@code common.attr} attributes.
 *
 * @author Philip Helger
 */
public final class XvrlValidator extends AbstractXvrlContentObject {

    private final @Nullable String name;

    private final @Nullable String version;

    private XvrlValidator(final Builder builder) {
        super(builder);
        this.name = builder.name;
        this.version = builder.version;
    }

    /**
     * @return the name of the validator. May be <code>null</code>.
     */
    public @Nullable String getName() {
        return this.name;
    }

    /**
     * @return the version of the validator. May be <code>null</code>.
     */
    public @Nullable String getVersion() {
        return this.version;
    }

    @Override
    public String toString() {
        return "XvrlValidator[name=" + this.name + ", version=" + this.version + "]";
    }

    /**
     * @return a new empty builder. Never <code>null</code>.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a prefilled builder for the provided validator name.
     *
     * @param name the name of the validator. May be <code>null</code>.
     * @return a new builder. Never <code>null</code>.
     */
    public static Builder builder(final @Nullable String name) {
        return new Builder().name(name);
    }

    /**
     * @return a new builder prefilled with the state of this object. Never <code>null</code>.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for {@link XvrlValidator}.
     */
    public static final class Builder extends AbstractContentBuilder<XvrlValidator, Builder> {

        private @Nullable String name;

        private @Nullable String version;

        private Builder() {
        }

        private Builder(final XvrlValidator src) {
            super(src);
            this.name = src.name;
            this.version = src.version;
        }

        public Builder name(final @Nullable String name) {
            this.name = name;
            return this;
        }

        public Builder version(final @Nullable String version) {
            this.version = version;
            return this;
        }

        @Override
        public XvrlValidator build() {
            return new XvrlValidator(this);
        }
    }
}
