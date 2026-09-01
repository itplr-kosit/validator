package org.kosit.xvrl.jaxb;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.xml.namespace.QName;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.jaxb.JaxbHelper;
import org.kosit.xvrl.model.*;
import org.w3c.dom.Node;

import jakarta.xml.bind.JAXBElement;

/**
 * Creates the JAXB representation of the XVRL data model, hence it converts the immutable types of
 * {@code org.kosit.xvrl.model} into the XJC generated types of this package.
 *
 * <p>
 * This class is the only place that knows about both worlds; the data model itself has no JAXB dependency at all. The
 * inverse direction is implemented by {@link XvrlJaxbReader}.
 *
 * @author Philip Helger
 */
public final class XvrlJaxbCreator {

    private static final ObjectFactory OF = new ObjectFactory();

    private XvrlJaxbCreator() {
    }

    /**
     * Converts a report summary to the JAXB element that represents the root of an XVRL document.
     *
     * @param reports the report summary to convert. May not be <code>null</code>.
     * @return the JAXB root element. Never <code>null</code>.
     */
    public static @NonNull JAXBElement<XvrlReportsType> createReports(final @NonNull XvrlReports reports) {
        return OF.createReports(createReportsType(reports));
    }

    // ---------- top level types ----------

    public static @Nullable XvrlReportsType createReportsType(final @Nullable XvrlReports src) {
        if (src == null)
            return null;

        final XvrlReportsType ret = new XvrlReportsType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        ret.setMetadata(createMandatoryMetadataType(src.getMetadata()));
        for (final IXvrlReportsItem item : src.getAllItems())
            switch (item) {
                case final XvrlReport report -> ret.getReportOrReportsOrDigest().add(createReportType(report));
                case final XvrlReports reportSummary -> ret.getReportOrReportsOrDigest().add(createReportsType(reportSummary));
                case final XvrlDigest digest -> ret.getReportOrReportsOrDigest().add(createDigestType(digest));
                default -> throw new IllegalArgumentException("Unsupported XVRL reports item " + item.getClass().getName());
            }
        return ret;
    }

    public static @Nullable XvrlReportType createReportType(final @Nullable XvrlReport src) {
        if (src == null)
            return null;

        final XvrlReportType ret = new XvrlReportType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        ret.setMetadata(createMandatoryMetadataType(src.getMetadata()));
        for (final XvrlDetection detection : src.getDetections())
            ret.getDetection().add(createDetectionType(detection));
        ret.setDigest(createMandatoryDigestType(src.getDigest()));
        return ret;
    }

    public static @Nullable XvrlMetadataType createMetadataType(final @Nullable XvrlMetadata src) {
        if (src == null)
            return null;

        final XvrlMetadataType ret = new XvrlMetadataType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        for (final XvrlTimestamp e : src.getTimestamps())
            ret.getTimestamps().add(createTimestampType(e));
        for (final XvrlValidator e : src.getValidators())
            ret.getValidators().add(createValidatorType(e));
        for (final XvrlCreator e : src.getCreators())
            ret.getCreators().add(createCreatorType(e));
        for (final XvrlDocument e : src.getDocuments())
            ret.getDocuments().add(createDocumentType(e));
        for (final XvrlTitle e : src.getTitles())
            ret.getTitles().add(createTitleType(e));
        for (final XvrlSummary e : src.getSummaries())
            ret.getSummaries().add(createSummaryType(e));
        for (final XvrlCategory e : src.getCategories())
            ret.getCategories().add(createCategoryType(e));
        for (final XvrlSchema e : src.getSchemas())
            ret.getSchemas().add(createSchemaType(e));
        for (final XvrlSupplemental e : src.getSupplementals())
            ret.getSupplementals().add(createSupplementalType(e));
        return ret;
    }

    public static @Nullable XvrlDigestType createDigestType(final @Nullable XvrlDigest src) {
        if (src == null)
            return null;

        final XvrlDigestType ret = new XvrlDigestType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        ret.setValid(createValidityType(src.getValid()));
        ret.setFatalErrorCount(src.getFatalErrorCount());
        ret.setErrorCount(src.getErrorCount());
        ret.setWarningCount(src.getWarningCount());
        ret.setInfoCount(src.getInfoCount());
        ret.setUnspecifiedCount(src.getUnspecifiedCount());
        // Only touch the lazily created JAXB lists if there is something to add - an empty list is marshalled as an
        // empty attribute value, whereas an absent list emits no attribute at all
        addAll(src.getFatalErrorCodes(), ret::getFatalErrorCodes);
        addAll(src.getErrorCodes(), ret::getErrorCodes);
        addAll(src.getWarningCodes(), ret::getWarningCodes);
        addAll(src.getInfoCodes(), ret::getInfoCodes);
        addAll(src.getUnspecifiedCodes(), ret::getUnspecifiedCodes);
        ret.setWorst(createWorstType(src.getWorst()));
        return ret;
    }

