package org.kosit.validator.impl.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.Test;
import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.kosit.validator.impl.Helper.Resolving;
import org.xml.sax.SAXParseException;

/**
 * Tests {@link StrictLocalResolvingStrategy}
 * 
 * @author Andreas Penski
 */
public class StrictLocalResolvingTest {

    @Test
    public void testRemoteSchemaResolving() throws Exception {
        final ResolvingConfigurationStrategy s = new StrictLocalResolvingStrategy();
        final SchemaFactory schemaFactory = s.createSchemaFactory();
        final Throwable t = assertThrows(SAXParseException.class,
                () -> schemaFactory.newSchema(Resolving.SCHEMA_WITH_REMOTE_REFERENCE.toURL()));
        assertThat(t.getMessage()).contains("schema_reference");
    }

    @Test
    public void testLocalSchemaResolving() throws Exception {
        final ResolvingConfigurationStrategy s = new StrictLocalResolvingStrategy();
        final SchemaFactory schemaFactory = s.createSchemaFactory();
        final Schema schema = schemaFactory.newSchema(Resolving.SCHEMA_WITH_REFERENCE.toURL());
        assertThat(schema).isNotNull();
    }
}
