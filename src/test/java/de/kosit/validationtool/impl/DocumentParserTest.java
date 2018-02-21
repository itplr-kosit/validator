/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl;

import static de.kosit.validationtool.api.InputFactory.read;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;

import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.DocumentParseAction;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;

/**
 * @author Andreas Penski
 */
public class DocumentParserTest {

    private static final URL CONTENT = ConversionServiceTest.class.getResource("/valid/scenarios.xml");

    private static final URL ILLFORMED = ConversionServiceTest.class.getResource("/invalid/scenarios-illformed.xml");

    private static final URL NOT_EXISTING = ConversionServiceTest.class.getResource("/does not exist.xml");



    @Rule
    public ExpectedException exception = ExpectedException.none();

    private DocumentParseAction parser;

    @Before
    public void setup() {
        parser = new DocumentParseAction();
    }

    @Test
    public void testSimple() throws IOException {
        final Result<Document, XMLSyntaxError> result = parser.parseDocument(read(CONTENT));
        assertThat(result).isNotNull();
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.isValid()).isTrue();
    }


    @Test
    public void testIllformed() throws IOException {
        final Result<Document, XMLSyntaxError> result = parser.parseDocument(read(ILLFORMED));
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getObject()).isNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testNullInput() {
        exception.expect(IllegalArgumentException.class);
        parser.parseDocument(null);

    }


}
