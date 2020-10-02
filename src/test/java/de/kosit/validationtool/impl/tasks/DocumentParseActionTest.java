/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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

package de.kosit.validationtool.impl.tasks;

import static de.kosit.validationtool.api.InputFactory.read;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;

import net.sf.saxon.s9api.XdmNode;

/**
 * Testet die Document Parsing-Funktionalitäten.
 *
 * @author Andreas Penski
 */
public class DocumentParseActionTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private DocumentParseAction action;

    @Before
    public void setup() {
        this.action = new DocumentParseAction(Helper.createProcessor());
    }

    @Test
    public void testSimple() {
        final Result<XdmNode, XMLSyntaxError> result = this.action.parseDocument(read(Simple.SIMPLE_VALID));
        assertThat(result).isNotNull();
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testIllformed() {
        final Result<XdmNode, XMLSyntaxError> result = this.action.parseDocument(read(Simple.NOT_WELLFORMED));
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getObject()).isNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    public void testNullInput() {
        this.exception.expect(IllegalArgumentException.class);
        this.action.parseDocument(null);

    }

}
