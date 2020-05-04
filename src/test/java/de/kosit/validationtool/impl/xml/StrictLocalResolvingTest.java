package de.kosit.validationtool.impl.xml;

import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.SAXParseException;

import de.kosit.validationtool.api.ResolvingConfigurationStrategy;
import de.kosit.validationtool.impl.Helper.Resolving;

/**
 * Tests {@link StrictLocalResolvingStrategy}
 * 
 * @author Andreas Penski
 */
public class StrictLocalResolvingTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testRemoteSchemaResolving() throws Exception {
        this.expectedException.expect(SAXParseException.class);
        this.expectedException.expectMessage(Matchers.containsString("schema_reference"));
        final ResolvingConfigurationStrategy s = new StrictLocalResolvingStrategy();
        final SchemaFactory schemaFactory = s.createSchemaFactory();
        schemaFactory.newSchema(Resolving.SCHEMA_WITH_REMOTE_REFERENCE.toURL());
    }

    @Test
    public void testLocalSchemaResolving() throws Exception {
        final ResolvingConfigurationStrategy s = new StrictLocalResolvingStrategy();
        final SchemaFactory schemaFactory = s.createSchemaFactory();
        final Schema schema = schemaFactory.newSchema(Resolving.SCHEMA_WITH_REFERENCE.toURL());
        assertThat(schema).isNotNull();
    }
}
