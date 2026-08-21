/*
 * Copyright 2017-2026  Koordinierungsstelle für IT-Standards (KoSIT)
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
package org.kosit.validator.impl.conformatron.action;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.validation.CTValidationSyntax;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLResult;
import org.kosit.validator.impl.conformatron.util.SourceDigest;
import org.kosit.validator.impl.input.ByteArrayVInput;

/**
 * Tests the first action built against the conformatron-api (step 2, {@code parse-document}).
 *
 * @author Andreas Schmitz
 */
public class ParseXMLActionTest {

    private static final String WELLFORMED = "<?xml version=\"1.0\"?><doc><child>content</child></doc>";

    private static final String NOT_WELLFORMED = "<?xml version=\"1.0\"?><doc><child>content</doc>";

    private ParseXMLAction action;

    @BeforeEach
    public void setup() {
        this.action = new ParseXMLAction();
    }

    @Test
    public void testActionIdentity() {
        assertThat(this.action.getName()).isEqualTo("ParseXML");
        assertThat(this.action.getType()).isEqualTo(CTActionType.PARSE_DOCUMENT);
    }

    @Test
    public void testParseWellformed() {
        final byte[] bytes = WELLFORMED.getBytes(StandardCharsets.UTF_8);
        final ParseXMLResult result = this.action.execute(new ByteArrayVInput(bytes, "test.xml", "SHA-256"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isSameAs(CTStepResult.SUCCESS);
        assertThat(result.getParsedSource()).isNotNull();
        assertThat(result.getParsedSource().getAsDom()).isNotNull();
        assertThat(result.getParsedSource().getAsDom().getDocumentElement().getLocalName()).isEqualTo("doc");
        assertThat(result.getParsedSource().getParsedContent()).isSameAs(result.getParsedSource().getAsDom());
        assertThat(result.getParsedSource().isParsed()).isTrue();
        assertThat(result.getParsedSource().getSourceBytes()).isEqualTo(bytes);
        assertThat(result.getParsedSource().getHashAlgorithmName()).isEqualTo(SourceDigest.getAlgorithmName());
        assertThat(result.getParsedSource().getHashBytes()).isEqualTo(SourceDigest.hashBytes(bytes));
        assertThat(result.getParsedSource().getSource().getName()).isEqualTo("test.xml");
        assertThat(result.getParsedSource().getSource().getDetectedSyntax()).isEqualTo(CTValidationSyntax.XML);
        assertThat(result.getDetectionList().getCount()).isEqualTo(1);
        assertThat(result.getDetectionList().getWorstSeverity()).isEqualTo(CTStandardSeverity.NONE);
        assertThat(result.getDetectionList().getAll().get(0).getCode()).isEqualTo(ParseXMLAction.CODE_DOCUMENT_PARSED);
    }

    @Test
    public void testParseNotWellformed() {
        final byte[] bytes = NOT_WELLFORMED.getBytes(StandardCharsets.UTF_8);
        final ParseXMLResult result = this.action.execute(new ByteArrayVInput(bytes, "broken.xml", "SHA-256"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getResult()).isEqualTo(CTStepResult.FAILURE);
        // spec output path 2: source identity retained (bytes + hash), only the parsed content is absent
        assertThat(result.getParsedSource()).isNotNull();
        assertThat(result.getParsedSource().isParsed()).isFalse();
        assertThat(result.getParsedSource().getAsDom()).isNull();
        assertThat(result.getParsedSource().getSourceBytes()).isEqualTo(bytes);
        assertThat(result.getParsedSource().getHashAlgorithmName()).isEqualTo(SourceDigest.getAlgorithmName());
        assertThat(result.getParsedSource().getHashBytes()).isEqualTo(SourceDigest.hashBytes(bytes));
        assertThat(result.getDetectionList().containsAtLeastOneError()).isTrue();
        assertThat(result.getDetectionList().getWorstSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(result.getDetectionList().getAll()).allSatisfy(detection -> {
            assertThat(detection.getCode()).isEqualTo(ParseXMLAction.CODE_NOT_WELLFORMED);
            assertThat(detection.getLocation().getResourceId()).isEqualTo("broken.xml");
            assertThat(detection.getLocation().hasLineNumber()).isTrue();
        });
    }

    @Test
    public void testSourceBytesAreImmutable() {
        final byte[] bytes = WELLFORMED.getBytes(StandardCharsets.UTF_8);
        final ParseXMLResult result = this.action.execute(new ByteArrayVInput(bytes, "test.xml", "SHA-256"));

        final byte[] exposed = result.getParsedSource().getSourceBytes();
        exposed[0] = '!';
        assertThat(result.getParsedSource().getSourceBytes()).isEqualTo(bytes);
    }

    @Test
    public void testNullInput() {
        assertThrows(NullPointerException.class, () -> this.action.execute(null));
    }
}
