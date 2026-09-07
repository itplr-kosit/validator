package org.kosit.base.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.StringReader;
import java.net.URI;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class SchemaResolverTest {

    private static final String VALID_XML = """
            <root xmlns="urn:kosit:test:simple"><child>content</child></root>""";

    private static final String INVALID_XML = """
            <root xmlns="urn:kosit:test:simple"><unexpected/></root>""";

    private static URL getResource(final String name) {
        final URL ret = SchemaResolverTest.class.getResource(name);
        assertThat(ret).as(name).isNotNull();
        return ret;
    }

    @Test
    public void resolveKeepsThePathAsSystemId() {
        final URL resource = getResource("/xsd/simple.xsd");
        final Source source = SchemaResolver.resolve(resource);

        assertThat(source).isInstanceOf(StreamSource.class);
        assertThat(source.getSystemId()).endsWith("/xsd/simple.xsd");
        assertThat(((StreamSource) source).getInputStream()).isNotNull();
    }

    @Test
    public void resolveRejectsNull() {
        assertThatThrownBy(() -> SchemaResolver.resolve(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void resolveFailsForAMissingResource() throws Exception {
        final URL missing = URI.create("file:/does/not/exist.xsd").toURL();
        assertThatThrownBy(() -> SchemaResolver.resolve(missing)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can not load schema for resource");
    }

    @Test
    public void createParsedSchemaFromUrl() throws Exception {
        final Schema schema = SchemaResolver.createParsedSchema(getResource("/xsd/simple.xsd"));
        assertThat(schema).isNotNull();

        assertThatCode(() -> schema.newValidator().validate(new StreamSource(new StringReader(VALID_XML)))).doesNotThrowAnyException();
        assertThatThrownBy(() -> schema.newValidator().validate(new StreamSource(new StringReader(INVALID_XML))))
                .isInstanceOf(SAXException.class);
    }

    @Test
    public void createParsedSchemaFromSources() {
        final Source source = SchemaResolver.resolve(getResource("/xsd/simple.xsd"));
        assertThat(SchemaResolver.createParsedSchema(new Source[] { source })).isNotNull();
    }

    @Test
    public void createParsedSchemaFailsForABrokenSchema() {
        final URL resource = getResource("/xsd/not-wellformed.xsd");
        assertThatThrownBy(() -> SchemaResolver.createParsedSchema(resource)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Can not load schema from sources").hasCauseInstanceOf(SAXException.class);
    }
}
