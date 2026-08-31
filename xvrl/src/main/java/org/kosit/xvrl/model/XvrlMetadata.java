package org.kosit.xvrl.model;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;

/**
 * The XVRL {@code metadata} element - the descriptive information of an {@link XvrlReport} or of an
 * {@link XvrlReports}.
 *
 * @author Philip Helger
 */
public final class XvrlMetadata extends AbstractXvrlCommonObject {

    private final List<XvrlTimestamp> timestamps;

    private final List<XvrlValidator> validators;

    private final List<XvrlCreator> creators;

    private final List<XvrlDocument> documents;

    private final List<XvrlTitle> titles;

    private final List<XvrlSummary> summaries;

    private final List<XvrlCategory> categories;

    private final List<XvrlSchema> schemas;

    private final List<XvrlSupplemental> supplementals;

    private XvrlMetadata(final Builder builder) {
        super(builder);
        this.timestamps = List.copyOf(builder.timestamps);
        this.validators = List.copyOf(builder.validators);
        this.creators = List.copyOf(builder.creators);
        this.documents = List.copyOf(builder.documents);
        this.titles = List.copyOf(builder.titles);
        this.summaries = List.copyOf(builder.summaries);
        this.categories = List.copyOf(builder.categories);
        this.schemas = List.copyOf(builder.schemas);
        this.supplementals = List.copyOf(builder.supplementals);
    }

    /**
     * @return all timestamps. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlTimestamp> getTimestamps() {
        return this.timestamps;
    }

    /**
     * @return all validators. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlValidator> getValidators() {
        return this.validators;
    }

    /**
     * @return the first validator or <code>null</code> if none is present.
     */
    public @Nullable XvrlValidator getFirstValidator() {
        return this.validators.isEmpty() ? null : this.validators.getFirst();
    }

    /**
     * @return all creators. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlCreator> getCreators() {
        return this.creators;
    }

    /**
     * @return all documents. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlDocument> getDocuments() {
        return this.documents;
    }

    /**
     * @return the first document or <code>null</code> if none is present.
     */
    public @Nullable XvrlDocument getFirstDocument() {
        return this.documents.isEmpty() ? null : this.documents.getFirst();
    }

    /**
     * @return all titles. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlTitle> getTitles() {
        return this.titles;
    }

    /**
     * @return all summaries. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlSummary> getSummaries() {
        return this.summaries;
    }

    /**
     * @return all categories. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlCategory> getCategories() {
        return this.categories;
    }

    /**
     * @return all schemas. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlSchema> getSchemas() {
        return this.schemas;
    }

    /**
     * @return all supplementals. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlSupplemental> getSupplementals() {
        return this.supplementals;
    }

    @Override
    public String toString() {
        return "XvrlMetadata[validators=" + this.validators + ", documents=" + this.documents + "]";
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
     * Builder for {@link XvrlMetadata}.
     */
    public static final class Builder extends AbstractCommonBuilder<XvrlMetadata, Builder> {

        private final List<XvrlTimestamp> timestamps = new ArrayList<>();

        private final List<XvrlValidator> validators = new ArrayList<>();

        private final List<XvrlCreator> creators = new ArrayList<>();

        private final List<XvrlDocument> documents = new ArrayList<>();

        private final List<XvrlTitle> titles = new ArrayList<>();

        private final List<XvrlSummary> summaries = new ArrayList<>();

        private final List<XvrlCategory> categories = new ArrayList<>();

        private final List<XvrlSchema> schemas = new ArrayList<>();

        private final List<XvrlSupplemental> supplementals = new ArrayList<>();

        private Builder() {
        }

        private Builder(final XvrlMetadata src) {
            super(src);
            this.timestamps.addAll(src.timestamps);
            this.validators.addAll(src.validators);
            this.creators.addAll(src.creators);
            this.documents.addAll(src.documents);
            this.titles.addAll(src.titles);
            this.summaries.addAll(src.summaries);
            this.categories.addAll(src.categories);
            this.schemas.addAll(src.schemas);
            this.supplementals.addAll(src.supplementals);
        }

        public Builder addTimestamp(final @Nullable XvrlTimestamp timestamp) {
            if (timestamp != null)
                this.timestamps.add(timestamp);
            return this;
        }

        public Builder addTimestamp(final XvrlTimestamp.@Nullable Builder timestamp) {
            return addTimestamp(timestamp == null ? null : timestamp.build());
        }

        public Builder addValidator(final @Nullable XvrlValidator validator) {
            if (validator != null)
                this.validators.add(validator);
            return this;
        }