    public static @Nullable XvrlDetectionType createDetectionType(final @Nullable XvrlDetection src) {
        if (src == null)
            return null;

        final XvrlDetectionType ret = new XvrlDetectionType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        ret.setSeverity(createSeverityType(src.getSeverity()));
        ret.setCode(src.getCode());
        for (final XvrlLocation e : src.getLocations())
            ret.getLocations().add(createLocationType(e));
        for (final XvrlProvenance e : src.getProvenances())
            ret.getProvenances().add(createProvenanceType(e));
        for (final XvrlTitle e : src.getTitles())
            ret.getTitles().add(createTitleType(e));
        for (final XvrlSummary e : src.getSummaries())
            ret.getSummaries().add(createSummaryType(e));
        for (final XvrlCategory e : src.getCategories())
            ret.getCategories().add(createCategoryType(e));
        for (final XvrlLet e : src.getLets())
            ret.getLets().add(createLetType(e));
        for (final XvrlMessage e : src.getMessages())
            ret.getMessages().add(createMessageType(e));
        for (final XvrlSupplemental e : src.getSupplementals())
            ret.getSupplementals().add(createSupplementalType(e));
        for (final XvrlContext e : src.getContexts())
            ret.getContexts().add(createContextType(e));
        return ret;
    }

    // ---------- leaf types ----------

    public static @Nullable XvrlLocationType createLocationType(final @Nullable XvrlLocation src) {
        if (src == null)
            return null;

        final XvrlLocationType ret = new XvrlLocationType();
        ret.getOtherAttributes().putAll(src.getOtherAttributes());
        ret.setXpathDefaultNamespace(src.getXPathDefaultNamespace());
        ret.setXpath(src.getXPath());
        ret.setJsonpointer(src.getJsonPointer());
        ret.setJsonpath(src.getJsonPath());
        ret.setHref(src.getHref());
        ret.setLine(src.getLine());
        ret.setColumn(src.getColumn());
        ret.setOctetPosition(src.getOctetPosition());
        return ret;
    }

    public static @Nullable XvrlProvenanceType createProvenanceType(final @Nullable XvrlProvenance src) {
        if (src == null)
            return null;

        final XvrlProvenanceType ret = new XvrlProvenanceType();
        for (final XvrlLocation e : src.getLocations())
            ret.getLocation().add(createLocationType(e));
        return ret;
    }

    public static @Nullable XvrlTimestampType createTimestampType(final @Nullable XvrlTimestamp src) {
        if (src == null)
            return null;

        final XvrlTimestampType ret = new XvrlTimestampType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        if (src.getValue() != null)
            ret.setValue(JaxbHelper.createTimestamp(src.getValue().toZonedDateTime()));
        return ret;
    }

    public static @Nullable XvrlValidatorType createValidatorType(final @Nullable XvrlValidator src) {
        if (src == null)
            return null;

        final XvrlValidatorType ret = new XvrlValidatorType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        ret.setName(src.getName());
        ret.setVersion(src.getVersion());
        applyContent(src, ret.getContent());
        return ret;
    }

    public static @Nullable XvrlCreatorType createCreatorType(final @Nullable XvrlCreator src) {
        if (src == null)
            return null;

        final XvrlCreatorType ret = new XvrlCreatorType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        ret.setName(src.getName());
        ret.setVersion(src.getVersion());
        ret.setInvocation(src.getInvocation());
        return ret;
    }

    public static @Nullable XvrlDocumentType createDocumentType(final @Nullable XvrlDocument src) {
        if (src == null)
            return null;

        final XvrlDocumentType ret = new XvrlDocumentType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        ret.setHref(src.getHref());
        applyContent(src, ret.getContent());
        return ret;
    }

    public static @Nullable XvrlSchemaType createSchemaType(final @Nullable XvrlSchema src) {
        if (src == null)
            return null;

        final XvrlSchemaType ret = new XvrlSchemaType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        ret.setHref(src.getHref());
        ret.setSchematypens(src.getSchemaTypeNs());
        ret.setVersion(src.getVersion());
        applyContent(src, ret.getContent());
        return ret;
    }

    public static @Nullable XvrlTitleType createTitleType(final @Nullable XvrlTitle src) {
        if (src == null)
            return null;

        final XvrlTitleType ret = new XvrlTitleType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        applyContent(src, ret.getContent());
        return ret;
    }

    public static @Nullable XvrlSummaryType createSummaryType(final @Nullable XvrlSummary src) {
        if (src == null)
            return null;

        final XvrlSummaryType ret = new XvrlSummaryType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        applyContent(src, ret.getContent());
        return ret;
    }

    public static @Nullable XvrlCategoryType createCategoryType(final @Nullable XvrlCategory src) {
        if (src == null)
            return null;

        final XvrlCategoryType ret = new XvrlCategoryType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        ret.setVocabulary(src.getVocabulary());
        applyContent(src, ret.getContent());
        return ret;
    }

