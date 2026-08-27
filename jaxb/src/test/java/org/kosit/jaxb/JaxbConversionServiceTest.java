package org.kosit.jaxb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kosit.jaxb.testtypes.Book;
import org.kosit.jaxb.testtypes.Person;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

public class JaxbConversionServiceTest {

    private AbstractJaxbConverter<Person> service;

    /**
     * Creates a service from a fixed set of JAXB-annotated classes. For testing only
     *
     * @param clazz JAXB-annotated class to include in the JAXB context
     * @param jaxbMapper JAXB mapper
     * @return a new conversion service
     * @throws IllegalArgumentException if no classes are supplied
     * @throws JaxbConversionException if the JAXB context can not be created
     */
    private static <T> AbstractJaxbConverter<T> createJaxbSvc(final Class<T> clazz, final Function<T, JAXBElement<T>> jaxbMapper) {
        Objects.requireNonNull(clazz);
        try {
            return new AbstractJaxbConverter<>(JAXBContext.newInstance(clazz), clazz, jaxbMapper) {
            };
        } catch (final JAXBException e) {
            throw new JaxbConversionException("Can not create JAXB context for: " + clazz, e);
        }
    }

    @BeforeEach
    public void setUp() {
        this.service = createJaxbSvc(Person.class, x -> new JAXBElement<>(new QName(null, "person"), Person.class, x));
    }

