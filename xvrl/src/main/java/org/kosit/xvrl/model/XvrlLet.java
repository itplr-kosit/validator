package org.kosit.xvrl.model;

import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;

/**
 * The XVRL {@code let} element - a named variable binding contributing to a detection. It has mixed content and the
 * {@code common.attr} attributes.
 *
 * @author Philip Helger
 */
public final class XvrlLet extends AbstractXvrlContentObject {

    private final @Nullable QName name;

    private final @Nullable String value;

    private XvrlLet(final Builder builder) {
        super(builder);
        this.name = builder.name;
        this.value = builder.value;
    }

    /**
     * @return the qualified name of the variable. May be <code>null</code>.
     */
    public @Nullable QName getName() {
        return this.name;
    }

    /**
     * @return the value of the variable. May be <code>null</code>.
     */
    public @Nullable String getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "XvrlLet[name=" + this.name + ", value=" + this.value + "]";
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
     * Builder for {@link XvrlLet}.
     */
    public static final class Builder extends AbstractContentBuilder<XvrlLet, Builder> {

        private @Nullable QName name;

        private @Nullable String value;

        private Builder() {
        }

        private Builder(final XvrlLet src) {
            super(src);
            this.name = src.name;
            this.value = src.value;
        }

        public Builder name(final @Nullable QName name) {
            this.name = name;
            return this;
        }

        public Builder value(final @Nullable String value) {
            this.value = value;
            return this;
        }

        @Override
        public XvrlLet build() {
            return new XvrlLet(this);
        }
    }
}
