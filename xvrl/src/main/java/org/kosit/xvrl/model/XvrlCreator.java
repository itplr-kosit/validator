package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;

/**
 * The XVRL {@code creator} element - the identification of the software that created the report, together with the
 * optional invocation that produced it.
 *
 * @author Philip Helger
 */
public final class XvrlCreator extends AbstractXvrlCommonObject {

    private final @Nullable String name;

    private final @Nullable String version;

    private final @Nullable String invocation;

    private XvrlCreator(final Builder builder) {
        super(builder);
        this.name = builder.name;
        this.version = builder.version;
        this.invocation = builder.invocation;
    }

    /**
     * @return the name of the creator. May be <code>null</code>.
     */
    public @Nullable String getName() {
        return this.name;
    }

    /**
     * @return the version of the creator. May be <code>null</code>.
     */
    public @Nullable String getVersion() {
        return this.version;
    }

    /**
     * @return the invocation that created the report. May be <code>null</code>.
     */
    public @Nullable String getInvocation() {
        return this.invocation;
    }

    @Override
    public String toString() {
        return "XvrlCreator[name=" + this.name + ", version=" + this.version + "]";
    }

    /**
     * @return a new empty builder. Never <code>null</code>.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a prefilled builder for the provided creator name.
     *
     * @param name the name of the creator. May be <code>null</code>.
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
     * Builder for {@link XvrlCreator}.
     */
    public static final class Builder extends AbstractCommonBuilder<XvrlCreator, Builder> {

        private @Nullable String name;

        private @Nullable String version;

        private @Nullable String invocation;

        private Builder() {
        }

        private Builder(final XvrlCreator src) {
            super(src);
            this.name = src.name;
            this.version = src.version;
            this.invocation = src.invocation;
        }

        public Builder name(final @Nullable String name) {
            this.name = name;
            return this;
        }

        public Builder version(final @Nullable String version) {
            this.version = version;
            return this;
        }

        public Builder invocation(final @Nullable String invocation) {
            this.invocation = invocation;
            return this;
        }

        @Override
        public XvrlCreator build() {
            return new XvrlCreator(this);
        }
    }
}