        public Builder addValidator(final XvrlValidator.@Nullable Builder validator) {
            return addValidator(validator == null ? null : validator.build());
        }

        /**
         * Replaces all validators with the provided one.
         *
         * @param validator the only validator to keep. May be <code>null</code> to remove all of them.
         * @return this for chaining
         */
        public Builder validator(final @Nullable XvrlValidator validator) {
            this.validators.clear();
            return addValidator(validator);
        }

        /**
         * Replaces all validators with the provided one.
         *
         * @param validator the only validator to keep. May be <code>null</code> to remove all of them.
         * @return this for chaining
         */
        public Builder validator(final XvrlValidator.@Nullable Builder validator) {
            return validator(validator == null ? null : validator.build());
        }

        public Builder validator(final @Nullable String validator) {
            return validator(validator == null ? null : XvrlValidator.builder(validator));
        }

        public Builder addCreator(final @Nullable XvrlCreator creator) {
            if (creator != null)
                this.creators.add(creator);
            return this;
        }

        public Builder addCreator(final XvrlCreator.@Nullable Builder creator) {
            return addCreator(creator == null ? null : creator.build());
        }

        /**
         * Replaces all creators with the provided one.
         *
         * @param creator the only creator to keep. May be <code>null</code> to remove all of them.
         * @return this for chaining
         */
        public Builder creator(final @Nullable XvrlCreator creator) {
            this.creators.clear();
            return addCreator(creator);
        }

        /**
         * Replaces all creators with the provided one.
         *
         * @param creator the only creator to keep. May be <code>null</code> to remove all of them.
         * @return this for chaining
         */
        public Builder creator(final XvrlCreator.@Nullable Builder creator) {
            return creator(creator == null ? null : creator.build());
        }

        public Builder addDocument(final @Nullable XvrlDocument document) {
            if (document != null)
                this.documents.add(document);
            return this;
        }

        public Builder addDocument(final XvrlDocument.@Nullable Builder document) {
            return addDocument(document == null ? null : document.build());
        }

        public Builder addDocuments(final @Nullable Iterable<? extends XvrlDocument> documents) {
            if (documents != null)
                for (final XvrlDocument document : documents)
                    addDocument(document);
            return this;
        }

        public Builder removeAllDocuments() {
            this.documents.clear();
            return this;
        }

        /**
         * Replaces all documents with the provided one.
         *
         * @param document the only document to keep. May be <code>null</code> to remove all of them.
         * @return this for chaining
         */
        public Builder document(final @Nullable XvrlDocument document) {
            this.documents.clear();
            return addDocument(document);
        }

        /**
         * Replaces all documents with the provided one.
         *
         * @param document the only document to keep. May be <code>null</code> to remove all of them.
         * @return this for chaining
         */
        public Builder document(final XvrlDocument.@Nullable Builder document) {
            return document(document == null ? null : document.build());
        }

        public Builder addTitle(final @Nullable XvrlTitle title) {
            if (title != null)
                this.titles.add(title);
            return this;
        }

        public Builder addTitle(final XvrlTitle.@Nullable Builder title) {
            return addTitle(title == null ? null : title.build());
        }

        public Builder addTitle(final String title) {
            return addTitle(title == null ? null : XvrlTitle.builder(title));
        }

        public Builder addSummary(final @Nullable XvrlSummary summary) {
            if (summary != null)
                this.summaries.add(summary);
            return this;
        }

        public Builder addSummary(final XvrlSummary.@Nullable Builder summary) {
            return addSummary(summary == null ? null : summary.build());
        }

        public Builder addCategory(final @Nullable XvrlCategory category) {
            if (category != null)
                this.categories.add(category);
            return this;
        }

        public Builder addCategory(final XvrlCategory.@Nullable Builder category) {
            return addCategory(category == null ? null : category.build());
        }

        public Builder addSchema(final @Nullable XvrlSchema schema) {
            if (schema != null)
                this.schemas.add(schema);
            return this;
        }

        public Builder addSchema(final XvrlSchema.@Nullable Builder schema) {
            return addSchema(schema == null ? null : schema.build());
        }

        public Builder addSupplemental(final @Nullable XvrlSupplemental supplemental) {
            if (supplemental != null)
                this.supplementals.add(supplemental);
            return this;
        }

        public Builder addSupplemental(final XvrlSupplemental.@Nullable Builder supplemental) {
            return addSupplemental(supplemental == null ? null : supplemental.build());
        }

        @Override
        public XvrlMetadata build() {
            return new XvrlMetadata(this);
        }
    }
}
