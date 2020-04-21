package de.kosit.validationtool.config;

import static org.apache.commons.lang3.StringUtils.startsWith;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Check;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.CollectingErrorEventHandler;
import de.kosit.validationtool.impl.ContentRepository;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.RelativeUriResolver;
import de.kosit.validationtool.impl.Scenario;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.DocumentParseAction;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;
import de.kosit.validationtool.model.scenarios.CreateReportType;
import de.kosit.validationtool.model.scenarios.ScenarioType;
import de.kosit.validationtool.model.scenarios.Scenarios;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;

/**
 * Configuration class that loads neccessary {@link Check} configuration from an existing scenario.xml specification.
 * This is the recommended option when an official configuration exists as is the case with 'xrechnung'.
 * 
 * @author Andreas Penski
 */
@AllArgsConstructor
@Slf4j
public class LoadConfiguration extends BaseConfiguration {

    private static final String SUPPORTED_MAJOR_VERSION = "1";

    private static final String SUPPORTED_MAJOR_VERSION_SCHEMA = "http://www.xoev.de/de/validator/framework/1/scenarios";

    /**
     * URL, die auf die scenerio.xml Datei zeigt.
     */
    @Getter(AccessLevel.PACKAGE)
    private final URI scenarioDefinition;

    /**
     * Root-Ordner mit den von den einzelnen Szenarien benötigten Dateien
     */
    private final URI scenarioRepository;

    URI getScenarioRepository() {
        if (this.scenarioRepository == null) {
            log.info("Creating default scenario repository (alongside scenario definition)");
            return RelativeUriResolver.resolve(URI.create("."), this.scenarioDefinition);
        }
        return this.scenarioRepository;
    }

    private static void checkVersion(final URI scenarioDefinition) {
        try {
            final Result<XdmNode, XMLSyntaxError> result = DocumentParseAction.parseDocument(InputFactory.read(scenarioDefinition.toURL()));
            if (result.isValid() && !isSupportedDocument(result.getObject())) {
                throw new IllegalStateException(String.format(
                        "Specified scenario configuration %s is not supported.%nThis version only supports definitions of '%s'",
                        scenarioDefinition, SUPPORTED_MAJOR_VERSION_SCHEMA));

            }
        } catch (final MalformedURLException e) {
            throw new IllegalStateException("Error reading definition file");
        }
    }

    private static XdmNode findRoot(final XdmNode doc) {
        for (final XdmNode node : doc.children()) {
            if (node.getNodeKind() == XdmNodeKind.ELEMENT) {
                return node;
            }
        }
        throw new IllegalArgumentException("Kein root element gefunden");
    }

    private static boolean isSupportedDocument(final XdmNode doc) {
        final XdmNode root = findRoot(doc);
        final String frameworkVersion = root.getAttributeValue(new QName("frameworkVersion"));
        return startsWith(frameworkVersion, SUPPORTED_MAJOR_VERSION)
                && root.getNodeName().getNamespaceURI().equals(SUPPORTED_MAJOR_VERSION_SCHEMA);
    }

    private static Scenario createFallback(final Scenarios scenarios, final ContentRepository repository) {
        final ScenarioType t = new ScenarioType();
        t.setName("Fallback-Scenario");
        t.setMatch("count(/)<0");
        final CreateReportType reportType = new CreateReportType();
        reportType.setResource(scenarios.getNoScenarioReport().getResource());
        // always reject
        t.setAcceptMatch("count(/)<0");
        t.setCreateReport(reportType);
        final Scenario sceanrio = initialize(t, repository);
        sceanrio.setFallback(true);
        return sceanrio;
    }

    @Override
    protected RuntimeArtefacts buildArtefacts() {
        final ContentRepository contentRepository = buildContentRepository();
        final Scenarios def = loadScenarios();
        final List<Scenario> scenarios = initializeScenarios(def, contentRepository);
        final Scenario fallbackScenario = createFallback(def, contentRepository);
        final RuntimeArtefacts runtimeArtefacts = new RuntimeArtefacts(scenarios, fallbackScenario);
        runtimeArtefacts.setAuthor(def.getAuthor());
        runtimeArtefacts.setDate(def.getDate().toString());
        runtimeArtefacts.setName(def.getName());
        return runtimeArtefacts;
    }

    private static List<Scenario> initializeScenarios(final Scenarios def, final ContentRepository contentRepository) {
        return def.getScenario().stream().map(s -> initialize(s, contentRepository)).collect(Collectors.toList());
    }

    private ContentRepository buildContentRepository() {
        return new ContentRepository(getProcessor(), getScenarioRepository());
    }

    @Override
    public ContentRepository getContentRepository() {
        return buildContentRepository();
    }

    private Scenarios loadScenarios() {
        final ConversionService conversionService = new ConversionService();
        checkVersion(this.scenarioDefinition);
        log.info("Loading scenarios from {}", this.scenarioDefinition);
        final CollectingErrorEventHandler handler = new CollectingErrorEventHandler();
        final Scenarios scenarios = conversionService.readXml(this.scenarioDefinition, Scenarios.class,
                getContentRepository().getScenarioSchema(), handler);
        if (!handler.hasErrors()) {
            log.info("Loading scenario content from {}", this.scenarioRepository);
        } else {
            throw new IllegalStateException(
                    String.format("Can not load scenarios from %s due to %s", getScenarioDefinition(), handler.getErrorDescription()));
        }
        return scenarios;

    }

    private static Scenario initialize(final ScenarioType def, final ContentRepository repository) {
        final Scenario s = new Scenario(def);
        s.setSchema(repository.createSchema(def));
        s.setReportTransformation(repository.createReportTransformation(def));
        s.setMatchExecutable(repository.createMatchExecutable(def));
        if (def.getAcceptMatch() != null) {
            s.setAcceptExecutable(repository.createAccepptExecutable(def));
        }
        return s;
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDate() {
        return null;
    }
}
