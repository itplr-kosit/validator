package org.kosit.validator.impl.conformatron.model;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.detection.CTDetectionLocation;
import org.conformatron.api.model.detection.CTDetectionText;
import org.conformatron.api.model.detection.CTSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A detection that is <i>about</i> an identified subject — a validation scenario, a validation artifact, a conformance
 * target — rather than about the document itself. Carries the three things a report consumer needs to act on such a
 * statement, none of which fit into a plain {@link CTDetection}:
 * <ul>
 * <li>the subject's <b>identity</b>, reported as a CVRL extension attribute ({@code cvrl:scenario-id},
 * {@code cvrl:artifact-id}, {@code cvrl:target-id}). Consumers that use the validator for more than plain validation
 * need the id on the element, not buried in a message text they would have to parse.</li>
 * <li>where the subject can be <b>looked up</b> ({@link #getSubjectLocation()}) — an {@code href} for something that
 * lives at a URI, an {@code xpath} for something inside a configuration file.</li>
 * <li>further <b>facts about the subject</b> ({@link #getAttributes()}) such as the artifact type or the conformance
 * verdict, again as attributes rather than prose.</li>
 * </ul>
 * Optionally the subject's own XML can travel along ({@link #getEmbeddedXml()}) — used for the selected scenario, so
 * the report shows exactly which rules were applied.
 * <p>
 * Everything else is delegated, so the detection stays a plain {@link CTDetection} for every consumer that does not
 * care about subjects.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class SubjectDetection implements CTDetection {

    /** How the subject can be located: at a URI, or at a place inside a document. */
    public enum LocationKind {

        HREF("href"), XPATH("xpath");

        private final String attributeName;

        LocationKind(final String attributeName) {
            this.attributeName = attributeName;
        }

        public String getAttributeName() {
            return this.attributeName;
        }
    }

    /** Extension attribute naming the scenario a detection is about. */
    public static final String ATTR_SCENARIO_ID = "scenario-id";

    /** Extension attribute naming the validation artifact a detection is about. */
    public static final String ATTR_ARTIFACT_ID = "artifact-id";

    /** Extension attribute naming the conformance target a detection is about. */
    public static final String ATTR_TARGET_ID = "target-id";

    /** Extension attribute carrying the kind of a validation artifact (xsd, schematron-xslt2, …). */
    public static final String ATTR_ARTIFACT_TYPE = "artifact-type";

    /** Extension attribute carrying a conformance verdict. */
    public static final String ATTR_CONFORMANCE = "conformance";

    private final CTDetection delegate;

    private final String subjectAttribute;

    private final String subjectId;

    private final LocationKind locationKind;

    private final String subjectLocation;

    private final Map<String, String> attributes;

    private final Map<String, String> locationAttributes;

    private final byte[] embeddedXml;

    private final String hashAlgorithm;

    private final String hashValue;

    private final String secondaryLocation;

    private SubjectDetection(final CTDetection delegate, final String subjectAttribute, final String subjectId,
            final LocationKind locationKind, final String subjectLocation, final String secondaryLocation,
            final Map<String, String> attributes, final Map<String, String> locationAttributes, final byte[] embeddedXml,
            final String hashAlgorithm, final String hashValue) {
        Objects.requireNonNull(delegate, "Delegate must not be null");
        this.delegate = delegate;
        this.subjectAttribute = subjectAttribute;
        this.subjectId = subjectId;
        this.locationKind = locationKind;
        this.subjectLocation = subjectLocation;
        this.secondaryLocation = secondaryLocation;
        this.attributes = Map.copyOf(attributes);
        this.locationAttributes = Map.copyOf(locationAttributes);
        this.embeddedXml = embeddedXml;
        this.hashAlgorithm = hashAlgorithm;
        this.hashValue = hashValue;
    }

    /**
     * Starts a subject detection for the given plain detection.
     *
     * @param delegate the plain detection. May not be <code>null</code>.
     * @return a builder
     */
    @NonNull
    public static Builder about(final @NonNull CTDetection delegate) {
        return new Builder(delegate);
    }

    /**
     * Fluent builder — a subject carries between one and four report attributes, so positional factories get unwieldy.
     */
    public static final class Builder {

        private final CTDetection delegate;

        private final Map<String, String> attributes = new LinkedHashMap<>();

        private final Map<String, String> locationAttributes = new LinkedHashMap<>();

        private String subjectAttribute;

        private String subjectId;

        private LocationKind locationKind;

        private String subjectLocation;

        private byte[] embeddedXml;

        private String hashAlgorithm;

        private String hashValue;

        private String secondaryLocation;

        private Builder(final CTDetection delegate) {
            this.delegate = delegate;
        }

        /**
         * @param attributeName the extension attribute naming the subject kind, e.g. {@link #ATTR_SCENARIO_ID}
         * @param id the subject's id. May be <code>null</code>, in which case nothing is reported.
         * @return this for chaining
         */
        @NonNull
        public Builder identifiedBy(final @NonNull String attributeName, final @Nullable String id) {
            this.subjectAttribute = attributeName;
            this.subjectId = id;
            return this;
        }

        /**
         * @param uri the URI the subject can be retrieved from. May be <code>null</code>.
         * @return this for chaining
         */
        @NonNull
        public Builder locatedAt(final @Nullable String uri) {
            this.locationKind = LocationKind.HREF;
            this.subjectLocation = uri;
            return this;
        }

        /**
         * @param xpath an XPath selecting the subject inside its configuration. May be <code>null</code>.
         * @return this for chaining
         */
        @NonNull
        public Builder locatedByXPath(final @Nullable String xpath) {
            this.locationKind = LocationKind.XPATH;
            this.subjectLocation = xpath;
            return this;
        }

        /**
         * @param name the extension attribute name, e.g. {@link #ATTR_ARTIFACT_TYPE}
         * @param value the value. Ignored when <code>null</code>.
         * @return this for chaining
         */
        @NonNull
        public Builder with(final @NonNull String name, final @Nullable String value) {
            if (value != null) {
                this.attributes.put(name, value);
            }
            return this;
        }

        /**
         * A fact about the place rather than about the detection — the kind of artifact found at a location, for
         * instance. Reported on the {@code location} element.
         *
         * @param name the extension attribute name, e.g. {@link #ATTR_ARTIFACT_TYPE}
         * @param value the value. Ignored when <code>null</code>.
         * @return this for chaining
         */
        @NonNull
        public Builder describingLocation(final @NonNull String name, final @Nullable String value) {
            if (value != null) {
                this.locationAttributes.put(name, value);
            }
            return this;
        }

        /**
         * @param xml the subject's own XML, to be embedded as evidence. May be <code>null</code>.
         * @return this for chaining
         */
        @NonNull
        public Builder embedding(final byte @Nullable [] xml) {
            this.embeddedXml = xml;
            return this;
        }

        /**
         * The file the subject lives in, reported next to the primary location. A scenario is located by an XPath
         * inside its configuration <i>and</i> by the file that configuration is; both are needed to look it up.
         *
         * @param uri the containing file. May be <code>null</code>.
         * @return this for chaining
         */
        @NonNull
        public Builder inFile(final @Nullable String uri) {
            this.secondaryLocation = uri;
            return this;
        }

        /**
         * The hash of the subject's bytes. Without it the report does not prove which version of an artifact the
         * validation actually ran against.
         *
         * @param algorithm the hash algorithm name. May be <code>null</code>.
         * @param value the hash as hex. May be <code>null</code>.
         * @return this for chaining
         */
        @NonNull
        public Builder hashed(final @Nullable String algorithm, final @Nullable String value) {
            this.hashAlgorithm = algorithm;
            this.hashValue = value;
            return this;
        }

        @NonNull
        public SubjectDetection build() {
            return new SubjectDetection(this.delegate, this.subjectAttribute, this.subjectId, this.locationKind, this.subjectLocation,
                    this.secondaryLocation, this.attributes, this.locationAttributes, this.embeddedXml, this.hashAlgorithm, this.hashValue);
        }
    }

    /** The extension attribute that names this subject kind, e.g. {@code scenario-id}. */
    public @Nullable String getSubjectAttribute() {
        return this.subjectAttribute;
    }

    /** The subject's id. May be <code>null</code>. */
    public @Nullable String getSubjectId() {
        return this.subjectId;
    }

    /** How {@link #getSubjectLocation()} should be reported. May be <code>null</code>. */
    public @Nullable LocationKind getLocationKind() {
        return this.locationKind;
    }

    /** Where the subject can be looked up. May be <code>null</code>. */
    public @Nullable String getSubjectLocation() {
        return this.subjectLocation;
    }

    /** Facts about the subject's location, reported on the {@code location} element. Never <code>null</code>. */
    public @NonNull Map<String, String> getLocationAttributes() {
        return this.locationAttributes;
    }

    /** Further facts about the subject, as CVRL extension attributes. Never <code>null</code>. */
    public @NonNull Map<String, String> getAttributes() {
        return this.attributes;
    }

    /** The file the subject lives in, reported next to the primary location. May be <code>null</code>. */
    public @Nullable String getSecondaryLocation() {
        return this.secondaryLocation;
    }

    /** The hash algorithm of {@link #getHashValue()}. May be <code>null</code>. */
    public @Nullable String getHashAlgorithm() {
        return this.hashAlgorithm;
    }

    /** The subject's hash as hex. May be <code>null</code>. */
    public @Nullable String getHashValue() {
        return this.hashValue;
    }

    /** The subject's own XML for embedding. May be <code>null</code>. */
    public byte @Nullable [] getEmbeddedXml() {
        return this.embeddedXml == null ? null : this.embeddedXml.clone();
    }

    @Override
    public OffsetDateTime getDateTimeUTC() {
        return this.delegate.getDateTimeUTC();
    }

    @Override
    public CTSeverity getSeverity() {
        return this.delegate.getSeverity();
    }

    @Override
    public String getID() {
        return this.delegate.getID();
    }

    @Override
    public String getCode() {
        return this.delegate.getCode();
    }

    @Override
    public String getField() {
        return this.delegate.getField();
    }

    @Override
    public CTDetectionLocation getLocation() {
        return this.delegate.getLocation();
    }

    @Override
    public CTDetectionText getText() {
        return this.delegate.getText();
    }

    @Override
    public CTDetectionText getSummary() {
        return this.delegate.getSummary();
    }

    @Override
    public Exception getLinkedException() {
        return this.delegate.getLinkedException();
    }
}
