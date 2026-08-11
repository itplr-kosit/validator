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
package org.kosit.validator.impl.conformatron;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.conformatron.api.model.action.ECTActionType;
import org.conformatron.api.model.action.ECTStepResult;
import org.conformatron.api.model.detection.ECTSeverity;
import org.conformatron.api.model.validation.ECTValidationBaseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.conformatron.ParseDocumentAction.ParseDocumentResult;
import org.kosit.validator.impl.input.ByteArrayInput;

/**
 * Tests the first action built against the conformatron-api (step 2, {@code parse-document}).
 *
 * @author Andreas Schmitz
 */
public class ParseDocumentActionTest {

    private static final String WELLFORMED = "<?xml version=\"1.0\"?><doc><child>content</child></doc>";

    private static final String NOT_WELLFORMED = "<?xml version=\"1.0\"?><doc><child>content</doc>";

    private ParseDocumentAction action;

    @BeforeEach
    public void setup() {
        this.action = new ParseDocumentAction();
    }

    @Test
    public void testActionIdentity() {
        assertThat(this.action.getName()).isEqualTo("parse-document");
        assertThat(this.action.getType()).isEqualTo(ECTActionType.VALIDATOR);
    }

    @Test
    public void testParseWellformed() {
        final byte[] bytes = WELLFORMED.getBytes(StandardCharsets.UTF_8);
        final ParseDocumentResult result = this.action.execute(new ByteArrayInput(bytes, "test.xml", "SHA-256"));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.status()).isEqualTo(ECTStepResult.SUCCESS);
        assertThat(result.parsedSource()).isNotNull();
        assertThat(result.parsedSource().getAsDom()).isNotNull();
        assertThat(result.parsedSource().getAsDom().getDocumentElement().getLocalName()).isEqualTo("doc");
        assertThat(result.parsedSource().getParsedContent()).isSameAs(result.parsedSource().getAsDom());
        assertThat(result.parsedSource().isParsed()).isTrue();
        assertThat(result.parsedSource().getSourceBytes()).isEqualTo(bytes);
        assertThat(result.parsedSource().getHashAlgorithmName()).isEqualTo(SourceDigest.getAlgorithmName());
        assertThat(result.parsedSource().getHashBytes()).isEqualTo(SourceDigest.hashBytes(bytes));
        assertThat(result.parsedSource().getSource().getName()).isEqualTo("test.xml");
        assertThat(result.parsedSource().getSource().getDetectedSyntax()).isEqualTo(ECTValidationBaseType.XML);
        assertThat(result.detections().getCount()).isEqualTo(1);
        assertThat(result.detections().getWorstSeverity()).isEqualTo(ECTSeverity.INFO);
        assertThat(result.detections().getAll().get(0).getCode()).isEqualTo(ParseDocumentAction.CODE_DOCUMENT_PARSED);
    }

    @Test
    public void testParseNotWellformed() {
        final byte[] bytes = NOT_WELLFORMED.getBytes(StandardCharsets.UTF_8);
        final ParseDocumentResult result = this.action.execute(new ByteArrayInput(bytes, "broken.xml", "SHA-256"));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.status()).isEqualTo(ECTStepResult.FAILURE);
        // spec output path 2: source identity retained (bytes + hash), only the parsed content is absent
        assertThat(result.parsedSource()).isNotNull();
        assertThat(result.parsedSource().isParsed()).isFalse();
        assertThat(result.parsedSource().getAsDom()).isNull();
        assertThat(result.parsedSource().getSourceBytes()).isEqualTo(bytes);
        assertThat(result.parsedSource().getHashAlgorithmName()).isEqualTo(SourceDigest.getAlgorithmName());
        assertThat(result.parsedSource().getHashBytes()).isEqualTo(SourceDigest.hashBytes(bytes));
        assertThat(result.detections().containsAtLeastOneError()).isTrue();
        assertThat(result.detections().getWorstSeverity()).isEqualTo(ECTSeverity.FATAL_ERROR);
        assertThat(result.detections().getAll()).allSatisfy(detection -> {
            assertThat(detection.getCode()).isEqualTo(ParseDocumentAction.CODE_NOT_WELLFORMED);
            assertThat(detection.getLocation().getResourceID()).isEqualTo("broken.xml");
            assertThat(detection.getLocation().hasLineNumber()).isTrue();
        });
    }

    @Test
    public void testSourceBytesAreImmutable() {
        final byte[] bytes = WELLFORMED.getBytes(StandardCharsets.UTF_8);
        final ParseDocumentResult result = this.action.execute(new ByteArrayInput(bytes, "test.xml", "SHA-256"));

        final byte[] exposed = result.parsedSource().getSourceBytes();
        exposed[0] = '!';
        assertThat(result.parsedSource().getSourceBytes()).isEqualTo(bytes);
    }

    @Test
    public void testNullInput() {
        assertThrows(IllegalArgumentException.class, () -> this.action.execute(null));
    }
}