    public static @Nullable XvrlSupplementalType createSupplementalType(final @Nullable XvrlSupplemental src) {
        if (src == null)
            return null;

        final XvrlSupplementalType ret = new XvrlSupplementalType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        ret.setRole(src.getRole());
        applyContent(src, ret.getContent());
        return ret;
    }

    public static @Nullable XvrlLetType createLetType(final @Nullable XvrlLet src) {
        if (src == null)
            return null;

        final XvrlLetType ret = new XvrlLetType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        ret.setName(src.getName());
        ret.setValue(src.getValue());
        applyContent(src, ret.getContent());
        return ret;
    }

    public static @Nullable XvrlMessageType createMessageType(final @Nullable XvrlMessage src) {
        if (src == null)
            return null;

        final XvrlMessageType ret = new XvrlMessageType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        for (final Object item : src.getContent())
            if (item instanceof final XvrlValueOf valueOf)
                ret.getContent().add(OF.createXvrlMessageTypeValueOf(createValueOfType(valueOf)));
            else
                ret.getContent().add(item);
        return ret;
    }

    public static XvrlMessageType.@Nullable ValueOf createValueOfType(final @Nullable XvrlValueOf src) {
        if (src == null)
            return null;

        final XvrlMessageType.ValueOf ret = new XvrlMessageType.ValueOf();
        ret.getOtherAttributes().putAll(src.getOtherAttributes());
        for (final Object item : src.getContent())
            if (item instanceof final XvrlValueOf valueOf)
                ret.getContent().add(OF.createXvrlMessageTypeValueOfValueOf(createValueOfType(valueOf)));
            else
                ret.getContent().add(item);
        return ret;
    }

    public static @Nullable XvrlContextType createContextType(final @Nullable XvrlContext src) {
        if (src == null)
            return null;

        final XvrlContextType ret = new XvrlContextType();
        applyCommon(src, ret.getOtherAttributes(), ret::setLang, ret::setId, ret::setBase, ret::setXpathDefaultNamespace);
        if (src.getLocation() != null)
            ret.getContent().add(OF.createLocation(createLocationType(src.getLocation())));
        applyContent(src, ret.getContent());
        return ret;
    }

    // ---------- enums ----------

    public static @Nullable XvrlSeverityType createSeverityType(final @Nullable XvrlSeverity src) {
        return src == null ? null : XvrlSeverityType.fromValue(src.getID());
    }

    public static @Nullable XvrlValidityType createValidityType(final @Nullable XvrlValidity src) {
        return src == null ? null : XvrlValidityType.fromValue(src.getID());
    }

    public static @Nullable XvrlWorstType createWorstType(final @Nullable XvrlWorst src) {
        return src == null ? null : XvrlWorstType.fromValue(src.getID());
    }

    // ---------- helpers ----------

    @FunctionalInterface
    private interface StringSetter {

        void accept(@Nullable String value);
    }

    private static void applyCommon(final AbstractXvrlCommonObject src, final Map<QName, String> otherAttributes, final StringSetter lang,
            final StringSetter id, final StringSetter base, final StringSetter xpathDefaultNamespace) {
        otherAttributes.putAll(src.getOtherAttributes());
        lang.accept(src.getLang());
        id.accept(src.getID());
        base.accept(src.getBase());
        xpathDefaultNamespace.accept(src.getXPathDefaultNamespace());
    }

    /**
     * The XSD requires the {@code metadata} element in {@code reports} and in {@code report}, so an absent metadata is
     * written as an empty element instead of being omitted.
     *
     * @param src the metadata to convert. May be <code>null</code>.
     * @return the JAXB metadata. Never <code>null</code>.
     */
    private static @NonNull XvrlMetadataType createMandatoryMetadataType(final @Nullable XvrlMetadata src) {
        final XvrlMetadataType ret = createMetadataType(src);
        return ret != null ? ret : new XvrlMetadataType();
    }

    /**
     * The XSD requires the {@code digest} element in {@code report}, so an absent digest is written as an empty element
     * instead of being omitted.
     *
     * @param src the digest to convert. May be <code>null</code>.
     * @return the JAXB digest. Never <code>null</code>.
     */
    private static @NonNull XvrlDigestType createMandatoryDigestType(final @Nullable XvrlDigest src) {
        final XvrlDigestType ret = createDigestType(src);
        return ret != null ? ret : new XvrlDigestType();
    }

    private static void addAll(final List<String> src, final Supplier<List<String>> target) {
        if (!src.isEmpty())
            target.get().addAll(src);
    }

    private static void applyContent(final AbstractXvrlContentObject src, final List<Object> content) {
        for (final Object item : src.getContent()) {
            if (!(item instanceof String) && !(item instanceof Node))
                throw new IllegalArgumentException("Unsupported XVRL content type " + item.getClass().getName());
            content.add(item);
        }
    }
}
