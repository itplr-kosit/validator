package org.kosit.validator.impl;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.api.VInputFactory.read;
import static org.kosit.validator.impl.TestHelper.Simple.FOO_SCHEMATRON_INVALID;
import static org.kosit.validator.impl.TestHelper.Simple.GARBAGE;
import static org.kosit.validator.impl.TestHelper.Simple.NOT_WELLFORMED;
import static org.kosit.validator.impl.TestHelper.Simple.REJECTED;
import static org.kosit.validator.impl.TestHelper.Simple.SCHEMATRON_INVALID;
import static org.kosit.validator.impl.TestHelper.Simple.SIMPLE_VALID;
import static org.kosit.validator.impl.TestHelper.Simple.UNKNOWN;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.AcceptRecommendation;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.VInput;
import org.kosit.validator.api.VInputFactory;
import org.kosit.validator.api.VResult;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.tasks.XvrlSerializer;
import org.kosit.validator.impl.xml.ProcessorProvider;
import org.kosit.xvrl.impl.XvrlConversionService;
import org.w3c.dom.Document;

import jakarta.xml.bind.JAXBException;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

/**
 * Tests the check interface.
 *
 * @author Andreas Penski
 */
public class DefaultVCheckTest {

    public static final int MULTI_COUNT = 5;

    private DefaultVCheck validCheck;

    // for checking certain error scenarios.
    private DefaultVCheck errorCheck;

    private DefaultVCheck jarScenarioCheck;

    final private EngineInformation engineInformation = new TestEngineInformation();

    @BeforeEach
    public void setup() throws URISyntaxException {
        final VConfiguration validConfig = VConfiguration.load(Simple.SCENARIOS, Simple.REPOSITORY_URI)
                .build(TestHelper.getTestProcessor());
        this.validCheck = new DefaultVCheck(this.engineInformation, validConfig);

        final VConfiguration errorConfig = VConfiguration.load(Simple.ERROR_SCENARIOS, Simple.REPOSITORY_URI)
                .build(TestHelper.getTestProcessor());
        this.errorCheck = new DefaultVCheck(this.engineInformation, errorConfig);

        final VConfiguration jarConfig = VConfiguration
                .load(requireNonNull(DefaultVCheckTest.class.getClassLoader().getResource("simple/packaged/scenarios.xml")).toURI(),
                        requireNonNull(DefaultVCheckTest.class.getClassLoader().getResource("simple/packaged/repository/")).toURI())
                .build(TestHelper.getTestProcessor());

        this.jarScenarioCheck = new DefaultVCheck(this.engineInformation, jarConfig);
    }

    @Test
    public void testHappyCase() throws JAXBException, SaxonApiException {
        final VResult doc = this.validCheck.checkInput(read(SIMPLE_VALID));
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
        final XvrlSerializer s = new XvrlSerializer(new XvrlConversionService(), ProcessorProvider.getProcessor());
        final XdmNode blub = s.serialize(doc.getReportSummary());
        System.out.println(blub);
    }

