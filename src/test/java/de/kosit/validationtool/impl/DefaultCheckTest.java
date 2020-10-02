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

package de.kosit.validationtool.impl;

import static de.kosit.validationtool.api.InputFactory.read;
import static de.kosit.validationtool.impl.Helper.Simple.FOO_SCHEMATRON_INVALID;
import static de.kosit.validationtool.impl.Helper.Simple.GARBAGE;
import static de.kosit.validationtool.impl.Helper.Simple.NOT_WELLFORMED;
import static de.kosit.validationtool.impl.Helper.Simple.REJECTED;
import static de.kosit.validationtool.impl.Helper.Simple.SCHEMATRON_INVALID;
import static de.kosit.validationtool.impl.Helper.Simple.SIMPLE_VALID;
import static de.kosit.validationtool.impl.Helper.Simple.UNKNOWN;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import de.kosit.validationtool.api.AcceptRecommendation;
import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.impl.Helper.Simple;

import net.sf.saxon.s9api.XdmNode;

/**
 * Test das Check-Interface
 * 
 * @author Andreas Penski
 */
public class DefaultCheckTest {

    public static final int MULTI_COUNT = 5;

    private DefaultCheck validCheck;

    // for checking certain error scenarios.
    private DefaultCheck errorCheck;

    private DefaultCheck jarScenarioCheck;

    @Before
    public void setup() throws URISyntaxException {
        final Configuration validConfig = Configuration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI).build();
        this.validCheck = new DefaultCheck(validConfig);

        final Configuration errorConfig = Configuration.load(Simple.ERROR_SCENARIOS, Simple.REPOSITORY_URI).build();
        this.errorCheck = new DefaultCheck(errorConfig);

        final Configuration jarConfig = Configuration
                .load(requireNonNull(DefaultCheckTest.class.getClassLoader().getResource("simple/packaged/scenarios.xml")).toURI(),
                        requireNonNull(DefaultCheckTest.class.getClassLoader().getResource("simple/packaged/repository/")).toURI())
                .build();

