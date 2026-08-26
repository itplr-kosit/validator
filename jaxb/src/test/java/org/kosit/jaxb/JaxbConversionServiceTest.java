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

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kosit.jaxb.testtypes.Book;
import org.kosit.jaxb.testtypes.Person;

public class JaxbConversionServiceTest {

    private JaxbConversionService service;

    @BeforeEach
    public void setUp() {
        this.service = JaxbConversionService.forClasses(Person.class);
    }

    @Test
    public void writeAndReadString() {
        final Person original = new Person("Alice", 30, "alice@example.com");
        final String xml = this.service.writeXml(original);
        assertThat(xml).contains("name=\"Alice\"").contains("<email>alice@example.com</email>");

        final Person parsed = this.service.readXml(xml, Person.class);
        assertThat(parsed.getName()).isEqualTo("Alice");
        assertThat(parsed.getAge()).isEqualTo(30);
        assertThat(parsed.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    public void writeAndReadBytes() {
        final Person original = new Person("Bob", 25, "bob@example.com");
        final byte[] bytes = this.service.writeXml(original).getBytes(StandardCharsets.UTF_8);
        final Person parsed = this.service.readXml(bytes, Person.class);
        assertThat(parsed.getName()).isEqualTo("Bob");
    }

    @Test
    public void writeAndReadStreams() throws IOException {
        final Person original = new Person("Carol", 40, "carol@example.com");
        try ( ByteArrayOutputStream out = new ByteArrayOutputStream() ) {
            this.service.writeXml(original, out);
            try ( ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ) {
                final Person parsed = this.service.readXml(in, Person.class);
                assertThat(parsed.getEmail()).isEqualTo("carol@example.com");
            }
        }
    }

    @Test
    public void writeAndReadWriterReader() {
        final Person original = new Person("Dave", 50, "dave@example.com");
        final StringWriter sw = new StringWriter();
        this.service.writeXml(original, sw);
        final Person parsed = this.service.readXml(new StringReader(sw.toString()), Person.class);
        assertThat(parsed.getName()).isEqualTo("Dave");
    }

    @Test
    public void writeAndReadSourceResult() {
        final Person original = new Person("Eve", 22, "eve@example.com");
        final StringWriter sw = new StringWriter();
        this.service.writeXml(original, new StreamResult(sw));
        final Person parsed = this.service.readXml(new StreamSource(new StringReader(sw.toString())), Person.class);
        assertThat(parsed.getAge()).isEqualTo(22);
    }

    @Test
    public void writeAndReadPath(@TempDir final Path tempDir) throws IOException {
        final Path file = tempDir.resolve("person.xml");
        final Person original = new Person("Frank", 65, "frank@example.com");
        this.service.writeXml(original, file);
        assertThat(Files.size(file)).isGreaterThan(0);
        final Person parsed = this.service.readXml(file, Person.class);
        assertThat(parsed.getName()).isEqualTo("Frank");
    }

    @Test
    public void writeAndReadFile(@TempDir final Path tempDir) {
        final File file = tempDir.resolve("person.xml").toFile();
        final Person original = new Person("Gina", 18, "gina@example.com");
        this.service.writeXml(original, file);
        final Person parsed = this.service.readXml(file, Person.class);
        assertThat(parsed.getName()).isEqualTo("Gina");
    }

    @Test
    public void writeAndReadUri(@TempDir final Path tempDir) {
        final Path file = tempDir.resolve("person.xml");
        final Person original = new Person("Helen", 33, "helen@example.com");
        this.service.writeXml(original, file);
        final URI uri = file.toUri();
        final Person parsed = this.service.readXml(uri, Person.class);
        assertThat(parsed.getName()).isEqualTo("Helen");
    }

    @Test
    public void disablesExternalEntities() {
        final String evil = "" + "<?xml version=\"1.0\"?>" + "<!DOCTYPE person [" + "  <!ELEMENT person ANY>"
                + "  <!ENTITY xxe SYSTEM \"file:///etc/passwd\">" + "]>" + "<person name=\"&xxe;\" age=\"0\"/>";
        assertThatThrownBy(() -> this.service.readXml(evil, Person.class)).isInstanceOf(JaxbConversionException.class);
    }

    @Test
    public void writeNullThrows() {
        assertThatThrownBy(() -> this.service.writeXml(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void readNullSourceThrows() {
        assertThatThrownBy(() -> this.service.readXml((URI) null, Person.class)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> this.service.readXml((String) null, Person.class)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> this.service.readXml((byte[]) null, Person.class)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void readNullTypeThrows() {
        assertThatThrownBy(() -> this.service.readXml("<person/>", null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void constructorRejectsNullContext() {
        assertThatThrownBy(() -> new JaxbConversionService(null)).isInstanceOf(NullPointerException.class);
    }

    // ---------- namespace prefix map ----------

    @Test
    public void defaultNamespaceMappingSuppressesRootPrefix() {
        final JaxbConversionService s = JaxbConversionService.forClasses(Book.class).withNamespacePrefixMap(Map.of(Book.NS_BOOK, ""));
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
        final JaxbConversionService s = JaxbConversionService.forClasses(Book.class).withNamespacePrefixMap(map);
        final String xml = s.writeXml(new Book("Hamlet", "Shakespeare"));
        assertThat(xml).contains("xmlns:auth=\"" + Book.NS_AUTHOR + "\"");
        assertThat(xml).contains("<auth:author>Shakespeare</auth:author>");
    }

    @Test
    public void namespaceMapIsDefensivelyCopied() {
        final Map<String, String> map = new HashMap<>();
        map.put(Book.NS_BOOK, "");
        final JaxbConversionService s = JaxbConversionService.forClasses(Book.class).withNamespacePrefixMap(map);
        // Mutate the original map AFTER configuring the service.
        map.put(Book.NS_AUTHOR, "REMOTECHANGE");
        final String xml = s.writeXml(new Book("Hamlet", "Shakespeare"));
        // The mutation must not leak into the output.
        assertThat(xml).doesNotContain("REMOTECHANGE");
    }

    @Test
    public void nullNamespaceMapResetsToDefault() {
        final JaxbConversionService s = JaxbConversionService.forClasses(Book.class).withNamespacePrefixMap(Map.of(Book.NS_BOOK, "bk"))
                .withNamespacePrefixMap(null);
        final String xml = s.writeXml(new Book("Hamlet", "Shakespeare"));
        // After reset, JAXB picks its own prefixes — but it must not pick the configured "bk".
        assertThat(xml).doesNotContain("xmlns:bk=");
    }
}
