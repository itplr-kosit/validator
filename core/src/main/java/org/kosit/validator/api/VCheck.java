package org.kosit.validator.api;

import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

/**
 * Main validator interface for checking incoming files.
 *
 * @author Andreas Penski
 *
 * @deprecated Replaced by {@link ValidationEngine} (ADR-008): {@link #checkInput(VInput)} becomes
 *             {@link ValidationEngine#validate(VInput)}, implemented by individual engine classes —
 *             {@code ConformanceValidation} (full pipeline, all steps) and {@code SchematronValidation} (ad-hoc mode
 *             against a single Schematron). The {@code check(...)} report-document convenience methods are dropped
 *             without replacement — report extraction is a concern of the {@link VResult}. Remaining usages mark the
 *             code paths still to be migrated.
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public interface VCheck {

    /**
     * Checks an incoming xml {@link VInput Inputs}. The result-{@link Document} is readonly. To change the this
     * document you need to copy the nodes into an new {@link Document}.
     *
     * @param VInput the resource / xml file to validate.
     * @return a result-{@link Document} (readonly)
     */
    default Document check(final VInput VInput) {
        final VResult result = checkInput(VInput);
        // readonly view of the document!!!
        return result.getReportDocument();
    }

    /**
     * Checks an incoming xml file.
     *
     * @param VInput the resource / xml file to validate.
     * @return a {@link VResult} object
     */
    VResult checkInput(VInput VInput);

    /**
     * Checks an incoming xml files in batch mode. Processing is sequential. The result-{@link Document Documents} are
     * readonly. To change the this document you need to copy them into new {@link Document Documents}.
     *
     *
     * @param VInput list of xml {@link VInput Inputs}
     * @return list of result-{@link Document Documents} (readonly)
     */
    default List<Document> check(final List<VInput> VInput) {
        return VInput.stream().map(this::check).collect(Collectors.toList());
    }

    /**
     * Checks an incoming xml files in batch mode. Processing is sequential.
     *
     * @param VInput list of xml {@link VInput Inputs}
     * @return list of {@link VResult}
     */
    default List<VResult> checkInput(final List<VInput> VInput) {
        return VInput.stream().map(this::checkInput).collect(Collectors.toList());
    }

}
