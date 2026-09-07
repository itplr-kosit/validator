package org.kosit.schematron.resolve;

import static org.assertj.core.api.Assertions.assertThat;

import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.junit.jupiter.api.Test;
import org.kosit.validator.testdata.TestResources;

/**
 * Tests {@link RemoteResolvingStrategy}.
 *
 * @author Andreas Penski
 */
public class RemoteResolvingStrategyTest {

    @Test
    public void testRemoteSchemaResolving() throws Exception {
        final ResolvingConfigurationStrategy s = new RemoteResolvingStrategy();
        final SchemaFactory schemaFactory = s.createSchemaFactory();
        final Schema schema = schemaFactory.newSchema(TestResources.Resolving.SCHEMA_WITH_REMOTE_REFERENCE.toURL());
        assertThat(schema).isNotNull();
    }

    @Test
    public void testLocalSchemaResolving() throws Exception {
        final ResolvingConfigurationStrategy s = new StrictLocalResolvingStrategy();
        final SchemaFactory schemaFactory = s.createSchemaFactory();
        final Schema schema = schemaFactory.newSchema(TestResources.Resolving.SCHEMA_WITH_REFERENCE.toURL());
        assertThat(schema).isNotNull();
    }

}
