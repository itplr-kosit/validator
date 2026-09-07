package org.kosit.validator.config;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kosit.base.annotation.ReturnsImmutableObject;
import org.kosit.base.error.SimpleError;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.schematron.ContentRepository;
import org.kosit.schematron.resolve.RelativeUriResolver;
import org.kosit.schematron.resolve.ResolvingConfigurationStrategy;
import org.kosit.schematron.resolve.ResolvingMode;
import org.kosit.validator.api.VCheck;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.ScenarioArtifacts;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.impl.tasks.DocumentParseTask;
import org.kosit.validator.scenario.v1.Scenario1Converter;
import org.kosit.validator.scenario.v1.ScenarioType;
import org.kosit.validator.scenario.v1.Scenarios;
import org.kost.validator.api.xml.CollectingErrorEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

/**
 * Configuration class that loads necessary {@link VCheck} configuration from an existing scenario.xml specification.
 * This is the recommended option when an official configuration exists as is the case with 'xrechnung'.
 *
 * @author Andreas Penski
 */
public class ConfigurationLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLoader.class);

    private static final String SUPPORTED_MAJOR_VERSION = "2";

    private static final String SUPPORTED_MAJOR_VERSION_SCHEMA = Scenario1Converter.NS_URI;

    protected final Map<String, Object> parameters = new HashMap<>();

    /**
     * URL pointing to the scenario.xml file.
     */
    private final URI scenarioDefinition;

    /**
     * Root folder containing the files required by the individual scenarios.
     */
    private final URI scenarioRepository;

    protected ResolvingMode resolvingMode = ResolvingMode.STRICT_RELATIVE;

    protected ResolvingConfigurationStrategy resolvingConfigurationStrategy;

    private static void checkVersion(final URI scenarioDefinition, final Processor processor) {
        try {
            final SingleProcessingResult<XdmNode, SimpleError> result = new DocumentParseTask(processor)
                    .parseDocument(ReadResource.inMemory(Resource.of(scenarioDefinition.toURL())));
            if (result.isValid() && !isSupportedDocument(result.getObject())) {
                throw new IllegalStateException("Specified scenario configuration " + scenarioDefinition
                        + " is not supported.\nThis version only supports definitions of '" + SUPPORTED_MAJOR_VERSION_SCHEMA + "'");
            }
        } catch (final IOException e) {
            throw new IllegalStateException("Error reading definition file", e);
        }
    }

    private static XdmNode findRoot(final XdmNode doc) {
        for (final XdmNode node : doc.children()) {
            if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
                return node;
            }
        }
        throw new IllegalArgumentException("No root element found");
    }

    private static boolean isSupportedDocument(final XdmNode doc) {
        final XdmNode root = findRoot(doc);
        final String frameworkVersion = root.getAttributeValue(new QName("frameworkVersion"));
        return frameworkVersion != null && frameworkVersion.startsWith(SUPPORTED_MAJOR_VERSION)
                && root.getNodeName().getNamespace().equals(SUPPORTED_MAJOR_VERSION_SCHEMA);
    }

    private static Scenario createFallback(final ContentRepository repository) {
        LOGGER.info("create Fallback: ");
        return new FallbackBuilder().build(repository).getObject();
    }

    @ReturnsImmutableObject
    private static List<Scenario> initializeScenarios(final Scenarios def, final ContentRepository contentRepository) {
        return def.getScenario().stream().map(s -> initialize(s, contentRepository)).toList();
    }

    private static Scenario initialize(final ScenarioType def, final ContentRepository repository) {
        final Scenario s = new Scenario(def);
        s.setMatchExecutable(ScenarioArtifacts.createMatchExecutable(repository, def));
        s.setSchema(ScenarioArtifacts.createSchema(repository, def));
        s.setSchematronValidations(ScenarioArtifacts.createSchematronTransformations(repository, def));
        s.setReportTransformations(ScenarioArtifacts.createReportTransformations(repository, def));
        s.setFactory(repository.getResolvingConfigurationStrategy());
        s.setUriResolver(repository.getResolver());
        s.setUnparsedTextURIResolver(repository.getUnparsedTextURIResolver());
        if (def.getAcceptMatch() != null) {
            s.setAcceptExecutable(ScenarioArtifacts.createAcceptExecutable(repository, def));
        }
        return s;
    }

    /**
     * Creates a new {@code ConfigurationLoader} instance.
     *
     * @param scenarioDefinition URL pointing to scenario.xml
     * @param scenarioRepository root folder with the scenario specific files
     */
    public ConfigurationLoader(final URI scenarioDefinition, final URI scenarioRepository) {
        if (scenarioRepository == null) {
            LOGGER.info("Creating default scenario repository (alongside scenario definition)");
            this.scenarioDefinition = RelativeUriResolver.resolve(URI.create("."), null);
        } else
            this.scenarioDefinition = scenarioDefinition;
        this.scenarioRepository = scenarioRepository;
    }

    URI getScenarioRepository() {
        return this.scenarioRepository;
    }

    public VConfiguration build(final Processor processor) {
        final ResolvingConfigurationStrategy resolving = getResolvingConfigurationStrategy();
        final ContentRepository contentRepository = new ContentRepository(processor, resolving, getScenarioRepository());
        final Scenarios def = loadScenarios(processor);
        final List<Scenario> scenarios = initializeScenarios(def, contentRepository);
        final Scenario fallbackScenario = createFallback(contentRepository);
        final DefaultConfiguration configuration = new DefaultConfiguration(scenarios, fallbackScenario);
        configuration.setAdditionalParameters(this.parameters);
        configuration.setAuthor(def.getAuthor());
        configuration.setDate(def.getDate().toString());
        configuration.setName(def.getName());
        configuration.setContentRepository(contentRepository);
        configuration.getAdditionalParameters().put(ConfigurationKeys.SCENARIOS_FILE, this.scenarioDefinition);
        configuration.getAdditionalParameters().put(ConfigurationKeys.SCENARIOS_DEFINITION, def);
        return (configuration);
    }

    private ResolvingConfigurationStrategy getResolvingConfigurationStrategy() {
        if (this.resolvingConfigurationStrategy != null) {
            LOGGER.info("Custom resolving strategy supplied. Please take care of xml security!");
            return this.resolvingConfigurationStrategy;
        }
        LOGGER.info("Using resolving strategy {}", this.resolvingMode);
        return this.resolvingMode.getStrategy();
    }

    private Scenarios loadScenarios(final Processor processor) {
        checkVersion(this.scenarioDefinition, processor);
        LOGGER.info("Loading scenarios from {}", this.scenarioDefinition);
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        final Scenarios scenarios = new Scenario1Converter().withEventHandler(handler).readXml(this.scenarioDefinition);
        if (handler.hasErrors()) {
            throw new IllegalStateException(
                    "Can not load scenarios from " + this.scenarioDefinition + " due to " + handler.getErrorDescription());
        }
        LOGGER.info("Loading scenario content from {}", this.getScenarioRepository());
        return scenarios;
    }

    /**
     * Sets actual {@link ResolvingMode}, when the validator needs to resolve stuff on startup.
     *
     * @param mode the resolving mode
     * @return this
     */
    public ConfigurationLoader setResolvingMode(final ResolvingMode mode) {
        if (this.resolvingConfigurationStrategy != null) {
            LOGGER.warn("Ignoring resolving mode configuration since a custom strategy is already defined");
        }
        this.resolvingMode = mode;
        return this;
    }

    public ConfigurationLoader setResolvingStrategy(final ResolvingConfigurationStrategy strategy) {
        this.resolvingConfigurationStrategy = strategy;
        return this;
    }

    /**
     * Add a parameter to the configuration.
     *
     * @param name the name of the parameter
     * @param value the parameter value object
     * @return this
     */
    public ConfigurationLoader addParameter(final String name, final Object value) {
        this.parameters.put(name, value);
        return this;
    }
}