    @Test
    public void testJarCase() {
        final VResult doc = this.jarScenarioCheck.checkInput(read(SIMPLE_VALID));
        assertThat(doc).isNotNull();
        assertThat(doc.getReport()).isNotNull();
        assertThat(doc.isAcceptable()).isTrue();
        assertThat(doc.isSchematronValid()).isTrue();
        assertThat(doc.isSchemaValid()).isTrue();
        assertThat(doc.getAcceptRecommendation()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testWithoutAcceptMatch() {
        final VResult doc = this.validCheck.checkInput(read(Simple.FOO));
        assertThat(doc).isNotNull();
        assertThat(doc.getReport()).isNotNull();
        assertThat(doc.isAcceptable()).isTrue();
        assertThat(doc.getAcceptRecommendation()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.ACCEPTABLE);
    }

    @Test
    public void testHappyCaseDocument() {
        final Document doc = this.validCheck.check(read(SIMPLE_VALID));
        assertThat(doc).isNotNull();
    }

    @Test
    public void testMultipleCase() {
        @SuppressWarnings("unused")
        final List<VInput> VInput = IntStream.range(0, MULTI_COUNT).mapToObj(i -> read(SIMPLE_VALID)).collect(Collectors.toList());
        final List<VResult> docs = this.validCheck.checkInput(VInput);
        assertThat(docs).hasSize(MULTI_COUNT);
    }

    @Test
    public void testMultipleCaseDocument() {
        @SuppressWarnings("unused")
        final List<VInput> VInput = IntStream.range(0, MULTI_COUNT).mapToObj(i -> read(SIMPLE_VALID)).collect(Collectors.toList());
        final List<Document> docs = this.validCheck.check(VInput);
        assertThat(docs).hasSize(MULTI_COUNT);
    }

    @Test
    public void testExtract() {
        final DefaultResult doc = (DefaultResult) this.validCheck.checkInput(read(SIMPLE_VALID));
        assertThat(doc).isNotNull();
        assertThat(doc.getReport()).isNotNull();
        assertThat(doc.isAcceptable()).isTrue();
        // TODO MM add this
        // assertThat(doc.extractAsString("Report for eInvoice")).isNotEmpty();
        // assertThat(doc.extractAsElement("Report for eInvoice")).isNotNull();
        // assertThat(doc.extract("Report for eInvoice")).isNotEmpty();
    }

    @Test
    public void testGarbage() {
        final VResult result = this.validCheck.checkInput(read(GARBAGE));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isFalse();
        assertThat(result.isSchemaValid()).isFalse();
        assertThat(result.isProcessingSuccessful()).isFalse();
    }

    @Test
    public void testNoScenario() {
        final VResult result = this.validCheck.checkInput(read(UNKNOWN));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isTrue();
        assertThat(result.isProcessingSuccessful()).isTrue();
        assertThat(result.isSchemaValid()).isFalse();
        assertThat(result.getAcceptRecommendation()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.REJECT);
        assertThat(result.isAcceptable()).isFalse();
    }

    @Test
    public void testNotWellFormed() {
        final VResult result = this.validCheck.checkInput(read(NOT_WELLFORMED));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isFalse();
        assertThat(result.isSchemaValid()).isFalse();
        assertThat(result.isProcessingSuccessful()).isFalse();
        // TODO
        // assertThat(result.getAcceptRecommendation()).isEqualTo(AcceptRecommendation.REJECT);
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getReportDocument()).isNotNull();
    }

    @Test
    public void testRejectAcceptMatch() {
        final VResult result = this.validCheck.checkInput(read(REJECTED));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isTrue();
        assertThat(result.isSchemaValid()).isTrue();
        assertThat(result.isProcessingSuccessful()).isTrue();
        assertThat(result.getAcceptRecommendation()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.REJECT);
        assertThat(result.isAcceptable()).isFalse();
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getReportDocument()).isNotNull();
    }

    @Test
    public void testSchematronFailed() {
        final VResult result = this.validCheck.checkInput(read(SCHEMATRON_INVALID));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isTrue();
        assertThat(result.isSchemaValid()).isTrue();
        assertThat(result.getFailedAsserts()).isNotEmpty();
        assertThat(result.isSchematronValid()).isFalse();
        assertThat(result.getSchematronResult().get(0).findFailedAssert("content-1")).isPresent();
        assertThat(result.isProcessingSuccessful()).isTrue();
        // acceptMatch overules schematron!!!
        assertThat(result.getAcceptRecommendation()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.ACCEPTABLE);
        assertThat(result.isAcceptable()).isTrue();
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getReportDocument()).isNotNull();

    }

    @Test
    public void testSchematronFailedWithoutAcceptMatch() {
        final VResult result = this.validCheck.checkInput(read(FOO_SCHEMATRON_INVALID));
        assertThat(result).isNotNull();
        assertThat(result.isWellformed()).isTrue();
        assertThat(result.isSchemaValid()).isTrue();
        result.getFailedAsserts();
        assertThat(result.isSchematronValid()).isFalse();
        assertThat(result.getFailedAsserts()).isNotEmpty();
        assertThat(result.isProcessingSuccessful()).isTrue();
        assertThat(result.getAcceptRecommendation()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.REJECT);
        assertThat(result.isAcceptable()).isFalse();
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getReportDocument()).isNotNull();
    }

    @Test
    public void testSchematronExecutionError() {
        final VResult result = this.errorCheck.checkInput(read(SIMPLE_VALID));
        assertThat(result).isNotNull();
        assertThat(result.isProcessingSuccessful()).isFalse();
        assertThat(result.isSchematronValid()).isFalse();
        assertThat(result.isSchemaValid()).isTrue();
        assertThat(result.getAcceptRecommendation()).isEqualTo(org.kosit.validator.api.AcceptRecommendation.UNDEFINED);
        assertThat(result.isAcceptable()).isFalse();
        assertThat(result.getReport()).isNotNull();
        assertThat(result.getProcessingErrors()).hasSize(1);
    }

    @Test
    public void testXdmNode() throws Exception {
        XdmNode node = TestObjectFactory.createProcessor().newDocumentBuilder().build(new StreamSource(SIMPLE_VALID.toASCIIString()));
        VInput domVInput = VInputFactory.read(node, "node test");
        VResult result = this.validCheck.checkInput(domVInput);
        assertThat(result.isProcessingSuccessful()).isTrue();

        // test compatible configuration
        node = this.validCheck.getProcessor().newDocumentBuilder().build(new StreamSource(SIMPLE_VALID.toASCIIString()));
        domVInput = VInputFactory.read(node, "node test");
        result = this.validCheck.checkInput(domVInput);
        assertThat(result.isProcessingSuccessful()).isTrue();
    }
}
