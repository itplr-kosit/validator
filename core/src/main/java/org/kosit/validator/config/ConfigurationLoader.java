package org.kosit.validator.config;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
import org.kosit.validator.scenario.generic.ScenarioRepository;
import org.kosit.validator.scenario.generic.ScenarioResource;
import org.kosit.validator.scenario.generic.ScenarioConfiguration;
import org.kosit.validator.scenario.generic.ScenarioCoordinate;
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
        rejectWhatCanNotBeApplied(declaration);
        // a resource resolves in the repository it names; one that names none resolves in the repository the loader
        // was given, which is also what every configuration of version 2 does
        final Map<String, ContentRepository> repositories = new LinkedHashMap<>();
        for (final ScenarioRepository repository : declaration.getRepositories()) {
            repositories.put(repository.getName(), new ContentRepository(processor, resolving, locationOf(repository)));
        }
        final List<Scenario> scenarios = IntStream.range(0, declaration.getScenarios().size()).mapToObj(i -> Scenario
                .of(declaration.getScenarios().get(i), loaded.declarationXml().get(i), contentRepository, repositories, definitionFile))
                .toList();
        return new ScenarioSet(declaration.getName(), declaration.getAuthor(),
                declaration.getLastModificationDate() == null ? null : declaration.getLastModificationDate().toString(), definitionFile,
                scenarios);
    }

    /**
     * Where a declared repository stands: its location, resolved against the repository the loader was given. A
     * repository is a directory, so the URI has to end in a slash - without it resolving an artifact would drop the
     * last segment and reach beside the repository. {@link UriHelper} is used instead of {@link URI#resolve(String)},
     * so that a configuration inside an archive resolves its repositories within that archive.
     */
    private URI locationOf(final ScenarioRepository repository) {
        final String location = repository.getLocation().endsWith("/") ? repository.getLocation() : repository.getLocation() + "/";
        return UriHelper.resolve(getScenarioRepository(), location, true);
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
     * Refuses what this version can read but not apply - loudly, when the configuration is loaded, instead of applying
     * it differently from what it says.
     * <p>
     * A PDF scenario is out of scope entirely: version 3 can express one, the pipeline validates XML documents. Nothing
     * downstream would notice it either - a scenario without a match expression counts as unconditional, so every run
     * would end as ambiguous without ever naming the reason.
     * </p>
     * <p>
     * A resource is resolved by its location in the repository it names. An absolute location, which takes a resource
     * out of every repository, and a resource without a location, which would have to be retrieved by its coordinate,
     * both need a resolution this version does not have.
     * </p>
     * <p>
     * A coordinate identifies; in this version it does not select. That rules out what only makes sense together with
     * selection: a pseudo version such as {@code latest}, which names no version but a rule for choosing one, and a
     * date from which a configuration is valid. Both would end up in the report as if they had been honoured. And
     * because the report names scenarios and artifacts by their coordinates, a coordinate has to name exactly one of
     * them - see {@link #rejectAmbiguousCoordinates(ScenarioConfiguration)}.
     * </p>
     */
    private void rejectWhatCanNotBeApplied(final ScenarioConfiguration declaration) {
        if (declaration.getValidFromDate() != null) {
            throw new IllegalStateException("The configuration '" + declaration.getName() + "' is valid from "
                    + declaration.getValidFromDate() + ". This version applies a configuration as soon as it is loaded and does"
                    + " not choose between configurations by date, so it can not honour that");
        }
        for (final org.kosit.validator.scenario.generic.Scenario scenario : declaration.getScenarios()) {
            if (scenario.isPdf()) {
                throw new IllegalStateException("The scenario '" + scenario.getName() + "' validates a PDF document, which this"
                        + " version does not support. Remove it from the configuration or validate the embedded XML document"
                        + " with the scenario it refers to");
            }
            checkCoordinate(scenario.getCoordinate(), "The scenario '" + scenario.getName() + "'");
            for (final ScenarioResource resource : resourcesOf(scenario)) {
                final String what = "The resource '" + resource.getName() + "' of scenario '" + scenario.getName() + "'";
                if (resource.hasRepository() && declaration.findRepository(resource) == null) {
                    throw new IllegalStateException(
                            what + " names the repository '" + resource.getRepository() + "', which the configuration does not declare");
                }
                if (resource.hasAbsoluteLocation()) {
                    throw new IllegalStateException(what + " has an absolute location. This version resolves every artifact"
                            + " inside a repository, so it can not honour that yet");
                }
                if (!resource.hasLocation()) {
                    throw new IllegalStateException(what + " has no location. Retrieving an artifact by its coordinate alone needs"
                            + " a repository that serves coordinates, which this version does not have");
                }
                checkCoordinate(resource.getCoordinate(), what);
            }
        }
        rejectAmbiguousCoordinates(declaration);
    }

    /**
     * A coordinate is the identity the report names its subject by. A concrete version that is no DVR version - the
     * Factur-X version {@code 1.09.2} has a leading zero - identifies just as well and is used as written; the warning
     * says that it can not take part in any comparison. A pseudo version identifies nothing and is refused.
     *
     * @param coordinate the declared coordinate; {@code null} for none, which is checked elsewhere
     * @param what what declares it, for the message
     */
    private static void checkCoordinate(final @Nullable ScenarioCoordinate coordinate, final String what) {
        if (coordinate == null) {
            return;
        }
        if (!coordinate.hasCoordinate()) {
            LOGGER.warn("{} declares the coordinate '{}', which is used as it is written but is no DVR coordinate: {}", what,
                    coordinate.getAsSingleID(), coordinate.getCoordinateError());
            return;
        }
        if (coordinate.getCoordinate().getVersionObj().isPseudoVersion()) {
            throw new IllegalStateException(what + " declares the coordinate '" + coordinate.getAsSingleID() + "' with the pseudo version '"
                    + coordinate.getVersion() + "'. A coordinate names the exact version the"
                    + " configuration was written for, and this version retrieves no artifact by its coordinate, so nothing"
                    + " could turn the pseudo version into one");
        }
    }

    /**
     * Refuses a coordinate that names more than one thing. Two scenarios with one coordinate would be indistinguishable
     * in the report, and selecting a scenario by its coordinate would pick one of them silently. Two different files
     * under one coordinate would be reported as the same artifact. The same file under the same coordinate in several
     * scenarios is one artifact used twice, and fine.
     *
     * @param declaration the configuration
     */
    private static void rejectAmbiguousCoordinates(final ScenarioConfiguration declaration) {
        final Map<String, String> scenarioByCoordinate = new HashMap<>();
        final Map<String, String> artifactByCoordinate = new HashMap<>();
        for (final org.kosit.validator.scenario.generic.Scenario scenario : declaration.getScenarios()) {
            if (scenario.hasCoordinate()) {
                final String coordinate = scenario.getCoordinate().getAsSingleID();
                final String other = scenarioByCoordinate.putIfAbsent(coordinate, scenario.getName());
                if (other != null) {
                    throw new IllegalStateException("The scenarios '" + other + "' and '" + scenario.getName()
                            + "' both declare the coordinate '" + coordinate + "'. The report names a scenario by its coordinate,"
                            + " so it could not say which of them was applied");
                }
            }
            for (final ScenarioResource resource : resourcesOf(scenario)) {
                if (resource.hasCoordinate()) {
                    final String coordinate = resource.getCoordinate().getAsSingleID();
                    final String artifact = artifactOf(declaration, resource);
                    final String other = artifactByCoordinate.putIfAbsent(coordinate, artifact);
                    if (other != null && !other.equals(artifact)) {
                        throw new IllegalStateException("The coordinate '" + coordinate + "' is declared for both '" + other + "' and '"
                                + artifact + "'. A coordinate names one artifact, and the report names the artifact" + " by it");
                    }
                }
            }
        }
    }

    /**
     * The file a resource resolves to, written the way a message names it: the repository it lies in, then the
     * location.
     */
    private static String artifactOf(final ScenarioConfiguration declaration, final ScenarioResource resource) {
        final ScenarioRepository repository = declaration.findRepository(resource);
        return (repository == null ? "" : repository.getName() + ":") + resource.getLocation();
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
