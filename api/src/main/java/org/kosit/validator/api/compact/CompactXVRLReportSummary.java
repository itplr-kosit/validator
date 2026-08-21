package org.kosit.validator.api.compact;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import org.kosit.xvrl.model.ObjectFactory;
import org.kosit.xvrl.model.Validator;
import org.kosit.xvrl.model.XVRLMetadata;
import org.kosit.xvrl.model.XVRLReportSummary;

/**
 * Compact summary of the validation results using the existing XVRL. Provides convenience access to additive attributes
 * via the CVRL namespace.
 */
public class CompactXVRLReportSummary {

    public static final String CVRL_NS = "http://www.xoev.de/de/validator/framework/2/compact-format";

    public static final String CVRL_PREFIX = "compactvrl";

    private static final String ATTR_ACCEPTABLE = "acceptable";

    private static final String ATTR_REJECTED = "rejected";

    private static final String ATTR_PROCESSING_ERRORS = "processing-errors";

    private final XVRLReportSummary original;

    public CompactXVRLReportSummary(XVRLReportSummary original) {
        this.original = original;
    }

    /**
     * Creates a new instance with an empty underlying XVRLReportSummary.
     *
     * @return new instance of {@link CompactXVRLReportSummary}
     */
    public static CompactXVRLReportSummary create() {
        return new CompactXVRLReportSummary(new ObjectFactory().createXVRLReportSummary());
    }

    /**
     * Returns the list of compact reports.
     *
     * @return list of {@link CompactXVRLReport}
     */
    public List<CompactXVRLReport> getReports() {
        return original.getReports().stream().map(CompactXVRLReport::new).toList();
    }

    /**
     * Adds a compact report.
     *
     * @param report the compact report
     */
    public void addReport(CompactXVRLReport report) {
        original.getReports().add(report.getOriginal());
    }

    /**
     * Sets the value of the 'acceptable' attribute in the CVRL namespace.
     *
     * @param count number of acceptable results
     */
    public void setAcceptable(long count) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_ACCEPTABLE, CVRL_PREFIX), String.valueOf(count));
    }

    /**
     * Returns the value of the 'acceptable' attribute from the CVRL namespace.
     *
     * @return number of acceptable results or null
     */
    public Long getAcceptable() {
        String val = original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_ACCEPTABLE));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Sets the value of the 'rejected' attribute in the CVRL namespace.
     *
     * @param count number of rejected results
     */
    public void setRejected(long count) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_REJECTED, CVRL_PREFIX), String.valueOf(count));
    }

    /**
     * Returns the value of the 'rejected' attribute from the CVRL namespace.
     *
     * @return number of rejected results or null
     */
    public Long getRejected() {
        String val = original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_REJECTED));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Sets the value of the 'processing-errors' attribute in the CVRL namespace.
     *
     * @param count number of processing errors
     */
    public void setProcessingErrors(long count) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_PROCESSING_ERRORS, CVRL_PREFIX), String.valueOf(count));
    }

    /**
     * Returns the value of the 'processing-errors' attribute from the CVRL namespace.
     *
     * @return number of processing errors or null
     */
    public Long getProcessingErrors() {
        String val = original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_PROCESSING_ERRORS));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Sets the information about the validator used.
     *
     * @param info {@link ValidatorEngineInformation}
     */
    public void setValidatorInformation(ValidatorEngineInformation info) {
        if (original.getMetadata() == null) {
            original.setMetadata(new ObjectFactory().createXVRLMetadata());
        }
        Validator v = new ObjectFactory().createValidator();
        v.setName(info.getName());
        v.setVersion(info.getVersion());
        original.getMetadata().getValidators().clear();
        original.getMetadata().getValidators().add(v);
    }

    /**
     * Returns the information about the validator used.
     *
     * @return {@link ValidatorEngineInformation} or null
     */
    public ValidatorEngineInformation getValidatorInformation() {
        return Optional.ofNullable(original.getMetadata()).map(XVRLMetadata::getValidators).stream().flatMap(Collection::stream).findFirst()
                .map(v -> new ValidatorEngineInformation(v.getName(), v.getVersion())).orElse(null);
    }

    public XVRLReportSummary getOriginal() {
        return original;
    }
}
