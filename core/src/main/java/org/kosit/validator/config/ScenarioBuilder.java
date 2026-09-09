package org.kosit.validator.config;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.conformatron.api.model.validation.CTCompiledValidationArtifact;
import org.conformatron.api.model.validation.CTStandardValidationType;
import org.kosit.base.string.StringHelper;
import org.kosit.conformatron.validation.CompiledValidationArtifact;
import org.kosit.schematron.ContentRepository;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.scenario.v1.DescriptionType;
import org.kosit.validator.scenario.v1.NamespaceType;
import org.kosit.validator.scenario.v1.ObjectFactory;
import org.kosit.validator.scenario.v1.ResourceType;
import org.kosit.validator.scenario.v1.ScenarioType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * Builder for a {@link Scenario}.
 * <p>
 * A scenario needs nothing but a name: without a {@link #match(String) match} it applies unconditionally, without
 * {@link #validate(SchemaBuilder) schema} and {@link #validate(SchematronBuilder) schematron} it validates nothing.
 * Artifacts handed over compiled ({@link SchemaBuilder#schema(javax.xml.validation.Schema)},
 * {@link SchematronBuilder#executable(net.sf.saxon.s9api.XsltExecutable)}) are passed on to the pipeline as they are;
 * artifacts given by location are checked right away and compiled by the pipeline from the repository.
 * </p>
 *
 * @author Andreas Penski
 */
public class ScenarioBuilder implements SingleProcessingResultBuilder<Scenario> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioBuilder.class);

    private static int nameCount = 0;

    private static final String DEFAULT_DESCRIPTION = "This scenario was created via API";

    private final Map<String, String> namespaces = new HashMap<>();

    private final XPathBuilder matchConfig = new XPathBuilder("match");

    private final XPathBuilder acceptConfig = new XPathBuilder("accept");

    private String name;

    private SchemaBuilder schemaBuilder;

    private final List<SchematronBuilder> schematronBuilders = new ArrayList<>();

    private String description;

    @Override
    public SingleProcessingResult<Scenario, String> build(final ContentRepository repository) {
        final List<String> errors = new ArrayList<>();
        final ScenarioType type = createType();
        final Map<URI, CTCompiledValidationArtifact<?>> precompiled = new HashMap<>();
        final XPathExecutable match = buildMatch(repository, errors, type);
        buildSchema(repository, errors, type, precompiled);
        buildSchematron(repository, errors, type, precompiled);
        buildAccept(repository, errors, type);
        buildNamespaces(type);
        if (!errors.isEmpty()) {
            return new SingleProcessingResult<>(null, errors);
        }
        return new SingleProcessingResult<>(new Scenario(type, repository, match, null, precompiled), errors);
    }

    /**
     * Add a preconfiguration {@link XPathExecutable} to match the scenario
     *
     * @param executable the xpath executable
     * @return this
     */
    public ScenarioBuilder match(final XPathExecutable executable) {
        this.matchConfig.setExecutable(executable);
        return this;
    }

    /**
     * Add an xpath expression to match the scenario. You can leverage declared namespaces. A scenario without a match
     * applies unconditionally.
     *
     * @param xpath the expression
     * @return this
     */
    public ScenarioBuilder match(final String xpath) {
        this.matchConfig.setXpath(xpath);
        return this;
    }

    /**
     * Declare a namespace to use for match and accept configurations.
     *
     * @param prefix the prefix to use
     * @param uri the uri of this namespace
     * @return this
     */
    public ScenarioBuilder declareNamespace(final String prefix, final String uri) {
        this.namespaces.put(prefix, uri);
        return this;
    }

    /**
     * Add a preconfiguration {@link XPathExecutable} as the {@code acceptMatch} of the scenario. Note that 2.0 does not
     * evaluate it; it is kept in the declaration.
     *
     * @param executable the xpath executable
     * @return this
     */
    public ScenarioBuilder acceptWith(final XPathExecutable executable) {
        this.acceptConfig.setExecutable(executable);
        return this;
    }

    /**
     * Add an xpath expression as the {@code acceptMatch} of the scenario. You can leverage declared namespaces. Note
     * that 2.0 does not evaluate it; it is kept in the declaration.
     *
     * @param acceptXpath the xpath expresison
     * @return this
     */
    public ScenarioBuilder acceptWith(final String acceptXpath) {
        this.acceptConfig.setXpath(acceptXpath);
        return this;
    }

    /**
     * Add a schematron validation configuration for this scenario.
     *
     * @param schematron the schematron configuration
     * @return this
     */
    public ScenarioBuilder validate(final SchematronBuilder schematron) {
        if (schematron != null) {
            this.schematronBuilders.add(schematron);
        }
        return this;
    }

    /**
     * Validate matching documents with the specified schema configuration.
     *
     * @param schema the schema configuration
     * @return this
     */
    public ScenarioBuilder validate(final SchemaBuilder schema) {
        this.schemaBuilder = schema;
        return this;
    }

    /**
     * Add description for this scenario. This is part of the scenario declaration embedded into the report.
     *
     * @param description the description
     * @return this
     */
    public ScenarioBuilder description(final String description) {
        this.description = description;
        return this;
    }

    private static String generateName() {
        return "manually created scenario " + nameCount++;
    }

    private void buildNamespaces(final ScenarioType type) {
        this.namespaces.putAll(this.acceptConfig.getNamespaces());
        this.namespaces.putAll(this.matchConfig.getNamespaces());
        final List<NamespaceType> all = this.namespaces.entrySet().stream().map(e -> {
            final NamespaceType n = new NamespaceType();
            n.setPrefix(e.getKey());
            n.setValue(e.getValue());
            return n;
        }).toList();
        type.getNamespace().addAll(all);
    }

    private XPathExecutable buildMatch(final ContentRepository repository, final List<String> errors, final ScenarioType type) {
        this.matchConfig.setNamespaces(this.namespaces);
        if (!this.matchConfig.isAvailable()) {
            LOGGER.debug("No match configuration: scenario '{}' applies unconditionally", this.name);
            return null;
        }
        final SingleProcessingResult<XPathExecutable, String> result = this.matchConfig.build(repository);
        if (result.isValid()) {
            type.setMatch(this.matchConfig.getXPath());
            this.namespaces.putAll(this.matchConfig.getNamespaces());
            return result.getObject();
        }
        errors.addAll(result.getErrors());
        return null;
    }

    private void buildAccept(final ContentRepository repository, final List<String> errors, final ScenarioType type) {
        this.acceptConfig.setNamespaces(this.namespaces);
        if (this.acceptConfig.isAvailable()) {
            // compiled to check the expression; the pipeline does not evaluate acceptMatch
            final SingleProcessingResult<XPathExecutable, String> result = this.acceptConfig.build(repository);
            if (result.isValid()) {
                type.setAcceptMatch(this.acceptConfig.getXPath());
                this.namespaces.putAll(this.acceptConfig.getNamespaces());
            } else {
                errors.addAll(result.getErrors());
            }
        } else {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("No accept configuration available");
        }
    }

    private void buildSchematron(final ContentRepository repository, final List<String> errors, final ScenarioType type,
            final Map<URI, CTCompiledValidationArtifact<?>> precompiled) {
        this.schematronBuilders.forEach(e -> {
            final var result = e.build(repository);
            if (result.isValid()) {
                type.getValidateWithSchematron().add(result.getObject().validateResult());
                if (result.getObject().executable() != null) {
                    precompiled.put(URI.create(result.getObject().validateResult().getResource().getLocation()),
                            new CompiledValidationArtifact<>(CTStandardValidationType.SCHEMATRON_XSLT2, result.getObject().executable()));
                }
            } else {
                errors.addAll(result.getErrors());
            }
        });
    }

    private void buildSchema(final ContentRepository repository, final List<String> errors, final ScenarioType type,
            final Map<URI, CTCompiledValidationArtifact<?>> precompiled) {
        if (this.schemaBuilder == null) {
            LOGGER.debug("No schema configuration: scenario '{}' validates without XML Schema", this.name);
            return;
        }
        final var result = this.schemaBuilder.build(repository);
        if (result.isValid()) {
            type.setValidateWithXmlSchema(result.getObject().validationResult());
            if (result.getObject().schema() != null) {
                for (final ResourceType resource : result.getObject().validationResult().getResource()) {
                    precompiled.put(URI.create(resource.getLocation()),
                            new CompiledValidationArtifact<>(CTStandardValidationType.XSD, result.getObject().schema()));
                }
            }
        } else {
            errors.addAll(result.getErrors());
        }
    }

    private ScenarioType createType() {
        final ScenarioType type = new ScenarioType();
        type.setName(StringHelper.isNotEmpty(this.name) ? this.name : generateName());
        final DescriptionType desc = new DescriptionType();
        desc.getPOrOlOrUl()
                .add(new ObjectFactory().createDescriptionTypeP(StringHelper.blankToDefault(this.description, DEFAULT_DESCRIPTION)));
        type.setDescription(desc);
        return type;
    }

    public ScenarioBuilder name(final String name) {
        this.name = name;
        return this;
    }

    public ScenarioBuilder() {
    }

    Map<String, String> getNamespaces() {
        return this.namespaces;
    }

    XPathBuilder getMatchConfig() {
        return this.matchConfig;
    }

    XPathBuilder getAcceptConfig() {
        return this.acceptConfig;
    }

    String getName() {
        return this.name;
    }

    SchemaBuilder getSchemaBuilder() {
        return this.schemaBuilder;
    }

    List<SchematronBuilder> getSchematronBuilders() {
        return this.schematronBuilders;
    }

    String getDescription() {
        return this.description;
    }
}
