package org.kosit.jaxb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.xml.XmlHelper;
import org.kosit.jaxb.namespacemapper.MappedNamespacePrefixMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEventHandler;

/**
 * Generic, reusable JAXB read/write helper.
 * <p>
 * Reads and writes JAXB-annotated objects to and from a variety of generic source/output types:
 * <ul>
 * <li>read from: {@link URI}, {@link InputStream}, {@link Reader}, {@link Source}, {@code byte[]}, {@link String},
 * {@link Path}, {@link File}.
 * <li>write to: {@link String} (returned), {@link OutputStream}, {@link Writer}, {@link Result}, {@link Path},
 * {@link File}.
 * </ul>
 * <p>
 * Stream-based reads disable DTD processing and external entity resolution to avoid XXE attacks. The {@link Source}
 * overload bypasses that hardening; callers passing a custom {@link Source} are responsible for the parser
 * configuration.
 * <p>
 * Instances are not thread-safe with respect to configuration setters. The JAXB context is created once and reused.
 * 
 * @param <T> Type of class to read and write
 */
public abstract class AbstractJaxbConverter<T> {

    public static final boolean DEFAULT_FORMATTED = true;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJaxbConverter.class);

    private static final String NAMESPACE_PREFIX_MAPPER_PROPERTY = "org.glassfish.jaxb.namespacePrefixMapper";

    private final JAXBContext jaxbContext;

    private final Class<T> type;

    private final Function<T, JAXBElement<T>> jaxbMapper;

    private @Nullable Schema schema;

    private @Nullable ValidationEventHandler eventHandler;

    private boolean formattedOutput = DEFAULT_FORMATTED;

    private Charset encoding = StandardCharsets.UTF_8;

    private Map<String, String> namespacePrefixMap = Map.of();

    /**
     * Creates a service backed by the given JAXB context.
     *
     * @param jaxbContext the JAXB context to use for all read/write operations
     * @param type Type of object to be marshalled
     * @param jaxbMapper Mapper from object to element
     * @throws IllegalArgumentException if {@code jaxbContext} is {@code null}
     */
    public AbstractJaxbConverter(final JAXBContext jaxbContext, final Class<T> type, final Function<T, JAXBElement<T>> jaxbMapper) {
        ObjectHelper.requireNonNull(jaxbContext, "jaxbContext");
        ObjectHelper.requireNonNull(type, "type");
        ObjectHelper.requireNonNull(jaxbMapper, "jaxbMapper");
        this.jaxbContext = jaxbContext;
        this.type = type;
        this.jaxbMapper = jaxbMapper;
    }

    // ---------- configuration (fluent) ----------

    /**
     * Sets the XML schema used to validate read and write operations.
     *
     * @param schema schema to apply, or {@code null} to disable schema validation
     * @return this instance for chaining
     */
    public AbstractJaxbConverter<T> withSchema(final @Nullable Schema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Sets the validation event handler invoked on parser and schema events.
     *
     * @param handler handler to apply, or {@code null} to fall back to JAXB defaults
     * @return this instance for chaining
     */
    public AbstractJaxbConverter<T> withEventHandler(final @Nullable ValidationEventHandler handler) {
        this.eventHandler = handler;
        return this;
    }

    /**
     * Enables or disables formatted (indented) marshaller output. Default is {@code true}.
     *
     * @param formattedOutput whether output should be indented
     * @return this instance for chaining
     */
    public AbstractJaxbConverter<T> withFormattedOutput(final boolean formattedOutput) {
        this.formattedOutput = formattedOutput;
        return this;
    }

    /**
     * Sets the encoding declared on marshalled output. Default is {@code UTF-8}.
     *
     * @param encoding character encoding to use
     * @return this instance for chaining
     */
    public AbstractJaxbConverter<T> withEncoding(final Charset encoding) {
        ObjectHelper.requireNonNull(encoding, "encoding");
        this.encoding = encoding;
        return this;
    }

    /**
     * Sets the namespace-URI-to-prefix mapping applied during marshalling.
     * <p>
     * Map keys are namespace URIs; values are the preferred XML prefix for that namespace. A value of empty string
     * marks that URI as the <em>default namespace</em>, i.e. it is declared as {@code xmlns="..."} on the root element
     * and elements in that namespace appear without a prefix.
     * <p>
     * Passing {@code null} or an empty map resets to JAXB's default prefix selection. The map is defensively copied, so
     * later mutations of the supplied instance have no effect.
     *
     * @param map namespace URI &rarr; preferred prefix mapping, or {@code null} to reset
     * @return this instance for chaining
     */
    public AbstractJaxbConverter<T> withNamespacePrefixMap(final @Nullable Map<String, String> map) {
        this.namespacePrefixMap = map == null ? Map.of() : Map.copyOf(map);
        return this;
    }

    /**
     * Returns the JAXB context backing this service.
     *
     * @return the JAXB context
     */
    public JAXBContext getJaxbContext() {
        return this.jaxbContext;
    }

    // ---------- read ----------

    /**
     * Unmarshals XML from a URI.
     *
     * @param uri location of the XML document
     * @return the unmarshalled object
     * @throws JaxbConversionException on I/O, parsing, or binding errors
     */
    public T readXml(final URI uri) {
        ObjectHelper.requireNonNull(uri, "uri");
        return readSecure(new StreamSource(uri.toASCIIString()), "URI " + uri);
    }

    /**
     * Unmarshals XML from an input stream. The stream is not closed by this method.
     *
     * @param input stream to read from
     * @return the unmarshalled object
     * @throws JaxbConversionException on I/O, parsing, or binding errors
     */
    public T readXml(final InputStream input) {
        ObjectHelper.requireNonNull(input, "input");
        return readSecure(new StreamSource(input), "InputStream");
    }

    /**
     * Unmarshals XML from a character reader. The reader is not closed by this method.
     *
     * @param reader reader to read from
     * @return the unmarshalled object
     * @throws JaxbConversionException on I/O, parsing, or binding errors
     */
    public T readXml(final Reader reader) {
        ObjectHelper.requireNonNull(reader, "reader");
        return readSecure(new StreamSource(reader), "Reader");
    }

    /**
     * Unmarshals XML from a byte array.
     *
     * @param xml XML bytes to parse
     * @return the unmarshalled object
     * @throws JaxbConversionException on parsing or binding errors
     */
    public T readXml(final byte[] xml) {
        ObjectHelper.requireNonNull(xml, "xml");
        return readXml(new ByteArrayInputStream(xml));
    }

    /**
     * Unmarshals XML from a string.
     *
     * @param xml XML content to parse
     * @return the unmarshalled object
     * @throws JaxbConversionException on parsing or binding errors
     */
    public T readXml(final String xml) {
        ObjectHelper.requireNonNull(xml, "xml");
        return readXml(new StringReader(xml));
    }

    /**
     * Unmarshals XML from a file path.
     *
     * @param path file to read
     * @return the unmarshalled object
     * @throws JaxbConversionException on I/O, parsing, or binding errors
     */
    public T readXml(final Path path) {
        ObjectHelper.requireNonNull(path, "path");
        try ( InputStream in = Files.newInputStream(path) ) {
            return readSecure(new StreamSource(in, path.toUri().toASCIIString()), "Path " + path);
        } catch (final IOException e) {
            throw new JaxbConversionException("Can not read from path " + path, e);
        }
    }

    /**
     * Unmarshals XML from a file.
     *
     * @param file file to read
     * @return the unmarshalled object
     * @throws JaxbConversionException on I/O, parsing, or binding errors
     */
    public T readXml(final File file) {
        ObjectHelper.requireNonNull(file, "file");
        return readXml(file.toPath());
    }

    /**
     * Unmarshals XML from an arbitrary {@link Source}.
     * <p>
     * Unlike the other {@code readXml} overloads this method does not impose the XXE-safe {@link XMLInputFactory}
     * configuration; callers are responsible for the source's parser settings.
     *
     * @param source XML source
     * @return the unmarshalled object
     * @throws JaxbConversionException on parsing or binding errors
     */
    public T readXml(final Source source) {
        ObjectHelper.requireNonNull(source, "source");
        Objects.requireNonNull(type);
        try {
            final Unmarshaller u = createUnmarshaller();
            return u.unmarshal(source, type).getValue();
        } catch (final JAXBException e) {
            throw new JaxbConversionException("Can not unmarshal to type " + type.getSimpleName(), e);
        }
    }

    // ---------- write ----------

    /**
     * Serializes {@code model} to an XML byte array using the configured encoding.
     *
     * @param model the object to serialize
     * @return the serialized XML bytes
     * @throws JaxbConversionException on marshalling errors
     */
    public byte[] writeXmlBytes(final T model) {
        ObjectHelper.requireNonNull(model, "model");
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream() ) {
            writeXml(model, baos);
            return baos.toByteArray();
        } catch (final IOException e) {
            throw new JaxbConversionException("Error serializing object " + model.getClass().getName(), e);
        }
    }

    /**
     * Serializes {@code model} to an XML string using the configured encoding.
     *
     * @param model the object to serialize
     * @return the serialized XML
     * @throws JaxbConversionException on marshalling errors
     */
    public String writeXml(final T model) {
        ObjectHelper.requireNonNull(model, "model");
        try ( StringWriter sw = new StringWriter() ) {
            writeXml(model, sw);
            return sw.toString();
        } catch (final IOException e) {
            throw new JaxbConversionException("Error serializing object " + model.getClass().getName(), e);
        }
    }

    /**
     * Serializes {@code model} to an output stream. The stream is not closed by this method.
     *
     * @param model the object to serialize
     * @param output destination stream
     * @throws JaxbConversionException on marshalling errors
     */
    public void writeXml(final T model, final OutputStream output) {
        ObjectHelper.requireNonNull(model, "model");
        ObjectHelper.requireNonNull(output, "output");
        writeStreamingWithIntrospection(model, XMLOutputFactory.newFactory()::createXMLStreamWriter, output);
    }

    /**
     * Serializes {@code model} to a character writer. The writer is not closed by this method.
     *
     * @param model the object to serialize
     * @param writer destination writer
     * @throws JaxbConversionException on marshalling errors
     */
    public void writeXml(final T model, final Writer writer) {
        ObjectHelper.requireNonNull(model, "model");
        ObjectHelper.requireNonNull(writer, "writer");
        writeStreamingWithIntrospection(model, XMLOutputFactory.newFactory()::createXMLStreamWriter, writer);
    }

    /**
     * Serializes {@code model} to a JAXP {@link Result}.
     *
     * @param model the object to serialize
     * @param result destination result
     * @throws JaxbConversionException on marshalling errors
     */
    public void writeXml(final T model, final Result result) {
        ObjectHelper.requireNonNull(model, "model");
        ObjectHelper.requireNonNull(result, "result");
        try {
            final Marshaller marshaller = createMarshaller();
            marshaller.marshal(this.jaxbMapper.apply(model), result);
        } catch (final JAXBException e) {
            throw new JaxbConversionException("Error serializing object " + model.getClass().getName(), e);
        }
    }

    /**
     * Serializes {@code model} to a file path. The file is created if missing and truncated otherwise.
     *
     * @param model the object to serialize
     * @param path destination path
     * @throws JaxbConversionException on I/O or marshalling errors
     */
    public void writeXml(final T model, final Path path) {
        ObjectHelper.requireNonNull(model, "model");
        ObjectHelper.requireNonNull(path, "path");
        try ( OutputStream out = Files.newOutputStream(path) ) {
            writeXml(model, out);
        } catch (final IOException e) {
            throw new JaxbConversionException("Can not write to path " + path, e);
        }
    }

    /**
     * Serializes {@code model} to a file. The file is created if missing and truncated otherwise.
     *
     * @param model the object to serialize
     * @param file destination file
     * @throws JaxbConversionException on I/O or marshalling errors
     */
    public void writeXml(final T model, final File file) {
        ObjectHelper.requireNonNull(model, "model");
        ObjectHelper.requireNonNull(file, "file");
        writeXml(model, file.toPath());
    }

    // ---------- internals ----------

    @FunctionalInterface
    private interface XmlStreamWriterFactory<O> {

        XMLStreamWriter create(O output) throws XMLStreamException;
    }

    private <O> void writeStreamingWithIntrospection(final T model, final XmlStreamWriterFactory<O> factory, final O output) {
        try {
            final XMLStreamWriter xmlStreamWriter = factory.create(output);
            final Marshaller marshaller = createMarshaller();
            marshaller.marshal(this.jaxbMapper.apply(model), xmlStreamWriter);
            xmlStreamWriter.flush();
        } catch (final JAXBException | XMLStreamException e) {
            throw new JaxbConversionException("Error serializing object " + model.getClass().getName(), e);
        }
    }

    private Marshaller createMarshaller() throws JAXBException {
        final Marshaller marshaller = this.jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.valueOf(this.formattedOutput));
        marshaller.setProperty(Marshaller.JAXB_ENCODING, this.encoding.name());
        if (this.schema != null) {
            marshaller.setSchema(this.schema);
        }
        if (this.eventHandler != null) {
            marshaller.setEventHandler(this.eventHandler);
        }
        if (!this.namespacePrefixMap.isEmpty()) {
            // The namespacePrefixMapper property is JAXB-implementation-specific (Glassfish/EclipseLink). On other
            // runtimes setProperty will fail; emit a warning rather than aborting the whole marshal.
            try {
                marshaller.setProperty(NAMESPACE_PREFIX_MAPPER_PROPERTY, new MappedNamespacePrefixMapper(this.namespacePrefixMap));
            } catch (final PropertyException e) {
                LOGGER.warn("JAXB runtime does not support the '{}' property; namespace prefix map will be ignored: {}",
                        NAMESPACE_PREFIX_MAPPER_PROPERTY, e.getMessage());
            }
        }
        return marshaller;
    }

    private Unmarshaller createUnmarshaller() throws JAXBException {
        final Unmarshaller u = this.jaxbContext.createUnmarshaller();
        if (this.schema != null) {
            u.setSchema(this.schema);
        }
        if (this.eventHandler != null) {
            u.setEventHandler(this.eventHandler);
        }
        return u;
    }

    private T readSecure(final StreamSource source, final String context) {
        try {
            final XMLInputFactory inputFactory = XmlHelper.createSecureXmlInputFactory();
            final XMLStreamReader xsr = inputFactory.createXMLStreamReader(source);
            final Unmarshaller u = createUnmarshaller();
            return u.unmarshal(xsr, type).getValue();
        } catch (final JAXBException | XMLStreamException e) {
            throw new JaxbConversionException("Can not unmarshal to type " + type.getSimpleName() + " from " + context, e);
        }
    }
}
