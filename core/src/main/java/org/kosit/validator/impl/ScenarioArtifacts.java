package org.kosit.validator.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.kosit.base.string.StringHelper;
import org.kosit.jaxb.adapter.StringTrimAdapter;
import org.kosit.schematron.ContentRepository;
import org.kosit.validator.impl.Scenario.Transformation;
import org.kosit.schematron.SchXsltCompiler;
import org.kosit.validator.scenario.v1.NamespaceType;
import org.kosit.validator.scenario.v1.ResourceType;
import org.kosit.validator.scenario.v1.ScenarioType;
import org.kosit.validator.scenario.v1.ValidateWithSchematron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;

/**
 * Creates the validation artifacts declared by a scenario configuration from a {@link ContentRepository}.
 * <p>
 * The repository itself is scenario agnostic — it resolves and compiles artifacts addressed by URI, which is all the
 * canonical pipeline needs. Everything that reads the scenario configuration ({@code scenarios-v1.xsd}) to find out
 * <i>which</i> artifacts to create lives here, so that the repository can stay in the {@code validator-schematron}
 * module.
 * </p>
 *
 * @author Andreas Penski
 */
public final class ScenarioArtifacts {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioArtifacts.class);

    private ScenarioArtifacts() {
        // static utility
    }

    private static Map<String, String> namespaces(final ScenarioType s) {
        return s.getNamespace().stream().collect(Collectors.toMap(NamespaceType::getPrefix, ns -> StringTrimAdapter.trim(ns.getValue())));
    }

    /**
     * Returns the schema for this scenario.
     *
     * @param repository the content repository resolving and compiling the artifacts
     * @param s the scenario configuration
     * @return the matching schema
     */
    public static Schema createSchema(final ContentRepository repository, final ScenarioType s) {
        Schema schema = null;
        if (s.getValidateWithXmlSchema() != null) {
            final List<String> schemaResources = s.getValidateWithXmlSchema().getResource().stream().map(ResourceType::getLocation)
                    .toList();
            schema = repository.createSchema(schemaResources);
        }
        return schema;
    }

    /**
     * Returns a transformation.
     *
     * @param repository the content repository resolving and compiling the artifacts
     * @param t the scenario configuration
     * @return initialized transformation
     */
    public static List<Transformation> createReportTransformations(final ContentRepository repository, final ScenarioType t) {
        LOGGER.info("Create Report Transformations:");
        return t.getCreateReport().stream().map(createReportType -> createTransformation(repository, createReportType.getResource()))
                .toList();
    }

    public static Transformation createTransformation(final ContentRepository repository, final ResourceType resource) {
        final XsltExecutable executable = repository.loadXsltScript(URI.create(resource.getLocation()));
        return new Transformation(executable, resource);
    }

    public static XPathExecutable createMatchExecutable(final ContentRepository repository, final ScenarioType s) {
        return repository.createXPath(s.getMatch(), namespaces(s));
    }

    public static XPathExecutable createAccepptExecutable(final ContentRepository repository, final ScenarioType s) {
        return repository.createXPath(s.getAcceptMatch(), namespaces(s));
    }

    public static List<Transformation> createSchematronTransformations(final ContentRepository repository, final ScenarioType s) {
        return s.getValidateWithSchematron().isEmpty() ? Collections.emptyList()
                : s.getValidateWithSchematron().stream().map(v -> createSchematronTransformation(repository, v)).toList();
    }

    public static Transformation createSchematronTransformation(final ContentRepository repository,
            final ValidateWithSchematron validateWithSchematron) {
        LOGGER.info("Create Schematron Transformation:");
        final ResourceType resource = validateWithSchematron.getResource();
        final URI uri = URI.create(resource.getLocation());
        final String path = uri.getPath();
        final String compilerId = StringHelper.blankToDefault(validateWithSchematron.getCompiler(), SchXsltCompiler.COMPILER_ID);
        if (path != null && path.endsWith(".sch")) {
            final XsltExecutable executable = repository.loadSchematronXslt(uri, compilerId);
            return new Transformation(executable, resource);
        }
        return createTransformation(repository, validateWithSchematron.getResource());
    }

    public static Transformation createIdentityTransformation(final ContentRepository repository) {
        final URL url = ScenarioArtifacts.class.getClassLoader().getResource("transform/identity.xsl");
        try ( InputStream input = url.openStream() ) {
            final XsltCompiler xsltCompiler = repository.getProcessor().newXsltCompiler();
            final XsltExecutable executable = xsltCompiler.compile(new StreamSource(input));
            final ResourceType resource = new ResourceType();
            resource.setName("identity");
            resource.setLocation(url.toString());
            return new Transformation(executable, resource);
        } catch (final IOException | SaxonApiException e) {
            throw new IllegalStateException("Error creating identity transformation", e);
        }
    }
}
