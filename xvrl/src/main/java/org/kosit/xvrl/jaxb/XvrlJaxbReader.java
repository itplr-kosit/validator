package org.kosit.xvrl.jaxb;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.jspecify.annotations.Nullable;
import org.kosit.xvrl.model.AbstractXvrlCommonObject;
import org.kosit.xvrl.model.XvrlCategory;
import org.kosit.xvrl.model.XvrlContext;
import org.kosit.xvrl.model.XvrlCreator;
import org.kosit.xvrl.model.XvrlDetection;
import org.kosit.xvrl.model.XvrlDigest;
import org.kosit.xvrl.model.XvrlDocument;
import org.kosit.xvrl.model.XvrlLet;
import org.kosit.xvrl.model.XvrlLocation;
import org.kosit.xvrl.model.XvrlMessage;
import org.kosit.xvrl.model.XvrlMetadata;
import org.kosit.xvrl.model.XvrlProvenance;
import org.kosit.xvrl.model.XvrlReport;
import org.kosit.xvrl.model.XvrlReports;
import org.kosit.xvrl.model.XvrlSchema;
import org.kosit.xvrl.model.XvrlSeverity;
import org.kosit.xvrl.model.XvrlSummary;
import org.kosit.xvrl.model.XvrlSupplemental;
import org.kosit.xvrl.model.XvrlTimestamp;
import org.kosit.xvrl.model.XvrlTitle;
import org.kosit.xvrl.model.XvrlValidator;
import org.kosit.xvrl.model.XvrlValidity;
import org.kosit.xvrl.model.XvrlValueOf;
import org.kosit.xvrl.model.XvrlWorst;

import jakarta.xml.bind.JAXBElement;

/**
 * Reads the XVRL data model from its JAXB representation, hence it converts the XJC generated types of this package
 * into the immutable types of {@code org.kosit.xvrl.model}.
 *
 * <p>
 * This class is the inverse of {@link XvrlJaxbCreator}.
 *
 * @author Philip Helger
 */
public final class XvrlJaxbReader {

    private XvrlJaxbReader() {
    }

    // ---------- top level types ----------

    public static @Nullable XvrlReports readReports(final @Nullable XvrlReportsType src) {
        if (src == null)
            return null;

        final XvrlReports.Builder ret = XvrlReports.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.metadata(readMetadata(src.getMetadata()));
        for (final Serializable item : src.getReportOrReportsOrDigest())
            switch (item) {
                case final XvrlReportType report -> ret.addReport(readReport(report));
                case final XvrlReportsType reportSummary -> ret.addReportSummary(readReports(reportSummary));
                case final XvrlDigestType digest -> ret.addDigest(readDigest(digest));
                default -> throw new IllegalArgumentException("Unsupported XVRL reports item " + item.getClass().getName());
            }
        return ret.build();
    }

    public static @Nullable XvrlReport readReport(final @Nullable XvrlReportType src) {
        if (src == null)
            return null;

        final XvrlReport.Builder ret = XvrlReport.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.metadata(readMetadata(src.getMetadata()));
        for (final XvrlDetectionType e : src.getDetection())
            ret.addDetection(readDetection(e));
        ret.digest(readDigest(src.getDigest()));
        return ret.build();
    }

    public static @Nullable XvrlMetadata readMetadata(final @Nullable XvrlMetadataType src) {
        if (src == null)
            return null;

        final XvrlMetadata.Builder ret = XvrlMetadata.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        for (final XvrlTimestampType e : src.getTimestamps())
            ret.addTimestamp(readTimestamp(e));
        for (final XvrlValidatorType e : src.getValidators())
            ret.addValidator(readValidator(e));
        for (final XvrlCreatorType e : src.getCreators())
            ret.addCreator(readCreator(e));
        for (final XvrlDocumentType e : src.getDocuments())
            ret.addDocument(readDocument(e));
        for (final XvrlTitleType e : src.getTitles())
            ret.addTitle(readTitle(e));
        for (final XvrlSummaryType e : src.getSummaries())
            ret.addSummary(readSummary(e));
        for (final XvrlCategoryType e : src.getCategories())
            ret.addCategory(readCategory(e));
        for (final XvrlSchemaType e : src.getSchemas())
            ret.addSchema(readSchema(e));
        for (final XvrlSupplementalType e : src.getSupplementals())
            ret.addSupplemental(readSupplemental(e));
        return ret.build();
    }

