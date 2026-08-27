package org.kosit.validator.impl.tasks;

import static org.kosit.validator.xvrl.XvrlDetectionBuilder.detectionBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import org.conformatron.api.model.source.CTReadResource;
import org.jspecify.annotations.NonNull;
import org.kosit.base.error.DefaultSimpleError;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.impl.conformatron.source.ValidationSource;
import org.kosit.validator.impl.conformatron.source.XdmNodeValidationSource;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.xvrl.XvrlDetectionBuilder;
import org.kosit.validator.xvrl.XvrlReportBuilder;
import org.kosit.validator.xvrl.XvrlSupplementalBuilder;
import org.kosit.xvrl.model.XvrlReportType;
import org.kosit.xvrl.model.XvrlSeverityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

/**
 * Implements parsing functionality. Checks for well-formedness.
 *
 * @author Andreas Penski
 */
public class DocumentParseTask implements CheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentParseTask.class);

    public static final Process.ProcessKey<XdmNode, SimpleError> KEY = new Process.ProcessKey<>(XdmNode.class, SimpleError.class);

    private final Processor processor;

    private static XvrlReportType generateXvrlReport(final SingleProcessingResult<XdmNode, SimpleError> parserResult) {
        final XvrlReportBuilder builder = XvrlReportBuilder.builder("Document wellformedness Validator");
        if (parserResult.isValid()) {
            final XvrlDetectionBuilder detection = detectionBuilder().severity(XvrlSeverityType.INFO)
                    .add(XvrlSupplementalBuilder.supplemental().addContent(parserResult.getObject()));
            builder.add(detection);
        } else {
            final XvrlDetectionBuilder detection = detectionBuilder().severity(XvrlSeverityType.ERROR);
            parserResult.getErrors().forEach(detection::addError);
        }
        return builder.build();
    }

    /**
     * Outcome of a parse run: the legacy result for the existing pipeline plus the conformatron handshake object
     * (facade migration, step {@code parse-document}).
     *
     * @param result the legacy parse result consumed by the downstream steps
     * @param parsedSource the conformatron handshake object; {@code null} on parse failure or when the source bytes
     *            could not be retained (e.g. {@link XdmNodeVInput} shortcut)
     */
    public record ParseOutcome(SingleProcessingResult<XdmNode, SimpleError> result, XdmNodeValidationSource parsedSource) {
    }

    /**
     * Parses and checks the supplied document for well-formedness. This is the first processing step of the validation
     * tool. This function explicitly skips validation against a schema.
     *
     * @param content a document
     * @return result of the parsing including any errors
     */
    public SingleProcessingResult<XdmNode, SimpleError> parseDocument(final CTReadResource content) {
        return parseRetaining(content).result();
    }

    /**
     * Parses the supplied document and additionally retains the entire source document as an immutable byte array
     * (conformatron-api step 2): parsing operates on that buffer and the {@link XdmNodeValidationSource} carrying
     * bytes, SHA-512 hash and the parsed Saxon node is created from it. When the source type does not allow byte
     * retention, the document is parsed directly as before and no handshake object is created.
     *
     * @param content a document
     * @return the legacy parse result plus the conformatron handshake object (if available)
     */
    public ParseOutcome parseRetaining(final @NonNull CTReadResource content) {
        Objects.requireNonNull(content);

        try {
            final DocumentBuilder builder = this.processor.newDocumentBuilder();
            builder.setLineNumbering(true);

            final XdmNode doc = builder.build(content.getAsSource());
            final XdmNodeValidationSource parsedSource = new XdmNodeValidationSource(ValidationSource.completeXml(content), doc);
            return new ParseOutcome(new SingleProcessingResult<>(doc), parsedSource);
        } catch (final SaxonApiException | IOException e) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("Exception while parsing {}", content.getName(), e);
            final SimpleError error = DefaultSimpleError.builderError()
                    .message("IOException while reading resource " + content.getName() + ": " + e.getMessage()).linkedException(e).build();
            return new ParseOutcome(new SingleProcessingResult<>(Collections.singletonList(error)), null);
        }
    }

    @Override
    public ProcessStepResult<XdmNode, SimpleError> check(final Process process) {
        final ProcessStepResult<XdmNode, SimpleError> result = new ProcessStepResult<>(KEY);
        final ParseOutcome outcome = parseRetaining(process.getInput());
        final SingleProcessingResult<XdmNode, SimpleError> parserResult = outcome.result();
        if (outcome.parsedSource() != null) {
            process.setParsedSource(outcome.parsedSource());
        }
        result.setResult(parserResult);
        if (parserResult.isInvalid()) {
            process.setStopped(true);
        }
        result.setReport(generateXvrlReport(parserResult));
        return result;
    }

    public DocumentParseTask(final Processor processor) {
        this.processor = processor;
    }
}
