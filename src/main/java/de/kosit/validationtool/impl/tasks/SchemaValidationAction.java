/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl.tasks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.impl.CollectingErrorEventHandler;
import de.kosit.validationtool.impl.ObjectFactory;
import de.kosit.validationtool.impl.input.AbstractInput;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.CreateReportInput;
import de.kosit.validationtool.model.reportInput.ValidationResultsXmlSchema;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;
import de.kosit.validationtool.model.scenarios.ScenarioType;

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
@Slf4j
public class SchemaValidationAction implements CheckAction {

    private static class ByteArraySerializedDocument implements SerializedDocument {

        private byte[] bytes;

        @Override
        public void serialize(final XdmNode node) throws SaxonApiException, IOException {
            try ( final ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
                final Serializer serializer = ObjectFactory.createProcessor().newSerializer();
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
    }

    private static class FileSerializedDocument implements SerializedDocument {

        private final Path file;

        FileSerializedDocument() throws IOException {
            this.file = Files.createTempFile("validator", ".xml");
        }

        @Override
        public void serialize(final XdmNode node) throws SaxonApiException, IOException {
            try ( final OutputStream out = Files.newOutputStream(this.file) ) {
                final Serializer serializer = ObjectFactory.createProcessor().newSerializer();
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

    private static final Long BA_LIMIT = 10L;

    private static final String LIMIT_PARAMETER = "schema.validation.inmem.limit";

    @Setter(AccessLevel.PACKAGE)
    @Getter
    private long inMemoryLimit = Long.parseLong(System.getProperty(LIMIT_PARAMETER, BA_LIMIT.toString())) * FileUtils.ONE_MB;

    private Result<Boolean, XMLSyntaxError> validate(final Bag results, final ScenarioType scenarioType) {
        log.debug("Validating document using scenario {}", scenarioType.getName());
        final CollectingErrorEventHandler errorHandler = new CollectingErrorEventHandler();
        try ( final SourceProvider validateInput = resolveSource(results) ) {

            final Validator validator = ObjectFactory.createValidator(scenarioType.getSchema());
            validator.setErrorHandler(errorHandler);
            validator.validate(validateInput.getSource());
            return new Result<>(!errorHandler.hasErrors(), errorHandler.getErrors());
        } catch (final SAXException | SaxonApiException | IOException e) {
            final String msg = String.format("Error processing schema validation for scenario %s", scenarioType.getName());
            log.error(msg, e);
            results.addProcessingError(msg);
            return new Result<>(Boolean.FALSE);
        }
    }

    @Override
    public void check(final Bag results) {
        final CreateReportInput report = results.getReportInput();
        final ScenarioType scenario = results.getScenarioSelectionResult().getObject();

        final Result<Boolean, XMLSyntaxError> validateResult = validate(results, scenario);

        results.setSchemaValidationResult(validateResult);
        final ValidationResultsXmlSchema result = new ValidationResultsXmlSchema();
        report.setValidationResultsXmlSchema(result);
        result.getResource().addAll(scenario.getValidateWithXmlSchema().getResource());
        if (!validateResult.isValid()) {
            result.getXmlSyntaxError().addAll(validateResult.getErrors());
        }

    }

    private SourceProvider resolveSource(final Bag results) throws IOException, SaxonApiException {
        final SourceProvider source;
        if (results.getInput() instanceof AbstractInput && (((AbstractInput) results.getInput()).supportsMultipleReads())) {
            source = () -> results.getInput().getSource();
        } else {
            source = serialize(results.getInput(), results.getParserResult().getObject());
        }
        return source;

    }

    private SerializedDocument serialize(final Input input, final XdmNode object) throws IOException, SaxonApiException {
        final SerializedDocument doc;
        if (input instanceof AbstractInput && ((AbstractInput) input).getLength() < getInMemoryLimit()) {
            doc = new ByteArraySerializedDocument();
        } else {
            doc = new FileSerializedDocument();
        }
        doc.serialize(object);
        return doc;
    }

    @Override
    public boolean isSkipped(final Bag results) {
        return hasNoSchema(results);
    }

    private static boolean hasNoSchema(final Bag results) {
        return results.getScenarioSelectionResult() == null || results.getScenarioSelectionResult().getObject().getSchema() == null;
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

}
