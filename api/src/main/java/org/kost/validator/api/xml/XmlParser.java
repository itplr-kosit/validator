package org.kost.validator.api.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.conformatron.api.model.detection.CTDetection;
import org.conformatron.api.model.source.CTReadResource;
import org.jspecify.annotations.NonNull;
import org.kosit.base.xml.XmlHelper;
import org.kosit.conformatron.detection.DetectionList;
import org.kost.validator.api.saxon.ProcessorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.WhitespaceStrippingPolicy;
import net.sf.saxon.s9api.XdmNode;

public class XmlParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlParser.class);

    /**
     * Parses the document supplied by the {@link CTReadResource} and checks well-formedness.
     *
     * @param input the input carrying the document
     * @param docConsumer do something useful with the parsed Document - only called on success
     * @return the list of detections. Never <code>null</code> but maybe empty.
     */
    @NonNull
    public static DetectionList parseXmlDom(final CTReadResource input, final Consumer<Document> docConsumer) {
        Objects.requireNonNull(input);
        Objects.requireNonNull(docConsumer);

        final String resourceId = input.getName();
        final List<CTDetection> errors = new ArrayList<>();
        try {
            // Setup XML reader
            final var builder = XmlHelper.createSafeDocumentBuilder();
            builder.setErrorHandler(new CollectingSaxErrorHandler(resourceId, errors));

            // Main reading
            final Document document = builder.parse(input.getAsInputSource());
            if (errors.isEmpty()) {
                // Parsing succeeded
                docConsumer.accept(document);

                return DetectionList.empty();
            }
        } catch (final SAXParseException e) {
            // Avoid to collect the exception again
            // already collected by CollectingErrorHandler#fatalError unless thrown directly
            if (errors.stream().noneMatch(d -> d.getLinkedException() == e)) {
                errors.add(XmlDetection.errorNotWellformed(resourceId, e));
            }
        } catch (final SAXException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exception while parsing " + resourceId, e);
            }
            errors.add(XmlDetection.errorNotWellformed(resourceId, e));
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("IOException while parsing " + resourceId, e);
            }
            errors.add(XmlDetection.ioError(resourceId, e));
        }

        // Parsing failed (for whatever reason)
        return new DetectionList(errors);
    }

    @NonNull
    public static DetectionList parseXdmNode(final CTReadResource input, final Consumer<XdmNode> docConsumer) {
        Objects.requireNonNull(input);
        Objects.requireNonNull(docConsumer);

        final String resourceId = input.getName();
        final List<CTDetection> errors = new ArrayList<>();
        try {
            // Setup XML reader
            final var processor = ProcessorProvider.getProcessor();
            final var builder = processor.newDocumentBuilder();
            builder.setLineNumbering(false);
            builder.setDTDValidation(false);
            builder.setWhitespaceStrippingPolicy(WhitespaceStrippingPolicy.NONE);
            processor.getUnderlyingConfiguration().setErrorReporterFactory(_ -> new CollectingSaxonErrorReporter(resourceId, errors));

            final XMLReader aReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            aReader.setErrorHandler(new CollectingSaxErrorHandler(resourceId, errors));
            // Main reading
            final XdmNode document = builder.build(new SAXSource(aReader, input.getAsInputSource()));
            if (errors.isEmpty()) {
                // Parsing succeeded
                docConsumer.accept(document);

                return DetectionList.empty();
            }
        } catch (final SaxonApiException e) {
            LOGGER.error("Saxon API error", e);
            errors.add(XmlDetection.saxonApiError(resourceId, e));
        } catch (final ParserConfigurationException e) {
            LOGGER.error("Somethings wrong with the XML parser", e);
        } catch (final SAXException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exception while parsing " + resourceId, e);
            }
            errors.add(XmlDetection.errorNotWellformed(resourceId, e));
        } catch (final IOException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("IOException while parsing " + resourceId, e);
            }
            errors.add(XmlDetection.ioError(resourceId, e));
        }

        // Parsing failed (for whatever reason)
        return new DetectionList(errors);
    }

    private XmlParser() {
    }
}
