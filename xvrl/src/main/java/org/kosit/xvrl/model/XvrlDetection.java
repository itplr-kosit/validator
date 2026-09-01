package org.kosit.xvrl.model;

import java.util.ArrayList;
import java.util.List;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.base.error.SimpleError;

/**
 * The XVRL {@code detection} element - a single finding of a validation, hence the central payload of an
 * {@link XvrlReport}.
 *
 * @author Philip Helger
 */
public final class XvrlDetection extends AbstractXvrlCommonObject {

    private final List<XvrlLocation> locations;

    private final List<XvrlProvenance> provenances;

    private final List<XvrlTitle> titles;

    private final List<XvrlSummary> summaries;

    private final List<XvrlCategory> categories;

    private final List<XvrlLet> lets;

    private final List<XvrlMessage> messages;

    private final List<XvrlSupplemental> supplementals;

    private final List<XvrlContext> contexts;

    private final @Nullable XvrlSeverity severity;

    private final @Nullable String code;

    public static @NonNull XvrlSeverity translate(final @Nullable CTStandardSeverity severity) {
        if (severity == null)
            return XvrlSeverity.UNSPECIFIED;

        return switch (severity) {
            case NONE -> XvrlSeverity.UNSPECIFIED;
            case WARNING -> XvrlSeverity.WARNING;
            case ERROR -> XvrlSeverity.ERROR;
        };
    }

    private XvrlDetection(final Builder builder) {
        super(builder);
        this.locations = List.copyOf(builder.locations);
        this.provenances = List.copyOf(builder.provenances);
        this.titles = List.copyOf(builder.titles);
        this.summaries = List.copyOf(builder.summaries);
        this.categories = List.copyOf(builder.categories);
        this.lets = List.copyOf(builder.lets);
        this.messages = List.copyOf(builder.messages);
        this.supplementals = List.copyOf(builder.supplementals);
        this.contexts = List.copyOf(builder.contexts);
        this.severity = builder.severity;
        this.code = builder.code;
    }

    /**
     * @return all locations of this detection. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlLocation> getLocations() {
        return this.locations;
    }

    /**
     * @return all provenances of this detection. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlProvenance> getProvenances() {
        return this.provenances;
    }

    /**
     * @return all titles of this detection. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlTitle> getTitles() {
        return this.titles;
    }

    /**
     * @return all summaries of this detection. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlSummary> getSummaries() {
        return this.summaries;
    }

    /**
     * @return all categories of this detection. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlCategory> getCategories() {
        return this.categories;
    }

    /**
     * @return all variable bindings of this detection. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlLet> getLets() {
        return this.lets;
    }

    /**
     * @return all messages of this detection. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlMessage> getMessages() {
        return this.messages;
    }

    /**
     * @return all supplementals of this detection. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlSupplemental> getSupplementals() {
        return this.supplementals;
    }

    /**
     * @return all contexts of this detection. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlContext> getContexts() {
        return this.contexts;
    }

    /**
     * @return the severity of this detection. May be <code>null</code>.
     */
    public @Nullable XvrlSeverity getSeverity() {
        return this.severity;
    }

    /**
     * @return the machine readable code of this detection. May be <code>null</code>.
     */
    public @Nullable String getCode() {
        return this.code;
    }

    /**
     * @return the text of every message of this detection. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<String> getAllMessageStrings() {
        return this.messages.stream().flatMap(message -> message.getContentStrings().stream()).toList();
    }

    /**
     * @return the concatenated text of the first message or <code>null</code> if this detection has no message.
     */
    public @Nullable String getErrorMessage() {
        return this.messages.isEmpty() ? null : this.messages.getFirst().getContentText();
    }

    /**
     * @return the first location of this detection or <code>null</code> if it has none.
     */
    public @Nullable XvrlLocation getErrorLocation() {
        return this.locations.isEmpty() ? null : this.locations.getFirst();
    }

    /**
     * @return <code>true</code> if the severity of this detection is an error or a fatal error.
     */
    public boolean hasErrors() {
        return this.severity != null && this.severity.isError();
    }

    @Override
    public String toString() {
        return "XvrlDetection[severity=" + this.severity + ", code=" + this.code + ", message=" + getErrorMessage() + "]";
    }

