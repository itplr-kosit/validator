package org.kosit.xvrl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;

/**
 * The XVRL {@code report} element - the result of a single validation step, consisting of metadata, any number of
 * detections and an aggregating digest.
 *
 * @author Philip Helger
 */
public final class XvrlReport extends AbstractXvrlCommonObject implements IXvrlReportsItem {

    private final @Nullable XvrlMetadata metadata;

    private final List<XvrlDetection> detections;

    private final @Nullable XvrlDigest digest;

    private XvrlReport(final Builder builder) {
        super(builder);
        this.metadata = builder.metadata;
        this.detections = List.copyOf(builder.detections);
        this.digest = builder.digest;
    }

    /**
     * @return the metadata of this report. May be <code>null</code>.
     */
    public @Nullable XvrlMetadata getMetadata() {
        return this.metadata;
    }

    /**
     * @return all detections of this report. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlDetection> getDetections() {
        return this.detections;
    }

    /**
     * @return the digest of this report. May be <code>null</code>.
     */
    public @Nullable XvrlDigest getDigest() {
        return this.digest;
    }

    /**
     * @return the text of every message of every detection with an error severity. Never <code>null</code> but maybe
     *         empty.
     */
    @ReturnsImmutableObject
    public List<String> getAllErrors() {
        return this.detections.stream().filter(XvrlDetection::hasErrors).flatMap(detection -> detection.getAllMessageStrings().stream())
                .toList();
    }

    @Override
    public String toString() {
        return "id=" + getID() + ", errors=" + (this.digest == null ? null : this.digest.getErrorCount()) + ", valid="
                + (this.digest == null ? null : this.digest.getValid());
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
     * Builder for {@link XvrlReport}.
     */
    public static final class Builder extends AbstractCommonBuilder<XvrlReport, Builder> {

        private @Nullable XvrlMetadata metadata;

        private final List<XvrlDetection> detections = new ArrayList<>();

        private @Nullable XvrlDigest digest;

        private Builder() {
        }

        private Builder(final XvrlReport src) {
            super(src);
            this.metadata = src.metadata;
            this.detections.addAll(src.detections);
            this.digest = src.digest;
        }

        public Builder metadata(final @Nullable XvrlMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder metadata(final XvrlMetadata.@Nullable Builder metadata) {
            return metadata(metadata == null ? null : metadata.build());
        }

        /**
         * @return the metadata set so far. May be <code>null</code>.
         */
        public @Nullable XvrlMetadata getMetadata() {
            return this.metadata;
        }

        public Builder addDetection(final @Nullable XvrlDetection detection) {
            if (detection != null)
                this.detections.add(detection);
            return this;
        }

        public Builder addDetection(final XvrlDetection.@Nullable Builder detection) {
            return addDetection(detection == null ? null : detection.build());
        }

        public Builder addDetections(final @Nullable Iterable<? extends XvrlDetection> detections) {
            if (detections != null)
                for (final XvrlDetection detection : detections)
                    addDetection(detection);
            return this;
        }

        public Builder removeDetectionsIf(final Predicate<? super XvrlDetection> filter) {
            this.detections.removeIf(filter);
            return this;
        }

        /**
         * @return all detections added so far. The returned list is the live builder state. Never <code>null</code>.
         */
        public List<XvrlDetection> getDetections() {
            return this.detections;
        }

        public Builder digest(final @Nullable XvrlDigest digest) {
            this.digest = digest;
            return this;
        }

        public Builder digest(final XvrlDigest.@Nullable Builder digest) {
            return digest(digest == null ? null : digest.build());
        }

        /**
         * @return the digest set so far. May be <code>null</code>.
         */
        public @Nullable XvrlDigest getDigest() {
            return this.digest;
        }

        @Override
        public XvrlReport build() {
            return new XvrlReport(this);
        }
    }
}