    public static @Nullable XvrlDigest readDigest(final @Nullable XvrlDigestType src) {
        if (src == null)
            return null;

        final XvrlDigest.Builder ret = XvrlDigest.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.valid(readValidity(src.getValid()));
        ret.fatalErrorCount(src.getFatalErrorCount());
        ret.errorCount(src.getErrorCount());
        ret.warningCount(src.getWarningCount());
        ret.infoCount(src.getInfoCount());
        ret.unspecifiedCount(src.getUnspecifiedCount());
        ret.addFatalErrorCodes(src.getFatalErrorCodes());
        ret.addErrorCodes(src.getErrorCodes());
        ret.addWarningCodes(src.getWarningCodes());
        ret.addInfoCodes(src.getInfoCodes());
        ret.addUnspecifiedCodes(src.getUnspecifiedCodes());
        ret.worst(readWorst(src.getWorst()));
        return ret.build();
    }

    public static @Nullable XvrlDetection readDetection(final @Nullable XvrlDetectionType src) {
        if (src == null)
            return null;

        final XvrlDetection.Builder ret = XvrlDetection.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.severity(readSeverity(src.getSeverity()));
        ret.code(src.getCode());
        for (final XvrlLocationType e : src.getLocations())
            ret.addLocation(readLocation(e));
        for (final XvrlProvenanceType e : src.getProvenances())
            ret.addProvenance(readProvenance(e));
        for (final XvrlTitleType e : src.getTitles())
            ret.addTitle(readTitle(e));
        for (final XvrlSummaryType e : src.getSummaries())
            ret.addSummary(readSummary(e));
        for (final XvrlCategoryType e : src.getCategories())
            ret.addCategory(readCategory(e));
        for (final XvrlLetType e : src.getLets())
            ret.addLet(readLet(e));
        for (final XvrlMessageType e : src.getMessages())
            ret.addMessage(readMessage(e));
        for (final XvrlSupplementalType e : src.getSupplementals())
            ret.addSupplemental(readSupplemental(e));
        for (final XvrlContextType e : src.getContexts())
            ret.addContext(readContext(e));
        return ret.build();
    }

    // ---------- leaf types ----------

    public static @Nullable XvrlLocation readLocation(final @Nullable XvrlLocationType src) {
        if (src == null)
            return null;

        return XvrlLocation.builder().otherAttributes(src.getOtherAttributes()).xpathDefaultNamespace(src.getXpathDefaultNamespace())
                .xpath(src.getXpath()).jsonPointer(src.getJsonpointer()).jsonPath(src.getJsonpath()).href(src.getHref()).line(src.getLine())
                .column(src.getColumn()).octetPosition(src.getOctetPosition()).build();
    }

    public static @Nullable XvrlProvenance readProvenance(final @Nullable XvrlProvenanceType src) {
        if (src == null)
            return null;

        final XvrlProvenance.Builder ret = XvrlProvenance.builder();
        for (final XvrlLocationType e : src.getLocation())
            ret.addLocation(readLocation(e));
        return ret.build();
    }

    public static @Nullable XvrlTimestamp readTimestamp(final @Nullable XvrlTimestampType src) {
        if (src == null)
            return null;

        final XvrlTimestamp.Builder ret = XvrlTimestamp.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.value(readOffsetDateTime(src.getValue()));
        return ret.build();
    }

    public static @Nullable XvrlValidator readValidator(final @Nullable XvrlValidatorType src) {
        if (src == null)
            return null;

        final XvrlValidator.Builder ret = XvrlValidator.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.name(src.getName()).version(src.getVersion()).addAllContent(src.getContent());
        return ret.build();
    }

    public static @Nullable XvrlCreator readCreator(final @Nullable XvrlCreatorType src) {
        if (src == null)
            return null;

        final XvrlCreator.Builder ret = XvrlCreator.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.name(src.getName()).version(src.getVersion()).invocation(src.getInvocation());
        return ret.build();
    }

    public static @Nullable XvrlDocument readDocument(final @Nullable XvrlDocumentType src) {
        if (src == null)
            return null;

        final XvrlDocument.Builder ret = XvrlDocument.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.href(src.getHref()).addAllContent(src.getContent());
        return ret.build();
    }

    public static @Nullable XvrlSchema readSchema(final @Nullable XvrlSchemaType src) {
        if (src == null)
            return null;

        final XvrlSchema.Builder ret = XvrlSchema.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.href(src.getHref()).schemaTypeNs(src.getSchematypens()).version(src.getVersion()).addAllContent(src.getContent());
        return ret.build();
    }

    public static @Nullable XvrlTitle readTitle(final @Nullable XvrlTitleType src) {
        if (src == null)
            return null;

        final XvrlTitle.Builder ret = XvrlTitle.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.addAllContent(src.getContent());
        return ret.build();
    }

