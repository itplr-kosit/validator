package org.kosit.xvrl.model;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;

/**
 * The XVRL {@code reports} element - the root of an XVRL document, aggregating any number of {@link XvrlReport}, nested
 * {@link XvrlReports} and {@link XvrlDigest} objects.
 *
 * @author Philip Helger
 */
public final class XvrlReports extends AbstractXvrlCommonObject implements IXvrlReportsItem {

    private final @Nullable XvrlMetadata metadata;

    private final List<IXvrlReportsItem> items;

    private XvrlReports(final Builder builder) {
        super(builder);
        this.metadata = builder.metadata;
        this.items = List.copyOf(builder.items);
    }

    /**
     * @return the metadata of this report summary. May be <code>null</code>.
     */
    public @Nullable XvrlMetadata getMetadata() {
        return this.metadata;
    }

    /**
     * @return all contained items in document order. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<IXvrlReportsItem> getAllItems() {
        return this.items;
    }

    /**
     * @return all directly contained reports. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlReport> getReports() {
        return this.items.stream().filter(XvrlReport.class::isInstance).map(XvrlReport.class::cast).toList();
    }

    /**
     * @return all directly contained nested report summaries. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlReports> getReportSummaries() {
        return this.items.stream().filter(XvrlReports.class::isInstance).map(XvrlReports.class::cast).toList();
    }

    /**
     * @return all directly contained digests. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlDigest> getDigests() {
        return this.items.stream().filter(XvrlDigest.class::isInstance).map(XvrlDigest.class::cast).toList();
    }

    /**
     * @return the text of every message of every detection with an error severity, over all directly contained reports.
     *         Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<String> getAllErrors() {
        return getReports().stream().flatMap(report -> report.getAllErrors().stream()).toList();
    }

    @Override
    public String toString() {
        return "XvrlReports[items=" + this.items.size() + "]";
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
     * Builder for {@link XvrlReports}.
     */
    public static final class Builder extends AbstractCommonBuilder<XvrlReports, Builder> {

        private @Nullable XvrlMetadata metadata;

        private final List<IXvrlReportsItem> items = new ArrayList<>();

        private Builder() {
        }

        private Builder(final XvrlReports src) {
            super(src);
            this.metadata = src.metadata;
            this.items.addAll(src.items);
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

        public Builder addItem(final @Nullable IXvrlReportsItem item) {
            if (item != null)
                this.items.add(item);
            return this;
        }

        public Builder addItem(final @Nullable AbstractCommonBuilder<? extends IXvrlReportsItem, ?> item) {
            return addItem(item == null ? null : item.build());
        }

        public Builder addItems(final @Nullable Iterable<? extends IXvrlReportsItem> items) {
            if (items != null)
                for (final IXvrlReportsItem item : items)
                    addItem(item);
            return this;
        }

        public Builder addReport(final @Nullable XvrlReport report) {
            return addItem(report);
        }

        public Builder addReport(final XvrlReport.@Nullable Builder report) {
            return addItem(report == null ? null : report.build());
        }

        public Builder addReports(final @Nullable Iterable<? extends XvrlReport> reports) {
            return addItems(reports);
        }

        public Builder addReportSummary(final @Nullable XvrlReports reportSummary) {
            return addItem(reportSummary);
        }

        public Builder addReportSummary(final @Nullable Builder reportSummary) {
            return addItem(reportSummary == null ? null : reportSummary.build());
        }

        public Builder addDigest(final @Nullable XvrlDigest digest) {
            return addItem(digest);
        }

        public Builder addDigest(final XvrlDigest.@Nullable Builder digest) {
            return addItem(digest == null ? null : digest.build());
        }

        /**
         * @return all items added so far. The returned list is the live builder state. Never <code>null</code>.
         */
        public List<IXvrlReportsItem> getAllItems() {
            return this.items;
        }

        @Override
        public XvrlReports build() {
            return new XvrlReports(this);
        }
    }
}
