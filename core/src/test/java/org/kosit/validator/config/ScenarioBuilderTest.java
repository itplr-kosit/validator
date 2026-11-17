package org.kosit.validator.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kosit.validator.config.TestConfigurationFactory.createScenario;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kosit.base.string.StringHelper;
import org.kosit.schematron.ContentRepository;
import org.kosit.validator.TestHelper;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.scenario.generic.ScenarioNamespace;
import org.kosit.validator.scenario.v2.ScenarioType;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * Test {@link ScenarioBuilder}.
 *
 * @author Andreas Penski
 */
public class ScenarioBuilderTest {

    @Test
    public void simpleValid() {
        final SingleProcessingResult<Scenario, String> result = createScenario().build(TestHelper.createContentRepository());
        assertThat(result.isValid()).isTrue();
        assertThat(result.getObject().getDeclaration()).isNotNull();
    }

    @Test
    public void testNoSchemaValidatesWithSchematronAlone() {
        final ScenarioBuilder builder = createScenario();
        builder.validate((SchemaBuilder) null);
        final SingleProcessingResult<Scenario, String> result = builder.build(TestHelper.createContentRepository());
        assertThat(result.isValid()).isTrue();
        assertThat(result.getObject().getDeclaration().getXmlSchemas()).isEmpty();
    }

    @Test
    public void testNoMatchAppliesUnconditionally() {
        final ScenarioBuilder builder = createScenario();
        builder.match((String) null);
        final SingleProcessingResult<Scenario, String> result = builder.build(TestHelper.createContentRepository());
        assertThat(result.isValid()).isTrue();
        assertThat(result.getObject().isUnconditional()).isTrue();
        assertThat(result.getObject().getMatchExecutable()).isNull();
        assertThat(result.getObject().getDeclaration().getMatch()).isNull();
    }

    @Test
    public void testArtifactsHandedOverCompiledArePassedOn() {
        final ContentRepository repository = TestHelper.createContentRepository();
        final ScenarioBuilder builder = createScenario();
        builder.validate(ConfigurationBuilder.schematron("compiled").executable(repository.loadXsltScript(URI.create("simple.xsl"))));
        final SingleProcessingResult<Scenario, String> result = builder.build(repository);
        assertThat(result.isValid()).isTrue();
        // the location is synthetic, the compilation travels with the scenario
        assertThat(result.getObject().getPrecompiled()).hasSize(1);
        assertThat(result.getObject().getPrecompiled().keySet().iterator().next().toString()).startsWith("precompiled:");
    }

    @Test
    public void testInvalidMatch() {
        final ScenarioBuilder builder = createScenario();
        builder.match("/////");
        final SingleProcessingResult<Scenario, String> result = builder.build(TestHelper.createContentRepository());
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("match"));
    }

    @Test
    public void testNoAccept() {
        final ScenarioBuilder builder = createScenario();
        builder.acceptWith((String) null);
        final SingleProcessingResult<Scenario, String> result = builder.build(TestHelper.createContentRepository());
        assertThat(result.isValid()).isTrue();
    }

    @Test
    public void testInvalidAccept() {
        final ScenarioBuilder builder = createScenario();
        builder.acceptWith("/////");
        final SingleProcessingResult<Scenario, String> result = builder.build(TestHelper.createContentRepository());
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(e -> e.contains("accept"));
    }

    @Test
    public void testCombinedNamespaces() {
        final ContentRepository repository = TestHelper.createContentRepository();
        final Map<String, String> ns1 = new HashMap<>();
        ns1.put("n1", "http://n1.org");
        final XPathExecutable match = repository.createXPath("//n1:*", ns1);

        final Map<String, String> ns2 = new HashMap<>();
        ns2.put("n2", "http://n2.org");
        final XPathExecutable accept = repository.createXPath("//n2:*", ns2);

        final ScenarioBuilder builder = createScenario();
        builder.getNamespaces().clear();

        builder.match(match).acceptWith(accept).declareNamespace("n3", "http://n3.org");
        final SingleProcessingResult<Scenario, String> result = builder.build(repository);

        assertThat(result.isValid()).isTrue();
        final Scenario scenario = result.getObject();
        final List<ScenarioNamespace> namespaces = scenario.getDeclaration().getNamespaces();
        assertThat(namespaces.stream().map(ScenarioNamespace::getPrefix)).containsExactly("n1", "n2", "n3");
        assertThat(namespaces).hasSize(3);
    }

    @Test
    public void testConfigureWithExecutable() {
        final ContentRepository repository = TestHelper.createContentRepository();
        final XPathExecutable match = repository.createXPath("//*", null);
        final XPathExecutable accept = repository.createXPath("//*", null);
        final ScenarioBuilder builder = createScenario();
        builder.getNamespaces().clear();

        builder.match(match);
        builder.acceptWith(accept);
        final SingleProcessingResult<Scenario, String> result = builder.build(repository);
        assertThat(result.isValid()).isTrue();
        final var declaration = result.getObject().getDeclaration();
        assertThat(declaration.getMatch()).isNotEmpty();
        assertThat(declaration.getAcceptMatch()).isNotEmpty();
        assertThat(declaration.getNamespaces()).isEmpty();
    }

    @Test
    public void testConfigureWithSchematron() {
        final ContentRepository repository = TestHelper.createContentRepository();
        final XPathExecutable match = repository.createXPath("//*", null);
        final XPathExecutable accept = repository.createXPath("//*", null);
        final ScenarioBuilder builder = createScenario();
        builder.getNamespaces().clear();

        builder.match(match);
        builder.acceptWith(accept);
        final SingleProcessingResult<Scenario, String> result = builder.build(repository);
        assertThat(result.isValid()).isTrue();
        final var declaration = result.getObject().getDeclaration();
        assertThat(declaration.getMatch()).isNotEmpty();
        assertThat(declaration.getAcceptMatch()).isNotEmpty();
        assertThat(declaration.getNamespaces()).isEmpty();
        assertThat(declaration.getSchematrons()).isNotEmpty();
    }

    @Test
    public void testBasicAttributes() {
        final ContentRepository repository = TestHelper.createContentRepository();
        final String random = StringHelper.randomString(5);
        final ScenarioBuilder builder = createScenario();
        builder.name(random).description(random);
        final SingleProcessingResult<Scenario, String> result = builder.build(repository);
        assertThat(result.isValid()).isTrue();
        final var config = result.getObject().getDeclaration();
        assertThat(config.getName()).isEqualTo(random);
        assertThat(config.getDescription()).isNotNull();
        assertThat(config.getDescription().getBlocks()).isNotEmpty();
    }

    @Test
    public void testNoBasicAttributes() {
        final ContentRepository repository = TestHelper.createContentRepository();
        final ScenarioBuilder builder = createScenario();
        builder.name(null);
        final SingleProcessingResult<Scenario, String> result = builder.build(repository);
        assertThat(result.isValid()).isTrue();
        final var config = result.getObject().getDeclaration();
        assertThat(config.getName()).contains("manually");
        assertThat(config.getDescription()).isNotNull();
        assertThat(config.getDescription().getBlocks()).isNotEmpty();
    }
}
