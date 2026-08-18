package org.kosit.validator.api;

import java.util.List;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

/**
 * Main validator interface for checking incoming files.
 *
 * @author Andreas Penski
 *
 * @deprecated Replaced by {@link ValidationEngine} (ADR-008): {@link #checkInput(Input)} becomes
 *             {@link ValidationEngine#validate(Input)}, implemented by individual engine classes —
 *             {@code ConformanceValidation} (full pipeline, all steps) and {@code SchematronValidation} (ad-hoc mode
 *             against a single Schematron). The {@code check(...)} report-document convenience methods are dropped
 *             without replacement — report extraction is a concern of the {@link Result}. Remaining usages mark the
 *             code paths still to be migrated.
 */
@Deprecated(since = "2.0.0", forRemoval = true)
public interface Check {

    /**
     * Checks an incoming xml {@link Input Inputs}. The result-{@link Document} is readonly. To change the this document
     * you need to copy the nodes into an new {@link Document}.
     *
     * @param input the resource / xml file to validate.
     * @return a result-{@link Document} (readonly)
     */
    default Document check(final Input input) {
        final Result result = checkInput(input);
        // readonly view of the document!!!
        return result.getReportDocument();
    }

    /**
     * Checks an incoming xml file.
     *
     * @param input the resource / xml file to validate.
     * @return a {@link Result} object
     */
    Result checkInput(Input input);

    /**
     * Checks an incoming xml files in batch mode. Processing is sequential. The result-{@link Document Documents} are
     * readonly. To change the this document you need to copy them into new {@link Document Documents}.
     *
     *
     * @param input list of xml {@link Input Inputs}
     * @return list of result-{@link Document Documents} (readonly)
     */
    default List<Document> check(final List<Input> input) {
        return input.stream().map(this::check).collect(Collectors.toList());
    }

    /**
     * Checks an incoming xml files in batch mode. Processing is sequential.
     *
     * @param input list of xml {@link Input Inputs}
     * @return list of {@link Result}
     */
    default List<Result> checkInput(final List<Input> input) {
        return input.stream().map(this::checkInput).collect(Collectors.toList());
    }

}
