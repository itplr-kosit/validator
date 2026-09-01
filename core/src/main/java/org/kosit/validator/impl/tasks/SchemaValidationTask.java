package org.kosit.validator.impl.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.conformatron.api.model.source.CTReadResource;
import org.kosit.base.error.DefaultSimpleError;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.impl.CollectingErrorEventHandler;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.tasks.CheckTask.Process.ProcessKey;
import org.kosit.validator.model.ValidationResultsXmlSchema;
import org.kosit.validator.xvrl.XvrlHelper;
import org.kosit.xvrl.model.XvrlDetection;
import org.kosit.xvrl.model.XvrlMetadata;
import org.kosit.xvrl.model.XvrlReport;
import org.kosit.xvrl.model.XvrlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

/**
 * Schema valiation of the {@link VInput} with the schema of the supplied scenario. This implementation is based on JDK
 * functionality and therefore needs a {@link Source} to do the actual validation. Since we base the validator on Saxon
 * HE functionality, we have no support for schema in Saxon (e.g. the in memory version of the document is not
 * schema-aware) and need to re-read the actual source.
 *
 * Since the actual {@link VInput} implementation may not be read twice, we must serialize the previously read document.
 * This implementation tries to do the validation in an efficient manner. If possible the source is read a second time
 * to validate. If not, the source is serialized to the heap upon re-read/validaiton up to a configurable file size. The
 * document is serialized to a temporary file otherwise.
 * 
 * @author Andreas Penski
 */
