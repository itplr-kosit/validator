package org.kosit.schematron.resolve;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.Test;
import org.kosit.validator.testdata.TestResources;
import org.xml.sax.SAXParseException;

/**
 * Tests {@link StrictRelativeResolvingStrategy}.
 *
 * @author Andreas Penski
 */
public class StrictRelativeResolvingTest {

    @Test
    public void testRemoteSchemaResolving() throws Exception {
        final ResolvingConfigurationStrategy s = new StrictLocalResolvingStrategy();
        final SchemaFactory schemaFactory = s.createSchemaFactory();
        final Throwable t = assertThrows(SAXParseException.class,
                () -> schemaFactory.newSchema(TestResources.Resolving.SCHEMA_WITH_REMOTE_REFERENCE.toURL()));
        assertThat(t.getMessage()).contains("schema_reference");
    }

    @Test
    public void testLocalSchemaResolving() throws Exception {
        final ResolvingConfigurationStrategy s = new StrictLocalResolvingStrategy();
        final SchemaFactory schemaFactory = s.createSchemaFactory();
        final Schema schema = schemaFactory.newSchema(TestResources.Resolving.SCHEMA_WITH_REFERENCE.toURL());
        assertThat(schema).isNotNull();
    }

    // TODO loading schema from location outside of the repository - this is still possible yet
}
