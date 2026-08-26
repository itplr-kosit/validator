package org.kosit.validator.config;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Strings;
import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.impl.CollectingErrorEventHandler;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.conformatron.source.ReadResource;
import org.kosit.validator.impl.conformatron.source.Resource;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.impl.tasks.DocumentParseTask;
import org.kosit.validator.impl.xml.RelativeUriResolver;
import org.kosit.validator.model.XMLSyntaxError;
import org.kosit.validator.scenario.v1.Scenario1ConversionService;
import org.kosit.validator.scenario.v1.ScenarioType;
import org.kosit.validator.scenario.v1.Scenarios;
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

    private static final String SUPPORTED_MAJOR_VERSION_SCHEMA = "http://www.xoev.de/de/validator/framework/2/scenarios";

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
            final Result<XdmNode, XMLSyntaxError> result = new DocumentParseTask(processor)
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
        return Strings.CS.startsWith(frameworkVersion, SUPPORTED_MAJOR_VERSION)
                && root.getNodeName().getNamespaceURI().equals(SUPPORTED_MAJOR_VERSION_SCHEMA);
    }

    private static Scenario createFallback(final Scenarios scenarios, final ContentRepository repository) {
        LOGGER.info("create Fallback: ");
        return new FallbackBuilder().build(repository).getObject();
    }

    private static List<Scenario> initializeScenarios(final Scenarios def, final ContentRepository contentRepository) {
        return def.getScenario().stream().map(s -> initialize(s, contentRepository)).toList();
    }

    private static Scenario initialize(final ScenarioType def, final ContentRepository repository) {
        final Scenario s = new Scenario(def);
        s.setMatchExecutable(repository.createMatchExecutable(def));
        s.setSchema(repository.createSchema(def));
        s.setSchematronValidations(repository.createSchematronTransformations(def));
        s.setReportTransformations(repository.createReportTransformations(def));
        s.setFactory(repository.getResolvingConfigurationStrategy());
        s.setUriResolver(repository.getResolver());
        s.setUnparsedTextURIResolver(repository.getUnparsedTextURIResolver());
        if (def.getAcceptMatch() != null) {
            s.setAcceptExecutable(repository.createAccepptExecutable(def));
        }
        return s;
    }

    URI getScenarioRepository() {
        if (this.scenarioRepository == null) {
            LOGGER.info("Creating default scenario repository (alongside scenario definition)");
            return RelativeUriResolver.resolve(URI.create("."), this.scenarioDefinition);
        }
        return this.scenarioRepository;
    }

    public VConfiguration build(final Processor processor) {
        final ResolvingConfigurationStrategy resolving = getResolvingConfigurationStrategy();
        final ContentRepository contentRepository = new ContentRepository(processor, resolving, getScenarioRepository());
        final Scenarios def = loadScenarios(processor);
        final List<Scenario> scenarios = initializeScenarios(def, contentRepository);
        final Scenario fallbackScenario = createFallback(def, contentRepository);
        final DefaultConfiguration configuration = new DefaultConfiguration(scenarios, fallbackScenario);
        configuration.setAdditionalParameters(this.parameters);
        configuration.setAuthor(def.getAuthor());
        configuration.setDate(def.getDate().toString());
        configuration.setName(def.getName());
        configuration.setContentRepository(contentRepository);
        configuration.getAdditionalParameters().put(Keys.SCENARIOS_FILE, this.scenarioDefinition);
        configuration.getAdditionalParameters().put(Keys.SCENARIO_DEFINITION, def);
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
        final Scenario1ConversionService conversionService = new Scenario1ConversionService();
        final Scenarios scenarios = conversionService.withEventHandler(handler).readXml(this.scenarioDefinition, Scenarios.class);
        if (handler.hasErrors()) {
            throw new IllegalStateException(
                    "Can not load scenarios from " + getScenarioDefinition() + " due to " + handler.getErrorDescription());
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

    /**
     * Creates a new {@code ConfigurationLoader} instance.
     *
     * @param scenarioDefinition URL, die auf die scenerio.xml Datei zeigt.
     * @param scenarioRepository Root-Ordner mit den von den einzelnen Szenarien benötigten Dateien
     */
    public ConfigurationLoader(final URI scenarioDefinition, final URI scenarioRepository) {
        this.scenarioDefinition = scenarioDefinition;
        this.scenarioRepository = scenarioRepository;
    }

    /**
     * URL, die auf die scenerio.xml Datei zeigt.
     */
    URI getScenarioDefinition() {
        return this.scenarioDefinition;
    }
}
