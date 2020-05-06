package de.kosit.validationtool.impl.xml;

import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.kosit.validationtool.api.ResolvingConfigurationStrategy;
import de.kosit.validationtool.impl.Helper.Resolving;

/**
 * Tests {@link RemoteResolvingStrategy}.
 * 
 * @author Andreas Penski
 */
public class RemoteResolvingStrategyTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testRemoteSchemaResolving() throws Exception {
        final ResolvingConfigurationStrategy s = new RemoteResolvingStrategy();
        final SchemaFactory schemaFactory = s.createSchemaFactory();
        final Schema schema = schemaFactory.newSchema(Resolving.SCHEMA_WITH_REMOTE_REFERENCE.toURL());
        assertThat(schema).isNotNull();
    }

    @Test
    public void testLocalSchemaResolving() throws Exception {
        final ResolvingConfigurationStrategy s = new StrictLocalResolvingStrategy();
        final SchemaFactory schemaFactory = s.createSchemaFactory();
        final Schema schema = schemaFactory.newSchema(Resolving.SCHEMA_WITH_REFERENCE.toURL());
        assertThat(schema).isNotNull();
    }

}
