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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.conformatron.api.model.action.CTActionType;
import org.conformatron.api.model.action.CTStepResult;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.conformatron.api.model.source.CTResource;
import org.conformatron.api.model.validation.CTSyntax;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kosit.validator.helper.ResourceHelperExtension;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXMLResult;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.XMLDetection;
import org.kosit.validator.impl.conformatron.source.ReadResource;
import org.kosit.validator.impl.conformatron.source.Resource;

/**
 * Tests the first action built against the conformatron-api (step 2, {@code parse-document}).
 *
 * @author Andreas Schmitz
 */
public class ParseXMLActionTest {

    private static final String WELLFORMED = "<?xml version=\"1.0\"?><doc><child>content</child></doc>";

    private static final String NOT_WELLFORMED = "<?xml version=\"1.0\"?><doc><child>content</doc>";

    @RegisterExtension
    private final ResourceHelperExtension resHelper = new ResourceHelperExtension();

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
    public void testParseWellformed() throws IOException {
        final CTResource res = Resource.utf8("test.xml", WELLFORMED);
        assertNotNull(res);

        final ReadResource readRes = ReadResource.of(res, resHelper.get());
        assertNotNull(readRes);

        final ParseXMLResult result = this.action.execute(readRes);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getResult()).isSameAs(CTStepResult.SUCCESS);
        assertThat(result.getParsedSource()).isNotNull();
        assertThat(result.getParsedSource().getAsDom()).isNotNull();
        assertThat(result.getParsedSource().getAsDom().getDocumentElement().getLocalName()).isEqualTo("doc");
        assertThat(result.getParsedSource().getParsedContent()).isSameAs(result.getParsedSource().getAsDom());
        assertThat(result.getParsedSource().isParsed()).isTrue();
        assertThat(result.getParsedSource().getSource().getName()).isEqualTo("test.xml");
        assertThat(result.getParsedSource().getSource().getDetectedSyntax()).isEqualTo(CTSyntax.XML);
        assertThat(result.getDetectionList().getCount()).isEqualTo(1);
        assertThat(result.getDetectionList().getWorstSeverity()).isEqualTo(CTStandardSeverity.NONE);
        assertThat(result.getDetectionList().getAll().get(0).getCode()).isEqualTo(XMLDetection.CODE_DOCUMENT_PARSED);
    }

    @Test
    public void testParseNotWellformed() throws IOException {
        final CTResource res = Resource.utf8("broken.xml", NOT_WELLFORMED);
        assertNotNull(res);

        final ReadResource readRes = ReadResource.of(res, resHelper.get());
        assertNotNull(readRes);

        final ParseXMLResult result = this.action.execute(readRes);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getResult()).isEqualTo(CTStepResult.FAILURE);
        // spec output path 2: source identity retained (bytes + hash), only the parsed content is absent
        assertThat(result.getParsedSource()).isNotNull();
        assertThat(result.getParsedSource().isParsed()).isFalse();
        assertThat(result.getParsedSource().getAsDom()).isNull();
        assertThat(result.getDetectionList().containsAtLeastOneError()).isTrue();
        assertThat(result.getDetectionList().getWorstSeverity()).isEqualTo(CTStandardSeverity.ERROR);
        assertThat(result.getDetectionList().getAll()).allSatisfy(detection -> {
            assertThat(detection.getCode()).isEqualTo(XMLDetection.CODE_NOT_WELLFORMED);
            assertThat(detection.getLocation().getResourceId()).isEqualTo("broken.xml");
            assertThat(detection.getLocation().hasLineNumber()).isTrue();
        });
    }

    @Test
    public void testNullInput() {
        assertThrows(NullPointerException.class, () -> this.action.execute(null));
    }
}
