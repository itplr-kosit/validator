package org.kosit.xvrl.model;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.kosit.base.annotation.ReturnsImmutableObject;

/**
 * The XVRL {@code provenance} element - the origin of a detection, expressed as one or more locations.
 *
 * @author Philip Helger
 */
public final class XvrlProvenance {

    private final List<XvrlLocation> locations;

    private XvrlProvenance(final Builder builder) {
        this.locations = List.copyOf(builder.locations);
    }

    /**
     * @return all locations of this provenance. Never <code>null</code> but maybe empty.
     */
    @ReturnsImmutableObject
    public List<XvrlLocation> getLocations() {
        return this.locations;
    }

    /**
     * @return the first location or <code>null</code> if none is present.
     */
    public @Nullable XvrlLocation getFirstLocation() {
        return this.locations.isEmpty() ? null : this.locations.getFirst();
    }

    @Override
    public String toString() {
        return "XvrlProvenance" + this.locations;
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
     * Builder for {@link XvrlProvenance}.
     */
    public static final class Builder {

        private final List<XvrlLocation> locations = new ArrayList<>();

        private Builder() {
        }

        private Builder(final XvrlProvenance src) {
            this.locations.addAll(src.locations);
        }

        public Builder addLocation(final @Nullable XvrlLocation location) {
            if (location != null)
                this.locations.add(location);
            return this;
        }

        public Builder addLocation(final XvrlLocation.@Nullable Builder location) {
            return addLocation(location == null ? null : location.build());
        }

        public Builder addLocations(final @Nullable Iterable<? extends XvrlLocation> locations) {
            if (locations != null)
                for (final XvrlLocation location : locations)
                    addLocation(location);
            return this;
        }

        public Builder removeAllLocations() {
            this.locations.clear();
            return this;
        }

        /**
         * @return the immutable object created from the current builder state. Never <code>null</code>.
         */
        public XvrlProvenance build() {
            return new XvrlProvenance(this);
        }
    }
}
