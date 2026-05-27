/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kosit.validator.impl.tasks;

import static org.kosit.validator.impl.xvrl.XVRLReportBuilder.detection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Collectors;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.kosit.validator.api.Input;
import org.kosit.validator.impl.CollectingErrorEventHandler;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.input.AbstractInput;
import org.kosit.validator.impl.model.ProcessStepResult;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.tasks.CheckAction.Process.Key;
import org.kosit.validator.impl.xvrl.XVRLReportBuilder;
import org.kosit.validator.model.ValidationResultsXmlSchema;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.xvrl.model.XVRLReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

/**
 * Schema valiation of the {@link Input} with the schema of the supplied scenario. This implementation is based on JDK
 * functionality and therefore needs a {@link Source} to do the actual validation. Since we base the validator on Saxon
 * HE functionality, we have no support for schema in Saxon (e.g. the in memory version of the document is not
 * schema-aware) and need to re-read the actual source.
 *
 * Since the actual {@link Input} implementation may not be read twice, we must serialize the previously read document.
 * This implementation tries to do the validation in an efficient manner. If possible the source is read a second time
 * to validate. If not, the source is serialized to the heap upon re-read/validaiton up to a configurable file size. The
 * document is serialized to a temporary file otherwise.
 * 
 * @author Andreas Penski
 */
public class SchemaValidationAction implements CheckAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidationAction.class);

    public static final Key<Boolean, XMLSyntaxError> KEY = new Key<>(Boolean.class, XMLSyntaxError.class);

    private static final Long BA_LIMIT = 10L;

    private static final String LIMIT_PARAMETER = "schema.validation.inmem.limit";

    private final Processor processor;

    private long inMemoryLimit = Long.parseLong(System.getProperty(LIMIT_PARAMETER, BA_LIMIT.toString())) * FileUtils.ONE_MB;

    private static XVRLReport generateXVRLReport(final ValidationResultsXmlSchema result) {
        final XVRLReportBuilder builder = XVRLReportBuilder.builder("Schema Validator").addSchemas(result.getResource());
        builder.addAll(result.getXmlSyntaxError().stream().map(e -> detection().addError(e)).collect(Collectors.toList()));
        return builder.build();
    }

    private static boolean hasNoSchema(final Process results) {
        final Result<Scenario, String> scenarioSelection = results.getResult(ScenarioSelectionAction.KEY);
        return scenarioSelection == null || scenarioSelection.getObject().getSchema() == null;
    }

    private Result<Boolean, XMLSyntaxError> validate(final Process process, final Scenario scenario) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Validating document using scenario {}", scenario.getConfiguration().getName());
        final CollectingErrorEventHandler errorHandler = new CollectingErrorEventHandler();
        try ( SourceProvider validateInput = resolveSource(process) ) {
            final Validator validator = scenario.getFactory().createValidator(scenario.getSchema());
            validator.setErrorHandler(errorHandler);
            validator.validate(validateInput.getSource());
            return new Result<>(!errorHandler.hasErrors(), errorHandler.getErrors());
        } catch (final SAXException | SaxonApiException | IOException e) {
            final String msg = "Error processing schema validation for scenario " + scenario.getConfiguration().getName();
            LOGGER.error(msg, e);
            process.setStopped(true);
            final XMLSyntaxError error = new XMLSyntaxError();
            error.setMessage(msg);
            return new Result<>(Boolean.FALSE, Collections.singletonList(error));
        }
    }

    @Override
    public ProcessStepResult<Boolean, XMLSyntaxError> check(final Process results) {
        final Result<Scenario, String> scenarioResult = results.getResult(ScenarioSelectionAction.KEY);
        final ProcessStepResult<Boolean, XMLSyntaxError> stepResult = new ProcessStepResult<>(KEY);
        final Result<Boolean, XMLSyntaxError> validateResult = validate(results, scenarioResult.getObject());
        stepResult.setResult(validateResult);
        final ValidationResultsXmlSchema result = new ValidationResultsXmlSchema();
        result.getResource().addAll(scenarioResult.getObject().getConfiguration().getValidateWithXmlSchema().getResource());
        if (!validateResult.isValid()) {
            result.getXmlSyntaxError().addAll(validateResult.getErrors());
        }
        stepResult.setReport(generateXVRLReport(result));
        return stepResult;
    }

    private SourceProvider resolveSource(final Process results) throws IOException, SaxonApiException {
        final SourceProvider source;
        if (results.getInput() instanceof AbstractInput && (((AbstractInput) results.getInput()).supportsMultipleReads())) {
            source = () -> results.getInput().getSource();
        } else {
            final Result<XdmNode, XMLSyntaxError> parseResult = results.getResult(DocumentParseAction.KEY);
            source = serialize(results.getInput(), parseResult.getObject());
        }
        return source;
    }

    // intentionally return open stream/autoclosable here
    @SuppressWarnings("squid:S2095")
    private SerializedDocument serialize(final Input input, final XdmNode object) throws IOException, SaxonApiException {
        final SerializedDocument doc;
        if (input instanceof AbstractInput && ((AbstractInput) input).getLength() < getInMemoryLimit()) {
            doc = new ByteArraySerializedDocument(this.processor);
        } else {
            doc = new FileSerializedDocument(this.processor);
        }
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

    private interface SerializedDocument extends AutoCloseable, SourceProvider {

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

        public ByteArraySerializedDocument(final Processor processor) {
            this.processor = processor;
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

    public SchemaValidationAction(final Processor processor) {
        this.processor = processor;
    }

    void setInMemoryLimit(final long inMemoryLimit) {
        this.inMemoryLimit = inMemoryLimit;
    }

    public long getInMemoryLimit() {
        return this.inMemoryLimit;
    }
}
