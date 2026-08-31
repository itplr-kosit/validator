package org.kosit.xvrl.model;

/**
 * The XVRL {@code summary} element - a human readable summary. It has mixed content and the {@code common.attr}
 * attributes.
 *
 * @author Philip Helger
 */
public final class XvrlSummary extends AbstractXvrlContentObject {

    private XvrlSummary(final Builder builder) {
        super(builder);
    }

    @Override
    public String toString() {
        return "XvrlSummary[" + getContentText() + "]";
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
     * Builder for {@link XvrlSummary}.
     */
    public static final class Builder extends AbstractContentBuilder<XvrlSummary, Builder> {

        private Builder() {
        }

        private Builder(final XvrlSummary src) {
            super(src);
        }

        @Override
        public XvrlSummary build() {
            return new XvrlSummary(this);
        }
    }
}
