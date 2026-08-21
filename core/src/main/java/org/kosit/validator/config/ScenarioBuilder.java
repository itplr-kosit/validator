package org.kosit.validator.config;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.validation.Schema;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.kosit.validator.api.VInput;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.Scenario.Transformation;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.model.scenarios.CreateReportType;
import org.kosit.validator.model.scenarios.DescriptionType;
import org.kosit.validator.model.scenarios.NamespaceType;
import org.kosit.validator.model.scenarios.ObjectFactory;
import org.kosit.validator.model.scenarios.ScenarioType;
import org.kosit.validator.model.scenarios.ValidateWithSchematron;
import org.kosit.validator.model.scenarios.ValidateWithXmlSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XPathExecutable;

/**
 * Builder for {@link Scenario} configuration.
 * 
 * @author Andreas Penski
 */
public class ScenarioBuilder implements Builder<Scenario> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioBuilder.class);

    private static int nameCount = 0;

    private static final String DEFAULT_DESCRIPTION = "This scenario was created via API";

    private final Map<String, String> namespaces = new HashMap<>();

    private final XPathBuilder matchConfig = new XPathBuilder("match");

    private final XPathBuilder acceptConfig = new XPathBuilder("accept");

    private String name;

    private SchemaBuilder schemaBuilder;

    private final List<SchematronBuilder> schematronBuilders = new ArrayList<>();

    private ReportBuilder reportBuilder;

    private String description;

    @Override
    public Result<Scenario, String> build(final ContentRepository repository) {
        final List<String> errors = new ArrayList<>();
        final Scenario scenario = new Scenario(createType());
        buildMatch(repository, errors, scenario);
        buildSchema(repository, errors, scenario);
        buildSchematron(repository, errors, scenario);
        buildReport(repository, errors, scenario);
        buildAccept(repository, errors, scenario);
        buildNamespaces(scenario);
        scenario.setFactory(repository.getResolvingConfigurationStrategy());
        scenario.setUriResolver(repository.getResolver());
        scenario.setUnparsedTextURIResolver(repository.getUnparsedTextURIResolver());
        return new Result<>(scenario, errors);
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
     * Add an xpath expression to match the scenario. You can leverage declared namespaces.
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
     * Add a preconfiguration {@link XPathExecutable} to compute acceptance for the scenario
     * 
     * @param executable the xpath executable
     * @return this
     */
    public ScenarioBuilder acceptWith(final XPathExecutable executable) {
        this.acceptConfig.setExecutable(executable);
        return this;
    }

    /**
     * Add an xpath expression to compute acceptance for the scenario. You can leverage declared namespaces.
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
     * Validate matching {@link VInput Inputs} with the specified schema configuration.
     * 
     * @param schema the schema configuration
     * @return this
     */
    public ScenarioBuilder validate(final SchemaBuilder schema) {
        this.schemaBuilder = schema;
        return this;
    }

    /**
     * Add description for this scenario. This is part of the {@link org.kosit.validator.model.xvrl.XVRLReportSummary}
     * configuration and can be used while creating the report
     * 
     * @param description the description
     * @return this
     */
    public ScenarioBuilder description(final String description) {
        this.description = description;
        return this;
    }

    /**
     * Add a configuration for generating the final report for the {@link VInput}.
     * 
     * @param reportBuilder the report configuration
     * @return this
     */
    public ScenarioBuilder with(final ReportBuilder reportBuilder) {
        this.reportBuilder = reportBuilder;
        return this;
    }

    private static String generateName() {
        return "manually created scenario " + nameCount++;
    }

    private void buildNamespaces(final Scenario scenario) {
        this.namespaces.putAll(this.acceptConfig.getNamespaces());
        this.namespaces.putAll(this.matchConfig.getNamespaces());
        final List<NamespaceType> all = this.namespaces.entrySet().stream().map(e -> {
            final NamespaceType n = new NamespaceType();
            n.setPrefix(e.getKey());
            n.setValue(e.getValue());
            return n;
        }).collect(Collectors.toList());
        scenario.getConfiguration().getNamespace().addAll(all);
    }

    private void buildMatch(final ContentRepository repository, final List<String> errors, final Scenario scenario) {
        this.matchConfig.setNamespaces(this.namespaces);
        final Result<XPathExecutable, String> result = this.matchConfig.build(repository);
        if (result.isValid()) {
            scenario.setMatchExecutable(result.getObject());
            scenario.getConfiguration().setMatch(this.matchConfig.getXPath());
            this.namespaces.putAll(this.matchConfig.getNamespaces());
        } else {
            errors.addAll(result.getErrors());
        }
    }

    private void buildAccept(final ContentRepository repository, final List<String> errors, final Scenario scenario) {
        this.acceptConfig.setNamespaces(this.namespaces);
        if (this.acceptConfig.isAvailable()) {
            final Result<XPathExecutable, String> result = this.acceptConfig.build(repository);
            if (result.isValid()) {
                scenario.setAcceptExecutable(result.getObject());
                scenario.getConfiguration().setAcceptMatch(this.acceptConfig.getXPath());
                this.namespaces.putAll(this.acceptConfig.getNamespaces());
            } else {
                errors.addAll(result.getErrors());
            }
        } else {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug("No accept configuration available");
        }
    }

    private void buildReport(final ContentRepository repository, final List<String> errors, final Scenario scenario) {
        if (this.reportBuilder == null) {
            errors.add("Must supply report configuration");
        } else {
            final Result<Pair<CreateReportType, Transformation>, String> result = this.reportBuilder.build(repository);
            if (result.isValid()) {
                scenario.getReportTransformations().add(result.getObject().getRight());
                scenario.getConfiguration().getCreateReport().add(result.getObject().getLeft());
            } else {
                errors.addAll(result.getErrors());
            }
        }
    }

    private void buildSchematron(final ContentRepository repository, final List<String> errors, final Scenario scenario) {
        this.schematronBuilders.forEach(e -> {
            final Result<Pair<ValidateWithSchematron, Transformation>, String> result = e.build(repository);
            if (result.isValid()) {
                scenario.getConfiguration().getValidateWithSchematron().add(result.getObject().getLeft());
                scenario.getSchematronValidations().add(result.getObject().getRight());
            } else {
                errors.addAll(result.getErrors());
            }
        });
    }

    private void buildSchema(final ContentRepository repository, final List<String> errors, final Scenario scenario) {
        if (this.schemaBuilder == null) {
            errors.add("Must supply schema for validation");
        } else {
            final Result<Pair<ValidateWithXmlSchema, Schema>, String> result = this.schemaBuilder.build(repository);
            if (result.isValid()) {
                scenario.setSchema(result.getObject().getRight());
                scenario.getConfiguration().setValidateWithXmlSchema(result.getObject().getLeft());
            } else {
                errors.addAll(result.getErrors());
            }
        }
    }

    private ScenarioType createType() {
        final ScenarioType type = new ScenarioType();
        type.setName(isNotEmpty(this.name) ? this.name : generateName());
        final DescriptionType desc = new DescriptionType();
        desc.getPOrOlOrUl()
                .add(new ObjectFactory().createDescriptionTypeP(StringUtils.defaultIfBlank(this.description, DEFAULT_DESCRIPTION)));
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

    ReportBuilder getReportBuilder() {
        return this.reportBuilder;
    }

    String getDescription() {
        return this.description;
    }
}
