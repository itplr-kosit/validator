package org.kosit.validator.config;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.kosit.base.string.StringHelper;
import org.kosit.base.xml.XmlHelper;
import org.kosit.schematron.ContentRepository;
import org.kosit.schematron.SchematronCompilerRegistry;
import org.kosit.validator.config.SchematronBuilder.SchematronBuilderResult;
import org.kosit.validator.impl.model.SingleProcessingResult;
import org.kosit.validator.scenario.v1.CustomErrorLevel;
import org.kosit.validator.scenario.v1.ErrorLevelType;
import org.kosit.validator.scenario.v1.ResourceType;
import org.kosit.validator.scenario.v1.ValidateWithSchematron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.XsltExecutable;

/**
 * Builder for schematron validation configuration: the rule set, the Schematron processor named for it and the severity
 * overrides of its rules.
 * <p>
 * A rule set given by location is compiled once to check it; the pipeline compiles it again from the repository (cache
 * hit). A rule set handed over as {@link XsltExecutable} is passed on to the pipeline as it is.
 * </p>
 *
 * @author Andreas Penski
 */
public class SchematronBuilder implements SingleProcessingResultBuilder<SchematronBuilderResult> {

    /**
     * @param validateResult the declaration
     * @param executable the rule set handed over compiled, {@code null} when it was given by location
     */
    public static record SchematronBuilderResult(ValidateWithSchematron validateResult, @Nullable XsltExecutable executable) {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SchematronBuilder.class);

    private static final String DEFAULT_NAME = "manually configured";

    private XsltExecutable executable;

    private URI source;

    private String name;

    private String compiler;

    private final List<CustomErrorLevel> customLevels = new ArrayList<>();

    private static SingleProcessingResult<SchematronBuilderResult, String> createError(final String msg) {
        return new SingleProcessingResult<>(null, Collections.singletonList(msg));
    }

    @Override
    public SingleProcessingResult<SchematronBuilderResult, String> build(final ContentRepository repository) {
        if (this.executable == null && this.source == null) {
            return createError("Must supply source location and/or executable for schematron '" + this.name + "'");
        }
        final ValidateWithSchematron object = createObject();
        if (this.executable != null) {
            return new SingleProcessingResult<>(new SchematronBuilderResult(object, this.executable));
        }
        try {
            // checked here, compiled by the pipeline from the repository - the cache makes that a lookup
            final String path = this.source.getPath();
            if (path != null && path.endsWith(".sch")) {
                repository.loadSchematronXslt(StringHelper.blankToDefault(this.compiler, SchematronCompilerRegistry.FALLBACK_COMPILER_ID),
                        this.source);
            } else {
                repository.loadXsltScript(this.source);
            }
            return new SingleProcessingResult<>(new SchematronBuilderResult(object, null));
        } catch (final IllegalStateException e) {
            LOGGER.error(e.getMessage(), e);
            return createError("Can not create schematron configuration based  on " + this.source + ". Exception is " + e.getMessage());
        }
    }

    private ValidateWithSchematron createObject() {
        final ValidateWithSchematron o = new ValidateWithSchematron();
        final ResourceType r = new ResourceType();
        final String resourceName = StringHelper.isNotEmpty(this.name) ? this.name : DEFAULT_NAME;
        r.setLocation(this.source != null ? this.source.toASCIIString()
                : SchemaBuilder.PRECOMPILED_SCHEME + XmlHelper.createValidNCName(resourceName));
        r.setName(resourceName);
        o.setResource(r);
        o.setCompiler(this.compiler);
        o.getCustomLevel().addAll(this.customLevels);
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
     * Names the Schematron processor of this rule set ({@code validateWithSchematron/@compiler}): for a {@code .sch}
     * the one to compile it with, for a precompiled {@code .xsl} the one that produced it.
     *
     * @param compilerId the processor id, e.g. {@code schxslt}, {@code schxslt2}, {@code iso-schematron}
     * @return this
     */
    public SchematronBuilder compiler(final String compilerId) {
        this.compiler = compilerId;
        return this;
    }

    /**
     * Overrides the severity of rules of this rule set ({@code validateWithSchematron/customLevel}).
     *
     * @param level the level the rules get
     * @param codes the rule codes
     * @return this
     */
    public SchematronBuilder customLevel(final ErrorLevelType level, final String... codes) {
        final CustomErrorLevel custom = new CustomErrorLevel();
        custom.setLevel(level);
        custom.getValue().addAll(List.of(codes));
        this.customLevels.add(custom);
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