        this.jarScenarioCheck = new DefaultCheck(jarConfig);
    }

    @Test
    public void testHappyCase() {
        final Result doc = this.validCheck.checkInput(read(SIMPLE_VALID));
        assertThat(doc).isNotNull();
        assertThat(doc.getReport()).isNotNull();
        assertThat(doc.isAcceptable()).isTrue();
        assertThat(doc.isSchematronValid()).isTrue();
        assertThat(doc.isSchemaValid()).isTrue();
        assertThat(doc.getFailedAsserts()).isEmpty();
        assertThat(doc.getSchematronResult()).isNotEmpty();
        assertThat(doc.getSchematronResult()).hasSize(1);
        assertThat(doc.getSchematronResult().get(0).getActivePatterns()).isNotEmpty();
        assertThat(doc.getSchematronResult().get(0).getFiredRules()).isNotEmpty();
        assertThat(doc.getSchematronResult().get(0).hasFailedAsserts()).isFalse();
        assertThat(doc.getSchematronResult().get(0).getFailedAsserts()).isEmpty();
        assertThat(doc.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testJarCase() {
        final Result doc = this.jarScenarioCheck.checkInput(read(SIMPLE_VALID));
        assertThat(doc).isNotNull();
        assertThat(doc.getReport()).isNotNull();
        assertThat(doc.isAcceptable()).isTrue();
        assertThat(doc.isSchematronValid()).isTrue();
        assertThat(doc.isSchemaValid()).isTrue();
        assertThat(doc.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testWithoutAcceptMatch() {
        final Result doc = this.validCheck.checkInput(read(Simple.FOO));
        assertThat(doc).isNotNull();
        assertThat(doc.getReport()).isNotNull();
        assertThat(doc.isAcceptable()).isTrue();
        assertThat(doc.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testHappyCaseDocument() {
        final Document doc = this.validCheck.check(read(SIMPLE_VALID));
        assertThat(doc).isNotNull();
    }

    @Test
    public void testMultipleCase() {
        final List<Input> input = IntStream.range(0, MULTI_COUNT).mapToObj(i -> read(SIMPLE_VALID)).collect(Collectors.toList());
        final List<Result> docs = this.validCheck.checkInput(input);
        assertThat(docs).hasSize(MULTI_COUNT);
    }

    @Test
    public void testMultipleCaseDocument() {
        final List<Input> input = IntStream.range(0, MULTI_COUNT).mapToObj(i -> read(SIMPLE_VALID)).collect(Collectors.toList());
        final List<Document> docs = this.validCheck.check(input);
        assertThat(docs).hasSize(MULTI_COUNT);
    }

    @Test
    public void testExtractHtml() {
        final DefaultResult doc = (DefaultResult) this.validCheck.checkInput(read(SIMPLE_VALID));
        assertThat(doc).isNotNull();
        assertThat(doc.getReport()).isNotNull();
        assertThat(doc.isAcceptable()).isTrue();
        assertThat(doc.extractHtmlAsString()).isNotEmpty();
        assertThat(doc.extractHtmlAsElement()).isNotEmpty();
        assertThat(doc.extractHtml()).isNotEmpty();
    }

    @Test
    public void testGarbage() {
        final Result result = this.validCheck.checkInput(read(GARBAGE));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isFalse();
        assertThat(result.isSchemaValid()).isFalse();
        assertThat(result.isProcessingSuccessful()).isFalse();
    }

    @Test
    public void testNoScenario() {
        final Result result = this.validCheck.checkInput(read(UNKNOWN));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isTrue();
        assertThat(result.isProcessingSuccessful()).isTrue();
        assertThat(result.isSchemaValid()).isFalse();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);
        assertThat(result.isAcceptable()).isFalse();
    }

    @Test
    public void testNotWellFormed() {
        final Result result = this.validCheck.checkInput(read(NOT_WELLFORMED));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isFalse();
        assertThat(result.isSchemaValid()).isFalse();
        assertThat(result.isProcessingSuccessful()).isFalse();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getReportDocument()).isNotNull();
    }

    @Test
    public void testRejectAcceptMatch() {
        final Result result = this.validCheck.checkInput(read(REJECTED));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isTrue();
        assertThat(result.isSchemaValid()).isTrue();
        assertThat(result.isProcessingSuccessful()).isTrue();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);
        assertThat(result.isAcceptable()).isFalse();
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getReportDocument()).isNotNull();
    }

    @Test
    public void testSchematronFailed() {
        final Result result = this.validCheck.checkInput(read(SCHEMATRON_INVALID));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isTrue();
        assertThat(result.isSchemaValid()).isTrue();
        assertThat(result.getFailedAsserts()).isNotEmpty();
        assertThat(result.isSchematronValid()).isFalse();
        assertThat(result.getSchematronResult().get(0).findFailedAssert("content-1")).isPresent();
        assertThat(result.isProcessingSuccessful()).isTrue();
        // acceptMatch overules schematron!!!
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.ACCEPTABLE);
        assertThat(result.isAcceptable()).isTrue();
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getReportDocument()).isNotNull();

    }

    @Test
    public void testSchematronFailedWithoutAcceptMatch() {
        final Result result = this.validCheck.checkInput(read(FOO_SCHEMATRON_INVALID));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isTrue();
        assertThat(result.isSchemaValid()).isTrue();
        result.getFailedAsserts();
        assertThat(result.isSchematronValid()).isFalse();
        assertThat(result.getFailedAsserts()).isNotEmpty();
        assertThat(result.isProcessingSuccessful()).isTrue();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);
        assertThat(result.isAcceptable()).isFalse();
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getReportDocument()).isNotNull();
    }

    @Test
    public void testSchematronExecutionError() {
        final Result result = this.errorCheck.checkInput(read(SIMPLE_VALID));
        assertThat(result).isNotNull();
        assertThat(result.isProcessingSuccessful()).isFalse();
        assertThat(result.isSchematronValid()).isFalse();
        assertThat(result.isSchemaValid()).isTrue();
        assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.UNDEFINED);
        assertThat(result.isAcceptable()).isFalse();
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getProcessingErrors()).hasSize(1);
    }

    @Test
    public void testXdmNode() throws Exception {
        XdmNode node = TestObjectFactory.createProcessor().newDocumentBuilder().build(new StreamSource(SIMPLE_VALID.toASCIIString()));
        Input domInput = InputFactory.read(node, "node test");
        Result result = this.validCheck.checkInput(domInput);
        assertThat(result.isProcessingSuccessful()).isEqualTo(true);

        // test compatible configuration
        node = this.validCheck.getConfiguration().getContentRepository().getProcessor().newDocumentBuilder()
                .build(new StreamSource(SIMPLE_VALID.toASCIIString()));
        domInput = InputFactory.read(node, "node test");
        result = this.validCheck.checkInput(domInput);
        assertThat(result.isProcessingSuccessful()).isEqualTo(true);
    }
}
