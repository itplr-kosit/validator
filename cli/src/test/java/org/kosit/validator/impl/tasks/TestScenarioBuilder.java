package org.kosit.validator.impl.tasks;

import org.kosit.validator.api.ResolvingConfigurationStrategy;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Helper;
import org.kosit.validator.impl.ResolvingMode;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.impl.xml.ProcessorProvider;
import org.kosit.validator.impl.xml.StrictRelativeResolvingStrategy;
import org.kosit.validator.model.scenarios.CreateReportType;
import org.kosit.validator.model.scenarios.ResourceType;
import org.kosit.validator.model.scenarios.ScenarioType;
import org.kosit.validator.model.scenarios.ValidateWithXmlSchema;

import javax.xml.validation.Schema;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

public class TestScenarioBuilder {

    public static Scenario createDefault() {
        return createScenario(Helper.Simple.SCHEMA, Helper.Simple.REPORT_XSL);
    }

    private static Schema createSchema(final URL toURL) {
        final ContentRepository contentRepository = new ContentRepository(Helper.getTestProcessor(),
                ResolvingMode.STRICT_RELATIVE.getStrategy(), null);
        return contentRepository.createSchema(toURL);
    }

    public static Scenario createScenario(final URI schemafile, final URI reportTransformation) {

        try {
            final ContentRepository repo = new ContentRepository(ProcessorProvider.getProcessor(), new StrictRelativeResolvingStrategy(),
                    Helper.Simple.REPOSITORY_URI);
            final ScenarioType t = new ScenarioType();
            final Scenario scenario = new Scenario(t);
            scenario.setUnparsedTextURIResolver(repo.getUnparsedTextURIResolver());
            scenario.setUriResolver(repo.getResolver());
            // schema validation
            t.setValidateWithXmlSchema(createSchemaValidation(schemafile));
            if (reportTransformation != null) {

                t.getCreateReport().add(createReportTransformation(reportTransformation));
                // final XsltCompiler xsltCompiler = ProcessorProvider.getProcessor().newXsltCompiler();
                // xsltCompiler.setURIResolver(new RelativeUriResolver(Helper.Simple.REPOSITORY_URI));
                // scenario.setUriResolver(new RelativeUriResolver(Helper.Simple.REPOSITORY_URI));
                // scenario.setUnparsedTextURIResolver(new RelativeUriResolver(Helper.Simple.REPOSITORY_URI));
                // final XsltExecutable executable = xsltCompiler.compile(new
                // StreamSource(reportTransformation.toURL().openStream()));
                // final Scenario.Transformation ts = new Scenario.Transformation(executable,
                // t.getCreateReport().get(0).getResource());
                scenario.setReportTransformations(repo.createReportTransformations(t));

            }

            scenario.setSchema(createSchema(schemafile.toURL()));
            final ResolvingConfigurationStrategy strategy = ResolvingMode.STRICT_RELATIVE.getStrategy();
            scenario.setFactory(strategy);
            return scenario;
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static CreateReportType createReportTransformation(final URI reportTransformation) {
        final CreateReportType report = new CreateReportType();
        final ResourceType reporResource = new ResourceType();
        reporResource.setLocation(reportTransformation.getRawPath());
        reporResource.setName("default");
        report.setResource(reporResource);
        report.setId("default");
        return report;
    }

    private static ValidateWithXmlSchema createSchemaValidation(final URI schemafile) {
        final ValidateWithXmlSchema v = new ValidateWithXmlSchema();
        final ResourceType r = new ResourceType();
        r.setLocation(schemafile.getRawPath());
        r.setName("default");
        v.getResource().add(r);
        return v;
    }

}