    @Test
    public void writeAndReadString() {
        final Person original = new Person("Alice", 30, "alice@example.com");
        final String xml = this.service.writeXml(original);
        assertThat(xml).contains("name=\"Alice\"").contains("<email>alice@example.com</email>");

        final Person parsed = this.service.readXml(xml);
        assertThat(parsed.getName()).isEqualTo("Alice");
        assertThat(parsed.getAge()).isEqualTo(30);
        assertThat(parsed.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    public void writeAndReadBytes() {
        final Person original = new Person("Bob", 25, "bob@example.com");
        final byte[] bytes = this.service.writeXml(original).getBytes(StandardCharsets.UTF_8);
        final Person parsed = this.service.readXml(bytes);
        assertThat(parsed.getName()).isEqualTo("Bob");
    }

    @Test
    public void writeAndReadStreams() throws IOException {
        final Person original = new Person("Carol", 40, "carol@example.com");
        try ( ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
            this.service.writeXml(original, out);
            try ( ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ) {
                final Person parsed = this.service.readXml(in);
                assertThat(parsed.getEmail()).isEqualTo("carol@example.com");
            }
        }
    }

    @Test
    public void writeAndReadWriterReader() {
        final Person original = new Person("Dave", 50, "dave@example.com");
        final StringWriter sw = new StringWriter();
        this.service.writeXml(original, sw);
        final Person parsed = this.service.readXml(new StringReader(sw.toString()));
        assertThat(parsed.getName()).isEqualTo("Dave");
    }

    @Test
    public void writeAndReadSourceResult() {
        final Person original = new Person("Eve", 22, "eve@example.com");
        final StringWriter sw = new StringWriter();
        this.service.writeXml(original, new StreamResult(sw));
        final Person parsed = this.service.readXml(new StreamSource(new StringReader(sw.toString())));
        assertThat(parsed.getAge()).isEqualTo(22);
    }

    @Test
    public void writeAndReadPath(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("person.xml");
        final Person original = new Person("Frank", 65, "frank@example.com");
        this.service.writeXml(original, file);
        assertThat(Files.size(file)).isGreaterThan(0);
        final Person parsed = this.service.readXml(file);
        assertThat(parsed.getName()).isEqualTo("Frank");
    }

    @Test
    public void writeAndReadFile(@TempDir final Path tempDir) {
        final File file = tempDir.resolve("person.xml").toFile();
        final Person original = new Person("Gina", 18, "gina@example.com");
        this.service.writeXml(original, file);
        final Person parsed = this.service.readXml(file);
        assertThat(parsed.getName()).isEqualTo("Gina");
    }

    @Test
    public void writeAndReadUri(@TempDir final Path tempDir) {
        final Path file = tempDir.resolve("person.xml");
        final Person original = new Person("Helen", 33, "helen@example.com");
        this.service.writeXml(original, file);
        final URI uri = file.toUri();
        final Person parsed = this.service.readXml(uri);
        assertThat(parsed.getName()).isEqualTo("Helen");
    }

    @Test
    public void disablesExternalEntities() {
        final String evil = "" + "<?xml version=\"1.0\"?>" + "<!DOCTYPE person [" + "  <!ELEMENT person ANY>"
                + "  <!ENTITY xxe SYSTEM \"file:///etc/passwd\">" + "]>" + "<person name=\"&xxe;\" age=\"0\"/>";
        assertThatThrownBy(() -> this.service.readXml(evil)).isInstanceOf(JaxbConversionException.class);
    }

    @Test
    public void writeNullThrows() {
        assertThatThrownBy(() -> this.service.writeXml(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void readNullSourceThrows() {
        assertThatThrownBy(() -> this.service.readXml((URI) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> this.service.readXml((String) null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> this.service.readXml((byte[]) null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void constructorRejectsNullContext() {
        assertThatThrownBy(() -> new AbstractJaxbConverter<>(null, Person.class, null) {
        }).isInstanceOf(NullPointerException.class);
    }

    // ---------- namespace prefix map ----------

    private static AbstractJaxbConverter<Book> bookSvc() {
        return createJaxbSvc(Book.class, x -> new JAXBElement<>(new QName(Book.NS_BOOK, "book"), Book.class, x));
    }

    @Test
    public void defaultNamespaceMappingSuppressesRootPrefix() {
        final AbstractJaxbConverter<Book> s = bookSvc().withNamespacePrefixMap(Map.of(Book.NS_BOOK, ""));
        final String xml = s.writeXml(new Book("Hamlet", "Shakespeare"));
        // The root must be declared as xmlns="..." and the root element must not carry a prefix.
        assertThat(xml).contains("xmlns=\"" + Book.NS_BOOK + "\"");
        assertThat(xml).contains("<book ").contains("<title>Hamlet</title>");
        // The unmapped author namespace must still get *some* prefix declaration.
        assertThat(xml).contains(Book.NS_AUTHOR);
    }

    @Test
    public void customPrefixIsHonoured() {
        final Map<String, String> map = new LinkedHashMap<>();
        map.put(Book.NS_BOOK, "");
        map.put(Book.NS_AUTHOR, "auth");
        final AbstractJaxbConverter<Book> s = bookSvc().withNamespacePrefixMap(map);
        final String xml = s.writeXml(new Book("Hamlet", "Shakespeare"));
        assertThat(xml).contains("xmlns:auth=\"" + Book.NS_AUTHOR + "\"");
        assertThat(xml).contains("<auth:author>Shakespeare</auth:author>");
    }

    @Test
    public void namespaceMapIsDefensivelyCopied() {
        final Map<String, String> map = new HashMap<>();
        map.put(Book.NS_BOOK, "");
        final AbstractJaxbConverter<Book> s = bookSvc().withNamespacePrefixMap(map);
        // Mutate the original map AFTER configuring the service.
        map.put(Book.NS_AUTHOR, "REMOTECHANGE");
        final String xml = s.writeXml(new Book("Hamlet", "Shakespeare"));
        // The mutation must not leak into the output.
        assertThat(xml).doesNotContain("REMOTECHANGE");
    }

    @Test
    public void nullNamespaceMapResetsToDefault() {
        final AbstractJaxbConverter<Book> s = bookSvc().withNamespacePrefixMap(Map.of(Book.NS_BOOK, "bk"))
                .withNamespacePrefixMap(null);
        final String xml = s.writeXml(new Book("Hamlet", "Shakespeare"));
        // After reset, JAXB picks its own prefixes — but it must not pick the configured "bk".
        assertThat(xml).doesNotContain("xmlns:bk=");
    }
}
