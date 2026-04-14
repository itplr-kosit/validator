package org.kosit.validator.api.compact;

import org.kosit.validator.model.xvrl.XVRLMetadata;
import org.kosit.validator.model.xvrl.XVRLReportSummary;
import org.kosit.validator.model.xvrl.Validator;
import org.kosit.validator.model.xvrl.ObjectFactory;
import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Kompakte Zusammenfassung der Validierungsergebnisse unter Verwendung des bestehenden XVRL. Bietet Convenience-Zugriff
 * auf additive Attribute über den CVRL-Namespace.
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
     * Erstellt eine neue Instanz mit einem leeren zugrunde liegenden XVRLReportSummary.
     *
     * @return neue Instanz von {@link CompactXVRLReportSummary}
     */
    public static CompactXVRLReportSummary create() {
        return new CompactXVRLReportSummary(new ObjectFactory().createXVRLReportSummary());
    }

    /**
     * Gibt die Liste der kompakten Reports zurück.
     *
     * @return Liste von {@link CompactXVRLReport}
     */
    public List<CompactXVRLReport> getReports() {
        return original.getReports().stream().map(CompactXVRLReport::new).collect(Collectors.toList());
    }

    /**
     * Fügt einen kompakten Report hinzu.
     *
     * @param report der kompakte Report
     */
    public void addReport(CompactXVRLReport report) {
        original.getReports().add(report.getOriginal());
    }

    /**
     * Setzt den Wert des 'acceptable' Attributs im CVRL-Namespace.
     *
     * @param count Anzahl akzeptabler Ergebnisse
     */
    public void setAcceptable(long count) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_ACCEPTABLE, CVRL_PREFIX), String.valueOf(count));
    }

    /**
     * Gibt den Wert des 'acceptable' Attributs aus dem CVRL-Namespace zurück.
     *
     * @return Anzahl akzeptabler Ergebnisse oder null
     */
    public Long getAcceptable() {
        String val = original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_ACCEPTABLE));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Setzt den Wert des 'rejected' Attributs im CVRL-Namespace.
     *
     * @param count Anzahl abgelehnter Ergebnisse
     */
    public void setRejected(long count) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_REJECTED, CVRL_PREFIX), String.valueOf(count));
    }

    /**
     * Gibt den Wert des 'rejected' Attributs aus dem CVRL-Namespace zurück.
     *
     * @return Anzahl abgelehnter Ergebnisse oder null
     */
    public Long getRejected() {
        String val = original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_REJECTED));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Setzt den Wert des 'processing-errors' Attributs im CVRL-Namespace.
     *
     * @param count Anzahl der Verarbeitungsfehler
     */
    public void setProcessingErrors(long count) {
        original.getOtherAttributes().put(new QName(CVRL_NS, ATTR_PROCESSING_ERRORS, CVRL_PREFIX), String.valueOf(count));
    }

    /**
     * Gibt den Wert des 'processing-errors' Attributs aus dem CVRL-Namespace zurück.
     *
     * @return Anzahl der Verarbeitungsfehler oder null
     */
    public Long getProcessingErrors() {
        String val = original.getOtherAttributes().get(new QName(CVRL_NS, ATTR_PROCESSING_ERRORS));
        return val != null ? Long.valueOf(val) : null;
    }

    /**
     * Setzt die Informationen über den verwendeten Validator.
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
     * Gibt die Informationen über den verwendeten Validator zurück.
     *
     * @return {@link ValidatorEngineInformation} oder null
     */
    public ValidatorEngineInformation getValidatorInformation() {
        return Optional.ofNullable(original.getMetadata()).map(XVRLMetadata::getValidators).stream().flatMap(Collection::stream).findFirst()
                .map(v -> new ValidatorEngineInformation(v.getName(), v.getVersion())).orElse(null);
    }

    public XVRLReportSummary getOriginal() {
        return original;
    }
}