    /**
     * @return a new empty builder. Never <code>null</code>.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static Builder builderError() {
        return builder().severity(CTStandardSeverity.ERROR);
    }

    public static Builder builderWarning() {
        return builder().severity(CTStandardSeverity.WARNING);
    }

    public static Builder builderNone() {
        return builder().severity(CTStandardSeverity.NONE);
    }

    /**
     * @return a new builder prefilled with the state of this object. Never <code>null</code>.
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder for {@link XvrlDetection}.
     */
    public static final class Builder extends AbstractCommonBuilder<XvrlDetection, Builder> {

        private final List<XvrlLocation> locations = new ArrayList<>();

        private final List<XvrlProvenance> provenances = new ArrayList<>();

        private final List<XvrlTitle> titles = new ArrayList<>();

        private final List<XvrlSummary> summaries = new ArrayList<>();

        private final List<XvrlCategory> categories = new ArrayList<>();

        private final List<XvrlLet> lets = new ArrayList<>();

        private final List<XvrlMessage> messages = new ArrayList<>();

        private final List<XvrlSupplemental> supplementals = new ArrayList<>();

        private final List<XvrlContext> contexts = new ArrayList<>();

        private @Nullable XvrlSeverity severity;

        private @Nullable String code;

        private Builder() {
        }

        private Builder(final XvrlDetection src) {
            super(src);
            this.locations.addAll(src.locations);
            this.provenances.addAll(src.provenances);
            this.titles.addAll(src.titles);
            this.summaries.addAll(src.summaries);
            this.categories.addAll(src.categories);
            this.lets.addAll(src.lets);
            this.messages.addAll(src.messages);
            this.supplementals.addAll(src.supplementals);
            this.contexts.addAll(src.contexts);
            this.severity = src.severity;
            this.code = src.code;
        }

        public Builder error(final @Nullable SimpleError error) {
            if (error != null) {
                addMessage(XvrlMessage.builder(error.getMessage()));
                severity(error.getSeverity());
                if (error.hasLineOrColumnNumber()) {
                    addLocation(XvrlLocation.builder().location(error));
                }
            }
            return this;
        }

        public Builder addLocation(final @Nullable XvrlLocation location) {
            if (location != null)
                this.locations.add(location);
            return this;
        }

        public Builder addLocation(final XvrlLocation.@Nullable Builder location) {
            return addLocation(location == null ? null : location.build());
        }

        public Builder addProvenance(final @Nullable XvrlProvenance provenance) {
            if (provenance != null)
                this.provenances.add(provenance);
            return this;
        }

        public Builder addProvenance(final XvrlProvenance.@Nullable Builder provenance) {
            return addProvenance(provenance == null ? null : provenance.build());
        }

        public Builder addTitle(final @Nullable XvrlTitle title) {
            if (title != null)
                this.titles.add(title);
            return this;
        }

        public Builder addTitle(final XvrlTitle.@Nullable Builder title) {
            return addTitle(title == null ? null : title.build());
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

        public Builder addLet(final @Nullable XvrlLet let) {
            if (let != null)
                this.lets.add(let);
            return this;
        }

        public Builder addLet(final XvrlLet.@Nullable Builder let) {
            return addLet(let == null ? null : let.build());
        }

        public Builder addMessage(final @Nullable XvrlMessage message) {
            if (message != null)
                this.messages.add(message);
            return this;
        }

        public Builder addMessage(final XvrlMessage.@Nullable Builder message) {
            return addMessage(message == null ? null : message.build());
        }

        public Builder addMessage(final @Nullable String message) {
            return addMessage(message == null ? null : XvrlMessage.builder(message));
        }

        public Builder addSupplemental(final @Nullable XvrlSupplemental supplemental) {
            if (supplemental != null)
                this.supplementals.add(supplemental);
            return this;
        }

        public Builder addSupplemental(final XvrlSupplemental.@Nullable Builder supplemental) {
            return addSupplemental(supplemental == null ? null : supplemental.build());
        }

        public Builder addContext(final @Nullable XvrlContext context) {
            if (context != null)
                this.contexts.add(context);
            return this;
        }

        public Builder addContext(final XvrlContext.@Nullable Builder context) {
            return addContext(context == null ? null : context.build());
        }

        public Builder severity(final @Nullable CTStandardSeverity severity) {
            return severity(translate(severity));
        }

        public Builder severity(final @Nullable XvrlSeverity severity) {
            this.severity = severity;
            return this;
        }

        /**
         * @return the severity set so far. May be <code>null</code>.
         */
        public @Nullable XvrlSeverity getSeverity() {
            return this.severity;
        }

        public Builder code(final @Nullable String code) {
            this.code = code;
            return this;
        }

        @Override
        public XvrlDetection build() {
            return new XvrlDetection(this);
        }
    }
}
