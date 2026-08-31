package org.kosit.xvrl.impl;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;

import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.jaxb.AbstractJaxbConverter;
import org.kosit.xvrl.jaxb.ObjectFactory;
import org.kosit.xvrl.jaxb.XvrlJaxbCreator;
import org.kosit.xvrl.jaxb.XvrlJaxbReader;
import org.kosit.xvrl.jaxb.XvrlReportsType;
import org.kosit.xvrl.model.XvrlReports;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.ValidationEventHandler;

/**
 * Reads and writes the XVRL data model ({@link XvrlReports}) from and to XML.
 * <p>
 * The class is a thin facade around an {@link AbstractJaxbConverter} for the XVRL JAXB model package
 * ({@code org.kosit.xvrl.jaxb}); the conversion between the JAXB model and the data model is performed by
 * {@link XvrlJaxbCreator} and {@link XvrlJaxbReader}.
 *
 * @author Philip Helger
 */
public final class XvrlConverter {

    public static final String XSD_PATH = "/xsd";

    public static final String XVRL_XSD_PATH = XSD_PATH + "/xvrl-1.0.xsd";

    public static final JAXBContext JAXB_CTX;

    public static final String NS_URI = "http://www.xproc.org/ns/xvrl";

    private static final Map<String, String> NS_PREFIX = new HashMap<>();

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), XvrlConverter.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException("Can not create XVRL JAXB context", e);
        }
        NS_PREFIX.put(NS_URI, "");
    }

    /**
     * The JAXB level converter for the generated XVRL model.
     */
    private static final class JaxbConverter extends AbstractJaxbConverter<XvrlReportsType> {

        JaxbConverter() {
            super(JAXB_CTX, XvrlReportsType.class, new ObjectFactory()::createReports);
            withNamespacePrefixMap(NS_PREFIX);
        }
    }

    private static XvrlReportsType toJaxb(final XvrlReports reports) {
        ObjectHelper.requireNonNull(reports, "reports");
        return XvrlJaxbCreator.createReportsType(reports);
    }

    private final JaxbConverter converter = new JaxbConverter();

    /**
     * Creates a new converter for the XVRL data model.
     *
     * @throws IllegalStateException if the JAXB context for the XVRL model package can not be created
     */
    public XvrlConverter() {
    }

    // ---------- configuration (fluent) ----------

    /**
     * Sets the XML schema used to validate read and write operations.
     *
     * @param schema schema to apply, or {@code null} to disable schema validation
     * @return this instance for chaining
     */
    public XvrlConverter withSchema(final @Nullable Schema schema) {
        this.converter.withSchema(schema);
        return this;
    }

    /**
     * Sets the validation event handler invoked on parser and schema events.
     *
     * @param handler handler to apply, or {@code null} to fall back to JAXB defaults
     * @return this instance for chaining
     */
    public XvrlConverter withEventHandler(final @Nullable ValidationEventHandler handler) {
        this.converter.withEventHandler(handler);
        return this;
    }

    /**
     * Enables or disables formatted (indented) marshaller output. Default is {@code true}.
     *
     * @param formattedOutput whether output should be indented
     * @return this instance for chaining
     */
    public XvrlConverter withFormattedOutput(final boolean formattedOutput) {
        this.converter.withFormattedOutput(formattedOutput);
        return this;
    }

    /**
     * Sets the encoding declared on marshalled output. Default is {@code UTF-8}.
     *
     * @param encoding character encoding to use
     * @return this instance for chaining
     */
    public XvrlConverter withEncoding(final Charset encoding) {
        this.converter.withEncoding(encoding);
        return this;
    }

    /**
     * @return the JAXB context backing this converter. Never <code>null</code>.
     */
    public JAXBContext getJaxbContext() {
        return this.converter.getJaxbContext();
    }

    // ---------- read ----------

    public @Nullable XvrlReports readXml(final URI uri) {
        return XvrlJaxbReader.readReports(this.converter.readXml(uri));
    }

    public @Nullable XvrlReports readXml(final InputStream input) {
        return XvrlJaxbReader.readReports(this.converter.readXml(input));
    }

    public @Nullable XvrlReports readXml(final Reader reader) {
        return XvrlJaxbReader.readReports(this.converter.readXml(reader));
    }

    public @Nullable XvrlReports readXml(final byte[] xml) {
        return XvrlJaxbReader.readReports(this.converter.readXml(xml));
    }

    public @Nullable XvrlReports readXml(final String xml) {
        return XvrlJaxbReader.readReports(this.converter.readXml(xml));
    }

    public @Nullable XvrlReports readXml(final Path path) {
        return XvrlJaxbReader.readReports(this.converter.readXml(path));
    }

    public @Nullable XvrlReports readXml(final File file) {
        return XvrlJaxbReader.readReports(this.converter.readXml(file));
    }

    public @Nullable XvrlReports readXml(final Source source) {
        return XvrlJaxbReader.readReports(this.converter.readXml(source));
    }

    // ---------- write ----------

    public byte[] writeXmlBytes(final XvrlReports reports) {
        return this.converter.writeXmlBytes(toJaxb(reports));
    }

    public String writeXml(final XvrlReports reports) {
        return this.converter.writeXml(toJaxb(reports));
    }

    public void writeXml(final XvrlReports reports, final OutputStream output) {
        this.converter.writeXml(toJaxb(reports), output);
    }

    public void writeXml(final XvrlReports reports, final Writer writer) {
        this.converter.writeXml(toJaxb(reports), writer);
    }

    public void writeXml(final XvrlReports reports, final Result result) {
        this.converter.writeXml(toJaxb(reports), result);
    }

    public void writeXml(final XvrlReports reports, final Path path) {
        this.converter.writeXml(toJaxb(reports), path);
    }

    public void writeXml(final XvrlReports reports, final File file) {
        this.converter.writeXml(toJaxb(reports), file);
    }
}