    public static @Nullable XvrlSummary readSummary(final @Nullable XvrlSummaryType src) {
        if (src == null)
            return null;

        final XvrlSummary.Builder ret = XvrlSummary.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.addAllContent(src.getContent());
        return ret.build();
    }

    public static @Nullable XvrlCategory readCategory(final @Nullable XvrlCategoryType src) {
        if (src == null)
            return null;

        final XvrlCategory.Builder ret = XvrlCategory.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.vocabulary(src.getVocabulary()).addAllContent(src.getContent());
        return ret.build();
    }

    public static @Nullable XvrlSupplemental readSupplemental(final @Nullable XvrlSupplementalType src) {
        if (src == null)
            return null;

        final XvrlSupplemental.Builder ret = XvrlSupplemental.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.role(src.getRole()).addAllContent(src.getContent());
        return ret.build();
    }

    public static @Nullable XvrlLet readLet(final @Nullable XvrlLetType src) {
        if (src == null)
            return null;

        final XvrlLet.Builder ret = XvrlLet.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        ret.name(src.getName()).value(src.getValue()).addAllContent(src.getContent());
        return ret.build();
    }

    public static @Nullable XvrlMessage readMessage(final @Nullable XvrlMessageType src) {
        if (src == null)
            return null;

        final XvrlMessage.Builder ret = XvrlMessage.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        for (final Object item : src.getContent())
            if (item instanceof final JAXBElement<?> jaxbElement) {
                if (jaxbElement.getValue() instanceof final XvrlMessageType.ValueOf valueOf)
                    ret.addContent(readValueOf(valueOf));
            } else
                if (item != null)
                    ret.addAllContent(List.of(item));
        return ret.build();
    }

    public static @Nullable XvrlValueOf readValueOf(final XvrlMessageType.@Nullable ValueOf src) {
        if (src == null)
            return null;

        final XvrlValueOf.Builder ret = XvrlValueOf.builder().otherAttributes(src.getOtherAttributes());
        for (final Object item : src.getContent())
            if (item instanceof final JAXBElement<?> jaxbElement) {
                if (jaxbElement.getValue() instanceof final XvrlMessageType.ValueOf valueOf)
                    ret.addContent(readValueOf(valueOf));
            } else
                if (item != null)
                    ret.addAllContent(List.of(item));
        return ret.build();
    }

    public static @Nullable XvrlContext readContext(final @Nullable XvrlContextType src) {
        if (src == null)
            return null;

        final XvrlContext.Builder ret = XvrlContext.builder();
        applyCommon(src.getOtherAttributes(), src.getLang(), src.getId(), src.getBase(), src.getXpathDefaultNamespace(), ret);
        for (final Object item : src.getContent())
            if (item instanceof final JAXBElement<?> jaxbElement) {
                if (jaxbElement.getValue() instanceof final XvrlLocationType location)
                    ret.location(readLocation(location));
            } else
                if (item != null)
                    ret.addAllContent(List.of(item));
        return ret.build();
    }

    // ---------- enums ----------

    public static @Nullable XvrlSeverity readSeverity(final @Nullable XvrlSeverityType src) {
        return src == null ? null : XvrlSeverity.getFromIDOrNull(src.value());
    }

    public static @Nullable XvrlValidity readValidity(final @Nullable XvrlValidityType src) {
        return src == null ? null : XvrlValidity.getFromIDOrNull(src.value());
    }

    public static @Nullable XvrlWorst readWorst(final @Nullable XvrlWorstType src) {
        return src == null ? null : XvrlWorst.getFromIDOrNull(src.value());
    }

    // ---------- helpers ----------

    /**
     * Converts the JAXB representation of {@code xs:dateTime} into a {@link OffsetDateTime}.
     *
     * @param value the value to convert. May be <code>null</code>.
     * @return the converted value or <code>null</code> if the source was <code>null</code>.
     */
    public static @Nullable OffsetDateTime readOffsetDateTime(final @Nullable XMLGregorianCalendar value) {
        return value == null ? null : value.toGregorianCalendar().toZonedDateTime().toOffsetDateTime();
    }

    private static void applyCommon(final Map<QName, String> otherAttributes, final @Nullable String lang, final @Nullable String id,
            final @Nullable String base, final @Nullable String xpathDefaultNamespace,
            final AbstractXvrlCommonObject.AbstractCommonBuilder<?, ?> builder) {
        builder.otherAttributes(otherAttributes);
        builder.lang(lang);
        builder.id(id);
        builder.base(base);
        builder.xpathDefaultNamespace(xpathDefaultNamespace);
    }
}
