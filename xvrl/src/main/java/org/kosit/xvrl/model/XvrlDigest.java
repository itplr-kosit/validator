package org.kosit.xvrl.model;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;

/**
 * The XVRL {@code digest} element - the aggregated outcome of a report.
 *
 * @author Philip Helger
 */
public final class XvrlDigest extends AbstractXvrlCommonObject implements IXvrlReportsItem {

    private final @Nullable XvrlValidity valid;

    private final @Nullable Long fatalErrorCount;

    private final @Nullable Long errorCount;

    private final @Nullable Long warningCount;

    private final @Nullable Long infoCount;

    private final @Nullable Long unspecifiedCount;

    private final List<String> fatalErrorCodes;

    private final List<String> errorCodes;

    private final List<String> warningCodes;

    private final List<String> infoCodes;

    private final List<String> unspecifiedCodes;

    private final @Nullable XvrlWorst worst;

    private XvrlDigest(final Builder builder) {
        super(builder);
        this.valid = builder.valid;
        this.fatalErrorCount = builder.fatalErrorCount;
        this.errorCount = builder.errorCount;
        this.warningCount = builder.warningCount;
        this.infoCount = builder.infoCount;
        this.unspecifiedCount = builder.unspecifiedCount;
        this.fatalErrorCodes = List.copyOf(builder.fatalErrorCodes);
        this.errorCodes = List.copyOf(builder.errorCodes);
        this.warningCodes = List.copyOf(builder.warningCodes);
        this.infoCodes = List.copyOf(builder.infoCodes);
        this.unspecifiedCodes = List.copyOf(builder.unspecifiedCodes);
        this.worst = builder.worst;
    }

    /**
     * @return the overall validity. May be <code>null</code>.
     */
    public @Nullable XvrlValidity getValid() {
        return this.valid;
    }

    /**
     * @return the number of fatal errors. May be <code>null</code>.
     */
    public @Nullable Long getFatalErrorCount() {
        return this.fatalErrorCount;
    }

    /**
     * @return the number of errors. May be <code>null</code>.
     */
    public @Nullable Long getErrorCount() {
        return this.errorCount;
    }

    /**
     * @return the number of warnings. May be <code>null</code>.
     */
    public @Nullable Long getWarningCount() {
        return this.warningCount;
    }

    /**
     * @return the number of infos. May be <code>null</code>.
     */
    public @Nullable Long getInfoCount() {
        return this.infoCount;
    }

    /**
     * @return the number of detections with an unspecified severity. May be <code>null</code>.
     */
    public @Nullable Long getUnspecifiedCount() {
        return this.unspecifiedCount;
    }

    /**
     * @return the codes of all fatal errors. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<String> getFatalErrorCodes() {
        return this.fatalErrorCodes;
    }

    /**
     * @return the codes of all errors. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<String> getErrorCodes() {
        return this.errorCodes;
    }

    /**
     * @return the codes of all warnings. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<String> getWarningCodes() {
        return this.warningCodes;
    }

    /**
     * @return the codes of all infos. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<String> getInfoCodes() {
        return this.infoCodes;
    }

    /**
     * @return the codes of all detections with an unspecified severity. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<String> getUnspecifiedCodes() {
        return this.unspecifiedCodes;
    }

    /**
     * @return the worst severity contained in the report. May be <code>null</code>.
     */
    public @Nullable XvrlWorst getWorst() {
        return this.worst;
    }

