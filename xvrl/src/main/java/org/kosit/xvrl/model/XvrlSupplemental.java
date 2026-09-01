package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;

/**
 * The XVRL {@code supplemental} element - additional, implementation defined information. It has mixed content and the
 * {@code common.attr} attributes.
 *
 * @author Philip Helger
 */
public final class XvrlSupplemental extends AbstractXvrlContentObject {

    private final @Nullable String role;

    private XvrlSupplemental(final Builder builder) {
        super(builder);
        this.role = builder.role;
    }

    /**
     * @return the role of this supplemental. May be <code>null</code>.
     */
    public @Nullable String getRole() {
        return this.role;
    }

    @Override
    public String toString() {
        return "XvrlSupplemental[role=" + this.role + ", " + getContentText() + "]";
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
     * @param text the text content. May be <code>null</code>.
     * @return a new builder. Never <code>null</code>.
     */
    public static Builder builder(final String text) {
        return new Builder().addContent(text);
    }

    /**
     * @return a new builder prefilled with the state of this object. Never <code>null</code>.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for {@link XvrlSupplemental}.
     */
    public static final class Builder extends AbstractContentBuilder<XvrlSupplemental, Builder> {

        private @Nullable String role;

        private Builder() {
        }

        private Builder(final XvrlSupplemental src) {
            super(src);
            this.role = src.role;
        }

        public Builder role(final @Nullable String role) {
            this.role = role;
            return this;
        }

        @Override
        public XvrlSupplemental build() {
            return new XvrlSupplemental(this);
        }
    }
}
