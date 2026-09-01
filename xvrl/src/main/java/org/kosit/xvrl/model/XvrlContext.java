package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;

/**
 * The XVRL {@code context} element - the context in which a detection occurred. It has an optional location, mixed
 * content and the {@code common.attr} attributes.
 *
 * @author Philip Helger
 */
public final class XvrlContext extends AbstractXvrlContentObject {

    private final @Nullable XvrlLocation location;

    private XvrlContext(final Builder builder) {
        super(builder);
        this.location = builder.location;
    }

    /**
     * @return the location of the context. May be <code>null</code>.
     */
    public @Nullable XvrlLocation getLocation() {
        return this.location;
    }

    @Override
    public String toString() {
        return "XvrlContext[location=" + this.location + ", " + getContentText() + "]";
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
     * Builder for {@link XvrlContext}.
     */
    public static final class Builder extends AbstractContentBuilder<XvrlContext, Builder> {

        private @Nullable XvrlLocation location;

        private Builder() {
        }

        private Builder(final XvrlContext src) {
            super(src);
            this.location = src.location;
        }

        public Builder location(final @Nullable XvrlLocation location) {
            this.location = location;
            return this;
        }

        public Builder location(final XvrlLocation.@Nullable Builder location) {
            return location(location == null ? null : location.build());
        }

        @Override
        public XvrlContext build() {
            return new XvrlContext(this);
        }
    }
}
