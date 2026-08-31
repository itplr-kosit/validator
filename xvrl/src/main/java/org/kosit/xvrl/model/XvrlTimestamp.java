package org.kosit.xvrl.model;

import java.time.OffsetDateTime;

import org.jspecify.annotations.Nullable;

/**
 * The XVRL {@code timestamp} element - the point in time at which a report was created.
 *
 * @author Philip Helger
 */
public final class XvrlTimestamp extends AbstractXvrlCommonObject {

    private final @Nullable OffsetDateTime value;

    private XvrlTimestamp(final Builder builder) {
        super(builder);
        this.value = builder.value;
    }

    /**
     * @return the point in time. May be <code>null</code>.
     */
    public @Nullable OffsetDateTime getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "XvrlTimestamp[" + this.value + "]";
    }

    /**
     * @return a new empty builder. Never <code>null</code>.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a prefilled builder for the provided point in time.
     *
     * @param value the point in time. May be <code>null</code>.
     * @return a new builder. Never <code>null</code>.
     */
    public static Builder builder(final @Nullable OffsetDateTime value) {
        return new Builder().value(value);
    }

    /**
     * Creates a prefilled builder holding the current point in time.
     *
     * @return a new builder. Never <code>null</code>.
     */
    public static Builder builderNow() {
        return builder(OffsetDateTime.now());
    }

    /**
     * @return a new builder prefilled with the state of this object. Never <code>null</code>.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for {@link XvrlTimestamp}.
     */
    public static final class Builder extends AbstractCommonBuilder<XvrlTimestamp, Builder> {

        private @Nullable OffsetDateTime value;

        private Builder() {
        }

        private Builder(final XvrlTimestamp src) {
            super(src);
            this.value = src.value;
        }

        public Builder value(final @Nullable OffsetDateTime value) {
            this.value = value;
            return this;
        }

        @Override
        public XvrlTimestamp build() {
            return new XvrlTimestamp(this);
        }
    }
}
