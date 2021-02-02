/*
 * Copyright 2017-2021  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Tests {@link StrictRelativeResolvingStrategy}.
 * 
 * @author Andreas Penski
 */
public class StrictRelativeResolvingTest {

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

    // TODO loading schema from location outside of the repository - this is still possible yet
}