    @Override
    public String toString() {
        return "XvrlDigest[valid=" + this.valid + ", errors=" + this.errorCount + ", warnings=" + this.warningCount + "]";
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
     * Builder for {@link XvrlDigest}.
     */
    public static final class Builder extends AbstractCommonBuilder<XvrlDigest, Builder> {

        private @Nullable XvrlValidity valid;

        private @Nullable Long fatalErrorCount;

        private @Nullable Long errorCount;

        private @Nullable Long warningCount;

        private @Nullable Long infoCount;

        private @Nullable Long unspecifiedCount;

        private final List<String> fatalErrorCodes = new ArrayList<>();

        private final List<String> errorCodes = new ArrayList<>();

        private final List<String> warningCodes = new ArrayList<>();

        private final List<String> infoCodes = new ArrayList<>();

        private final List<String> unspecifiedCodes = new ArrayList<>();

        private @Nullable XvrlWorst worst;

        private Builder() {
        }

        private Builder(final XvrlDigest src) {
            super(src);
            this.valid = src.valid;
            this.fatalErrorCount = src.fatalErrorCount;
            this.errorCount = src.errorCount;
            this.warningCount = src.warningCount;
            this.infoCount = src.infoCount;
            this.unspecifiedCount = src.unspecifiedCount;
            this.fatalErrorCodes.addAll(src.fatalErrorCodes);
            this.errorCodes.addAll(src.errorCodes);
            this.warningCodes.addAll(src.warningCodes);
            this.infoCodes.addAll(src.infoCodes);
            this.unspecifiedCodes.addAll(src.unspecifiedCodes);
            this.worst = src.worst;
        }

        private static void addAll(final List<String> target, final @Nullable Iterable<String> values) {
            if (values != null)
                for (final String value : values)
                    if (value != null)
                        target.add(value);
        }

        public Builder valid(final @Nullable XvrlValidity valid) {
            this.valid = valid;
            return this;
        }

        /**
         * @return the validity set so far. May be <code>null</code>.
         */
        public @Nullable XvrlValidity getValid() {
            return this.valid;
        }

        public Builder fatalErrorCount(final @Nullable Long fatalErrorCount) {
            this.fatalErrorCount = fatalErrorCount;
            return this;
        }

        public Builder errorCount(final @Nullable Long errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public Builder warningCount(final @Nullable Long warningCount) {
            this.warningCount = warningCount;
            return this;
        }

        public Builder infoCount(final @Nullable Long infoCount) {
            this.infoCount = infoCount;
            return this;
        }

        public Builder unspecifiedCount(final @Nullable Long unspecifiedCount) {
            this.unspecifiedCount = unspecifiedCount;
            return this;
        }

        /**
         * Sets the detection count for the provided severity.
         *
         * @param severity the severity to set the count for. May be <code>null</code> in which case nothing happens.
         * @param count the count to set. May be <code>null</code>.
         * @return this for chaining
         */
        public Builder count(final @Nullable XvrlSeverity severity, final @Nullable Long count) {
            if (severity != null)
                switch (severity) {
                    case FATAL_ERROR -> fatalErrorCount(count);
                    case ERROR -> errorCount(count);
                    case WARNING -> warningCount(count);
                    case INFO -> infoCount(count);
                    case UNSPECIFIED -> unspecifiedCount(count);
                }
            return this;
        }

        public Builder addFatalErrorCode(final @Nullable String code) {
            if (code != null)
                this.fatalErrorCodes.add(code);
            return this;
        }

        public Builder addFatalErrorCodes(final @Nullable Iterable<String> codes) {
            addAll(this.fatalErrorCodes, codes);
            return this;
        }

        public Builder addErrorCode(final @Nullable String code) {
            if (code != null)
                this.errorCodes.add(code);
            return this;
        }

        public Builder addErrorCodes(final @Nullable Iterable<String> codes) {
            addAll(this.errorCodes, codes);
            return this;
        }

        public Builder addWarningCode(final @Nullable String code) {
            if (code != null)
                this.warningCodes.add(code);
            return this;
        }

        public Builder addWarningCodes(final @Nullable Iterable<String> codes) {
            addAll(this.warningCodes, codes);
            return this;
        }

        public Builder addInfoCode(final @Nullable String code) {
            if (code != null)
                this.infoCodes.add(code);
            return this;
        }

        public Builder addInfoCodes(final @Nullable Iterable<String> codes) {
            addAll(this.infoCodes, codes);
            return this;
        }

        public Builder addUnspecifiedCode(final @Nullable String code) {
            if (code != null)
                this.unspecifiedCodes.add(code);
            return this;
        }

        public Builder addUnspecifiedCodes(final @Nullable Iterable<String> codes) {
            addAll(this.unspecifiedCodes, codes);
            return this;
        }

        public Builder worst(final @Nullable XvrlWorst worst) {
            this.worst = worst;
            return this;
        }

        @Override
        public XvrlDigest build() {
            return new XvrlDigest(this);
        }
    }
}
