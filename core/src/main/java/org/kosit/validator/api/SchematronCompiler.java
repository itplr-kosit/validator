package org.kosit.validator.api;

import java.net.URI;
import java.util.function.Function;

import javax.xml.transform.Source;

/**
 * Strategy interface for compiling Schematron schemas into executable XSLT stylesheets.
 * <p>
 * Implementations encapsulate a specific Schematron processor (for example SchXslt) and transform a {@code .sch}
 * resource into an XSLT {@link Source} that can be compiled and executed by Saxon.
 * </p>
 *
 * <p>
 * Instances of this interface are typically discovered and managed by
 * {@link org.kosit.validator.impl.SchematronCompilerRegistry} and used by
 * {@link org.kosit.validator.impl.ContentRepository} to lazily compile and cache Schematron transformations.
 * </p>
 */
public interface SchematronCompiler {

    /**
     * Returns the unique identifier of this compiler implementation.
     * <p>
     * The returned ID is used as a key in the {@code SchematronCompilerRegistry} and in configuration to select a
     * particular compiler (for example {@code "schxslt"} for the {@code SchXsltCompiler} implementation).
     * </p>
     *
     * @return a non-empty, stable identifier for this compiler implementation
     */
    String getId();

    /**
     * Compiles the given Schematron schema into an XSLT stylesheet.
     * <p>
     * The resulting {@link Source} represents an XSLT transformation that produces an SVRL report when applied to an
     * XML instance document with Saxon.
     * </p>
     *
     * <p>
     * The {@code rawResolver} callback is used to resolve the physical location of the Schematron resource within the
     * validator's content repository. Implementations must not cache the resolver itself, but may cache the compiled
     * XSLT {@link Source} for the given {@link URI}.
     * </p>
     *
     * @param schematronUri logical or physical URI of the Schematron schema to compile; usually relative to the
     *            configured content repository
     * @param rawResolver function that resolves the given {@link URI} to a {@link Source} representing the raw
     *            Schematron schema; must return a non-{@code null} {@link Source} or throw an appropriate exception if
     *            resolution fails
     *
     * @return a {@link Source} containing the compiled XSLT stylesheet
     *
     * @throws IllegalStateException if the Schematron resource cannot be resolved or if compilation fails for any
     *             reason
     */
    Source compileToXslt(URI schematronUri, Function<URI, Source> rawResolver);
}