package org.kosit.validator.config;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collections;

import org.kosit.base.string.StringHelper;
import org.kosit.validator.config.ReportBuilder.ReportBuilderResult;
import org.kosit.validator.impl.ContentRepository;
import org.kosit.validator.impl.Scenario.Transformation;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.scenario.v1.CreateReportType;
import org.kosit.validator.scenario.v1.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XsltExecutable;

/**
 * Builder style configuration for the report transformation.
 * 
 * @author Andreas Penski
 */
public class ReportBuilder implements SingleProcessingResultBuilder<ReportBuilderResult> {

    public static record ReportBuilderResult(CreateReportType createReport, Transformation transformation) {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportBuilder.class);

    private static final String DEFAULT_NAME = "manually created report";

    private XsltExecutable executable;

    private URI source;

    private String name;

    private String id;

    private static SingleProcessingResult<ReportBuilderResult, String> createError(final String msg) {
        return new SingleProcessingResult<>(null, Collections.singletonList(msg));
    }

    @Override
    public SingleProcessingResult<ReportBuilderResult, String> build(final ContentRepository repository) {
        if (this.executable == null && this.source == null) {
            return createError("Must supply source location and/or executable for report '" + this.name + "'");
        }
        final CreateReportType object = createObject();
        SingleProcessingResult<ReportBuilderResult, String> result;
        try {
            if (this.executable == null) {
                this.executable = repository.createTransformation(object.getResource()).getExecutable();
            }
            result = new SingleProcessingResult<>(
                    new ReportBuilderResult(object, new Transformation(this.executable, object.getResource())));
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
        r.setName(StringHelper.isNotEmpty(this.name) ? this.name : DEFAULT_NAME);
        o.setId(StringHelper.isNotEmpty(this.id) ? this.id : DEFAULT_NAME);
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