public class SchemaValidationTask implements CheckTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidationTask.class);

    public static final ProcessKey<Boolean, SimpleError> KEY = new ProcessKey<>(Boolean.class, SimpleError.class);

    private static final Long BA_LIMIT = Long.valueOf(10L);

    private static final String LIMIT_PARAMETER = "schema.validation.inmem.limit";

    private final Processor processor;

    private long inMemoryLimit = Long.parseLong(System.getProperty(LIMIT_PARAMETER, BA_LIMIT.toString())) * FileUtils.ONE_MB;

    private static XvrlReport generateXvrlReport(final ValidationResultsXmlSchema result) {
        final var mdBuilder = XvrlMetadata.builder().validator("Schema Validator");
        for (final var schema : result.getResource())
            mdBuilder.addSchema(XvrlSchema.builder().href(schema.getLocation()).schemaTypeNs(schema.getName()));

        final var builder = XvrlReport.builder().metadata(mdBuilder);
        builder.addDetections(result.getXmlSyntaxError().stream().map(e -> XvrlDetection.builder().error(e).build()).toList());
        return XvrlHelper.finalizeAndBuild(builder);
    }

    private static boolean hasNoSchema(final Process results) {
        final SingleProcessingResult<Scenario, String> scenarioSelection = results.getResult(ScenarioSelectionTask.KEY);
        return scenarioSelection == null || scenarioSelection.getObject().getSchema() == null;
    }

    private SingleProcessingResult<Boolean, SimpleError> validate(final Process process, final Scenario scenario) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Validating document using scenario {}", scenario.getConfiguration().getName());
        final CollectingErrorEventHandler errorHandler = new CollectingErrorEventHandler();
        try ( SourceProvider validateInput = resolveSource(process) ) {
            final Validator validator = scenario.getFactory().createValidator(scenario.getSchema());
            validator.setErrorHandler(errorHandler);
            validator.validate(validateInput.getSource());
            return new SingleProcessingResult<>(!errorHandler.hasErrors(), errorHandler.getErrors());
        } catch (final SAXException | SaxonApiException | IOException e) {
            final String msg = "Error processing schema validation for scenario " + scenario.getConfiguration().getName();
            LOGGER.error(msg, e);
            process.setStopped(true);
            final SimpleError error = DefaultSimpleError.builderError().message(msg).linkedException(e).build();
            return new SingleProcessingResult<>(Boolean.FALSE, Collections.singletonList(error));
        }
    }

    @Override
    public ProcessStepResult<Boolean, SimpleError> check(final Process results) {
        final SingleProcessingResult<Scenario, String> scenarioResult = results.getResult(ScenarioSelectionTask.KEY);
        final ProcessStepResult<Boolean, SimpleError> stepResult = new ProcessStepResult<>(KEY);
        final SingleProcessingResult<Boolean, SimpleError> validateResult = validate(results, scenarioResult.getObject());
        stepResult.setResult(validateResult);
        final ValidationResultsXmlSchema result = new ValidationResultsXmlSchema();
        result.getResource().addAll(scenarioResult.getObject().getConfiguration().getValidateWithXmlSchema().getResource());
        if (!validateResult.isValid()) {
            result.getXmlSyntaxError().addAll(validateResult.getErrors());
        }
        stepResult.setReport(generateXvrlReport(result));
        return stepResult;
    }

    private SourceProvider resolveSource(final Process results) throws IOException, SaxonApiException {
        final SourceProvider source;
        final SingleProcessingResult<XdmNode, SimpleError> parseResult = results.getResult(DocumentParseTask.KEY);
        source = serialize(results.getInput(), parseResult.getObject());
        return source;
    }

    // intentionally return open stream/autoclosable here
    private SerializedDocument serialize(final CTReadResource input, final XdmNode object) throws IOException, SaxonApiException {
        final SerializedDocument doc;
        doc = new FileSerializedDocument(this.processor);
        doc.serialize(object);
        return doc;
    }

    @Override
    public boolean isSkipped(final Process results) {
        return hasNoSchema(results);
    }

    private interface SourceProvider extends AutoCloseable {

        Source getSource() throws IOException;

        @Override
        default void close() throws IOException {
            // nothing
        }
    }

    private interface SerializedDocument extends SourceProvider {

        void serialize(XdmNode node) throws SaxonApiException, IOException;

        InputStream openStream() throws IOException;

        @Override
        default Source getSource() throws IOException {
            return new StreamSource(openStream());
        }
    }

    private static class ByteArraySerializedDocument implements SerializedDocument {

        private final Processor processor;

        private byte[] bytes;

        @Override
        public void serialize(final XdmNode node) throws SaxonApiException, IOException {
            try ( ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
                final Serializer serializer = this.processor.newSerializer();
                serializer.setOutputStream(out);
                serializer.serializeNode(node);
                serializer.close();
                this.bytes = out.toByteArray();
            }
        }

        @Override
        public void close() {
            // nothing do do
        }

        @Override
        public InputStream openStream() {
            return new ByteArrayInputStream(this.bytes);
        }

        public ByteArraySerializedDocument(final Processor saxonProcessor) {
            this.processor = saxonProcessor;
        }
    }

    private static class FileSerializedDocument implements SerializedDocument {

        private final Path file;

        private final Processor processor;

        FileSerializedDocument(final Processor processor) throws IOException {
            this.file = Files.createTempFile("validator", ".xml");
            this.processor = processor;
        }

        @Override
        public void serialize(final XdmNode node) throws SaxonApiException, IOException {
            try ( OutputStream out = Files.newOutputStream(this.file) ) {
                final Serializer serializer = this.processor.newSerializer();
                serializer.setOutputStream(out);
                serializer.serializeNode(node);
                serializer.close();
            }
        }

        @Override
        public void close() throws IOException {
            Files.deleteIfExists(this.file);
        }

        @Override
        public InputStream openStream() throws IOException {
            return Files.newInputStream(this.file);
        }
    }

    public SchemaValidationTask(final Processor processor) {
        this.processor = processor;
    }

    void setInMemoryLimit(final long inMemoryLimit) {
        this.inMemoryLimit = inMemoryLimit;
    }

    public long getInMemoryLimit() {
        return this.inMemoryLimit;
    }
}
