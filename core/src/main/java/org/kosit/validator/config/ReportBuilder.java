package org.kosit.validator.config;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Scenario.Transformation;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.model.scenarios.CreateReportType;
import org.kosit.validator.model.scenarios.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XsltExecutable;

/**
 * Builder style configuration for the report transformation.
 * 
 * @author Andreas Penski
 */
public class ReportBuilder implements Builder<Pair<CreateReportType, Transformation>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportBuilder.class);

    private static final String DEFAULT_NAME = "manually created report";

    private XsltExecutable executable;

    private URI source;

    private String name;

    private String id;

    private static Result<Pair<CreateReportType, Transformation>, String> createError(final String msg) {
        return new Result<>(null, Collections.singletonList(msg));
    }

    @Override
    public Result<Pair<CreateReportType, Transformation>, String> build(final ContentRepository repository) {
        if (this.executable == null && this.source == null) {
            return createError("Must supply source location and/or executable for report '" + this.name + "'");
        }
        final CreateReportType object = createObject();
        Result<Pair<CreateReportType, Transformation>, String> result;
        try {
            if (this.executable == null) {
                this.executable = repository.createTransformation(object.getResource()).getExecutable();
            }
            result = new Result<>(new ImmutablePair<>(object, new Transformation(this.executable, object.getResource())));
        } catch (final IllegalStateException e) {
            LOGGER.error(e.getMessage(), e);
            result = createError(" Can not create report configuration based on " + this.source + ". Exception is " + e.getMessage());
        }
        return result;
    }

    private CreateReportType createObject() {
        final CreateReportType o = new CreateReportType();
        final ResourceType r = new ResourceType();
        r.setLocation(this.source != null ? this.source.toASCIIString() : DEFAULT_NAME);
        r.setName(isNotEmpty(this.name) ? this.name : DEFAULT_NAME);
        o.setId(isNotEmpty(this.id) ? this.id : DEFAULT_NAME);
        o.setResource(r);
        return o;
    }

    /**
     * Specifies a source for this report. This is either used to compile the report transformation or as documentation
     * for a precompiled transformation.
     *
     * @param source the source
     * @return this
     */
    public ReportBuilder source(final String source) {
        return source(URI.create(source));
    }

    /**
     * Specifies a source for this report. This is either used to compile the report transformation or as documentation
     * for a precompiled transformation.
     *
     * @param source the source
     * @return this
     */
    public ReportBuilder source(final URI source) {
        this.source = source;
        return this;
    }

    /**
     * Specifies an explicit executable for this report. for a precompiled transformation.
     *
     * @param executable the compiled executable
     * @return this
     */
    public ReportBuilder source(final XsltExecutable executable) {
        this.executable = executable;
        return this;
    }

    /**
     * Specifies a source for this report. This is either used to compile the report transformation or as documentation
     * for a precompiled transformation.
     *
     * @param source the source
     * @return this
     */
    public ReportBuilder source(final Path source) {
        return source(source.toUri());
    }

    /**
     * Sets the name of the report source to a specific value.
     *
     * @param name the name
     * @return this
     */
    public ReportBuilder name(final String name) {
        this.name = name;
        return this;
    }

    public ReportBuilder id(final String id) {
        this.id = id;
        return this;
    }
}
