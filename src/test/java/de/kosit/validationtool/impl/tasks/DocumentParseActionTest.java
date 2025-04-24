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

package de.kosit.validationtool.impl.tasks;

import de.kosit.validationtool.impl.Helper;
import de.kosit.validationtool.impl.Helper.Simple;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;
import net.sf.saxon.s9api.XdmNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.kosit.validationtool.api.InputFactory.read;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testet die Document Parsing-Funktionalitäten.
 *
 * @author Andreas Penski
 */
class DocumentParseActionTest {

    private DocumentParseAction action;

    @BeforeEach
    void setup() {
        this.action = new DocumentParseAction(Helper.createProcessor());
    }

    @Test
    void simple() {
        final Result<XdmNode, XMLSyntaxError> result = this.action.parseDocument(read(Simple.SIMPLE_VALID));
        assertThat(result).isNotNull();
        assertThat(result.getObject()).isNotNull();
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void illFormed() {
        final Result<XdmNode, XMLSyntaxError> result = this.action.parseDocument(read(Simple.NOT_WELL_FORMED));
        assertThat(result).isNotNull();
        assertThat(result.getErrors()).isNotEmpty();
        assertThat(result.getObject()).isNull();
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void nullInput() {
        assertThrows(IllegalArgumentException.class, () -> this.action.parseDocument(null));
    }
}
