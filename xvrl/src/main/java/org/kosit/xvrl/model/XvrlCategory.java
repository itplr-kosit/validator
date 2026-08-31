package org.kosit.xvrl.model;

import org.jspecify.annotations.Nullable;

/**
 * The XVRL {@code category} element - a classification of a detection or of a report. It has mixed content and the
 * {@code common.attr} attributes.
 *
 * @author Philip Helger
 */
public final class XvrlCategory extends AbstractXvrlContentObject {

    private final @Nullable String vocabulary;

    private XvrlCategory(final Builder builder) {
        super(builder);
        this.vocabulary = builder.vocabulary;
    }

    /**
     * @return the vocabulary this category is taken from. May be <code>null</code>.
     */
    public @Nullable String getVocabulary() {
        return this.vocabulary;
    }

    @Override
    public String toString() {
        return "XvrlCategory[vocabulary=" + this.vocabulary + ", " + getContentText() + "]";
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
     * Builder for {@link XvrlCategory}.
     */
    public static final class Builder extends AbstractContentBuilder<XvrlCategory, Builder> {

        private @Nullable String vocabulary;

        private Builder() {
        }

        private Builder(final XvrlCategory src) {
            super(src);
            this.vocabulary = src.vocabulary;
        }

        public Builder vocabulary(final @Nullable String vocabulary) {
            this.vocabulary = vocabulary;
            return this;
        }

        @Override
        public XvrlCategory build() {
            return new XvrlCategory(this);
        }
    }
}
