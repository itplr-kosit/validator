package org.kosit.validator.config;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.kosit.base.uri.UriHelper;
import org.kosit.conformatron.source.ReadResource;
import org.kosit.conformatron.source.Resource;
import org.kosit.schematron.ContentRepository;
import org.kosit.schematron.resolve.ResolvingConfigurationStrategy;
import org.kosit.schematron.resolve.ResolvingMode;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlAction;
import org.kosit.validator.impl.conformatron.action.parsedoc.xml.ParseXmlResult;
import org.kosit.validator.scenario.v2.Scenario2Converter;
import org.kosit.validator.scenario.v2.Scenarios;
import org.kost.validator.api.xml.CollectingErrorEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

/**
 * Loads a {@link ScenarioSet} from an existing scenario.xml specification. This is the recommended option when an
 * official configuration exists as is the case with 'xrechnung'.
 * <p>
 * Loading validates the file against the scenario schema and compiles the match expressions; the validation artifacts
 * the scenarios refer to are <b>not</b> touched - they are resolved and compiled by the pipeline (steps 5 and 6), so
 * that a missing or broken artifact shows up in the report of the document instead of failing the start of the
 * validator.
 * </p>
 *
 * @author Andreas Penski
 */
public class ConfigurationLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationLoader.class);

    private static final String SUPPORTED_MAJOR_VERSION = "2";

    private static final String SUPPORTED_MAJOR_VERSION_SCHEMA = Scenario2Converter.NS_URI;

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
            // the same secured parse as step 2 of the pipeline; the scenario file is configuration, not input, but the
            // parser hardening applies to it just the same
            final ParseXmlResult result = new ParseXmlAction().execute(ReadResource.inMemory(Resource.of(scenarioDefinition.toURL())));
            if (result.isSuccess()
                    && !isSupportedDocument(processor.newDocumentBuilder().wrap(result.getParsedSource().getParsedContent()))) {
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

    /**
     * Creates a new {@code ConfigurationLoader} instance.
     *
     * @param scenarioDefinition URL pointing to scenario.xml
     * @param scenarioRepository root folder with the scenario specific files; {@code null} for the directory of the
     *            scenario.xml, also when that is inside an archive
     */
    public ConfigurationLoader(final URI scenarioDefinition, final @Nullable URI scenarioRepository) {
        if (scenarioDefinition == null) {
            throw new IllegalArgumentException("scenarioDefinition may not be null");
        }
        this.scenarioDefinition = scenarioDefinition;
        if (scenarioRepository == null) {
            LOGGER.info("Creating default scenario repository (alongside scenario definition)");
            this.scenarioRepository = UriHelper.resolve(scenarioDefinition, ".", true);
        } else {
            this.scenarioRepository = scenarioRepository;
        }
    }

    URI getScenarioRepository() {
        return this.scenarioRepository;
    }

    /**
     * Loads the scenarios.
     *
     * @param processor the Saxon processor the scenarios are compiled with - the same the engine runs with
     * @return the loaded scenarios with the identity of their configuration
     * @throws IllegalStateException if the file cannot be read, is not a supported scenario configuration, or a match
     *             expression does not compile
     */
    public ScenarioSet build(final Processor processor) {
        final ResolvingConfigurationStrategy resolving = getResolvingConfigurationStrategy();
        final ContentRepository contentRepository = new ContentRepository(processor, resolving, getScenarioRepository());
        final Scenarios def = loadScenarios(processor);
        final String definitionFile = this.scenarioDefinition.toString();
        final List<Scenario> scenarios = def.getScenario().stream().map(s -> Scenario.of(s, contentRepository, definitionFile)).toList();
        return new ScenarioSet(def.getName(), def.getAuthor(), def.getDate() == null ? null : def.getDate().toString(), definitionFile,
                scenarios);
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
        final Scenarios scenarios = new Scenario2Converter().withEventHandler(handler).readXml(this.scenarioDefinition);
        if (handler.hasErrors()) {
            throw new IllegalStateException(
                    "Can not load scenarios from " + this.scenarioDefinition + " due to " + handler.getErrorDescription());
        }
        LOGGER.info("Scenario content is resolved from {}", this.getScenarioRepository());
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
}
