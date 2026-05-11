/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
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

package org.kosit.validator.impl.tasks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.kosit.validator.api.InputFactory.read;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.Helper.Simple;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.model.XMLSyntaxError;

import net.sf.saxon.s9api.XdmNode;

/**
 * Tests the document parsing functionality.
 *
 * @author Andreas Penski
 */
public class DocumentParseActionTest {

    private DocumentParseAction action;

    @BeforeEach
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
        assertThrows(IllegalArgumentException.class, () -> this.action.parseDocument(null));

    }

}
