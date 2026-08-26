package org.kosit.validator.config;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;

import org.kosit.base.string.StringHelper;
import org.kosit.validator.config.SchematronBuilder.SchematronBuilderResult;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Scenario.Transformation;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.scenario.v1.ResourceType;
import org.kosit.validator.scenario.v1.ValidateWithSchematron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XsltExecutable;

/**
 * Builder for schematron validation configuration.
 * 
 * @author Andreas Penski
 */
public class SchematronBuilder implements SingleProcessingResultBuilder<SchematronBuilderResult> {

    public static record SchematronBuilderResult(ValidateWithSchematron validateResult, Transformation transformation) {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SchematronBuilder.class);

    private static final String DEFAULT_NAME = "manually configured";

    private XsltExecutable executable;

    private URI source;

    private String name;

    private static SingleProcessingResult<SchematronBuilderResult, String> createError(final String msg) {
        return new SingleProcessingResult<>(null, Collections.singletonList(msg));
    }

    @Override
    public SingleProcessingResult<SchematronBuilderResult, String> build(final ContentRepository repository) {
        if (this.executable == null && this.source == null) {
            return createError("Must supply source location and/or executable for schematron '" + this.name + "'");
        }
        final ValidateWithSchematron object = createObject();
        SingleProcessingResult<SchematronBuilderResult, String> result;
        try {
            if (this.executable == null) {
                this.executable = repository.createSchematronTransformation(object).getExecutable();
            }
            result = new SingleProcessingResult<>(
                    new SchematronBuilderResult(object, new Transformation(this.executable, object.getResource())));
        } catch (final IllegalStateException e) {
            LOGGER.error(e.getMessage(), e);
            result = createError("Can not create schematron configuration based  on " + this.source + ". Exception is " + e.getMessage());
        }
        return result;
    }

    private ValidateWithSchematron createObject() {
        final ValidateWithSchematron o = new ValidateWithSchematron();
        final ResourceType r = new ResourceType();
        r.setLocation(this.source.toASCIIString());
        r.setName(StringHelper.isNotEmpty(this.name) ? this.name : DEFAULT_NAME);
        o.setResource(r);
        return o;
    }

    /**
     * Specifies a source for this schematron validation. This is either used to compile the schematron transformation
     * or as documentation for a precompiled transformation.
     * 
     * @param source the source
     * @return this
     */
    public SchematronBuilder source(final String source) {
        return source(URI.create(source));
    }

    /**
     * Specifies a source for this schematron validation. This is either used to compile the schematron transformation
     * or as documentation for a precompiled transformation.
     * 
     * @param source the source
     * @return this
     */
    public SchematronBuilder source(final URI source) {
        this.source = source;
        return this;
    }

    /**
     * Specifies a source for this schematron validation. This is either used to compile the schematron transformation
     * or as documentation for a precompiled transformation.
     * 
     * @param source the source
     * @return this
     */
    public SchematronBuilder source(final Path source) {
        return source(source.toUri());
    }

    /**
     * Sets the name of the schematron source to a specific value.
     * 
     * @param name the name
     * @return this
     */
    public SchematronBuilder name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets a specific pre-compiled executable as schematron source.
     * 
     * @param executable the executable
     * @return this
     */
    public SchematronBuilder executable(final XsltExecutable executable) {
        this.executable = executable;
        return this;
    }

    XsltExecutable getExecutable() {
        return this.executable;
    }

    URI getSource() {
        return this.source;
    }

    String getName() {
        return this.name;
    }

    SchematronBuilder() {
    }
}
