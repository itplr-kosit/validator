package org.kosit.validator.config;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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
import org.kosit.validator.impl.conformatron.util.ScenarioXml;
import org.kosit.validator.scenario.generic.ScenarioResource;
import org.kosit.validator.scenario.generic.ScenarioConfiguration;
import org.kosit.validator.scenario.v2.Scenario2Converter;
import org.kosit.validator.scenario.v3.Scenario3Converter;
import org.kosit.validator.scenario.v3.Scenario3Mapper;
import org.kosit.validator.scenario.v2.Scenario2Mapper;
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

    /** The namespaces of the configuration formats this version reads. */

    private static final List<String> SUPPORTED_SCHEMAS = List.of(Scenario2Converter.NS_URI, Scenario3Converter.NS_URI);

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
                        + " is not supported.\nThis version supports definitions of " + SUPPORTED_SCHEMAS);
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
        return SUPPORTED_SCHEMAS.contains(findRoot(doc).getNodeName().getNamespace());
    }

    private String rootNamespace(final Processor processor) {
        try {
            final ParseXmlResult result = new ParseXmlAction().execute(ReadResource.inMemory(Resource.of(this.scenarioDefinition.toURL())));
            return findRoot(processor.newDocumentBuilder().wrap(result.getParsedSource().getParsedContent())).getNodeName().getNamespace();
        } catch (final IOException e) {
            throw new IllegalStateException("Error reading definition file", e);
        }
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
        final LoadedConfiguration loaded = loadScenarios(processor);
        final String definitionFile = this.scenarioDefinition.toString();
        final ScenarioConfiguration declaration = loaded.declaration();
        rejectWhatCanNotBeResolvedYet(declaration);
        final List<Scenario> scenarios = IntStream.range(0, declaration.getScenarios().size()).mapToObj(
                i -> Scenario.of(declaration.getScenarios().get(i), loaded.declarationXml().get(i), contentRepository, definitionFile))
                .toList();
        return new ScenarioSet(declaration.getName(), declaration.getAuthor(),
                declaration.getLastModificationDate() == null ? null : declaration.getLastModificationDate().toString(), definitionFile,
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

    /**
     * Reads the configuration into the version independent model, whichever format it is written in, and hands back the
     * declaration of every scenario as the document wrote it - step {@code select-scenario} embeds that into the
     * report, so it may not be a normalisation.
     */
    private LoadedConfiguration loadScenarios(final Processor processor) {
        checkVersion(this.scenarioDefinition, processor);
        LOGGER.info("Loading scenarios from {}", this.scenarioDefinition);
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        final LoadedConfiguration loaded;
        if (Scenario3Converter.NS_URI.equals(rootNamespace(processor))) {
            final org.kosit.validator.scenario.v3.Scenarios document = new Scenario3Converter().withEventHandler(handler)
                    .readXml(this.scenarioDefinition);
            loaded = new LoadedConfiguration(Scenario3Mapper.toGeneric(document),
                    document.getScenarioXmlOrScenarioPdf().stream().map(ScenarioXml::toXmlBytes).toList());
        } else {
            final Scenarios document = new Scenario2Converter().withEventHandler(handler).readXml(this.scenarioDefinition);
            loaded = new LoadedConfiguration(Scenario2Mapper.toGeneric(document),
                    document.getScenario().stream().map(ScenarioXml::toXmlBytes).toList());
        }
        if (handler.hasErrors()) {
            throw new IllegalStateException(
                    "Can not load scenarios from " + this.scenarioDefinition + " due to " + handler.getErrorDescription());
        }
        LOGGER.info("Scenario content is resolved from {}", this.getScenarioRepository());
        return loaded;
    }

    /**
     * Refuses what this version can read but not yet resolve.
     * <p>
     * Resolution still runs against the one repository the loader was given: every scenario resolves its artifacts
     * there and nowhere else. A configuration of version 3 may declare several repositories, point a resource at one of
     * them by name, or take a resource out of every repository with an absolute location - and honouring that needs the
     * artifact retrieval to resolve per resource, which comes with the configuration API. Until then such a
     * configuration is refused, because resolving it against the single repository would quietly validate against
     * artifacts the configuration did not name.
     * </p>
     */
    private void rejectWhatCanNotBeResolvedYet(final ScenarioConfiguration declaration) {
        if (declaration.getRepositories().size() > 1) {
            throw new IllegalStateException("The configuration " + this.scenarioDefinition + " declares "
                    + declaration.getRepositories().size() + " repositories. This version resolves every artifact in the one"
                    + " repository it is given, so it can not honour that yet");
        }
        for (final org.kosit.validator.scenario.generic.Scenario scenario : declaration.getScenarios()) {
            for (final ScenarioResource resource : resourcesOf(scenario)) {
                if (resource.hasAbsoluteLocation()) {
                    throw new IllegalStateException("The resource '" + resource.getName() + "' of scenario '" + scenario.getName()
                            + "' has an absolute location. This version resolves every artifact inside the repository, so it can"
                            + " not honour that yet");
                }
                if (!resource.hasLocation()) {
                    throw new IllegalStateException("The resource '" + resource.getName() + "' of scenario '" + scenario.getName()
                            + "' has no location. Retrieving an artifact by its coordinate alone needs a repository that serves"
                            + " coordinates, which this version does not have");
                }
            }
        }
    }

    private static List<ScenarioResource> resourcesOf(final org.kosit.validator.scenario.generic.Scenario scenario) {
        final List<ScenarioResource> resources = new ArrayList<>(scenario.getXmlSchemas());
        scenario.getSchematrons().forEach(schematron -> resources.add(schematron.getResource()));
        return resources;
    }

    /** A configuration read into the version independent model, with the XML every scenario was written as. */
    private record LoadedConfiguration(ScenarioConfiguration declaration, List<byte[]> declarationXml) {
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
