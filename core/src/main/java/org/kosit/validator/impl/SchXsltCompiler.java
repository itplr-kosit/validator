package org.kosit.validator.impl;

import lombok.extern.slf4j.Slf4j;
import name.dmaus.schxslt.Compiler;
import name.dmaus.schxslt.SchematronException;
import name.dmaus.schxslt.adapter.SchXslt;
import net.sf.saxon.s9api.*;
import org.kosit.validator.api.SchematronCompiler;
import org.w3c.dom.Document;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.net.URI;
import java.util.Map;
import java.util.function.Function;

/**
 * {@link SchematronCompiler} implementation backed by the original SchXslt XSLT-based Schematron processor (library
 * {@code name.dmaus.schxslt:schxslt}, version 1.10.1).
 *
 * <p>
 * This compiler delegates the actual transformation from Schematron schema ({@code .sch}) to XSLT stylesheet to the
 * {@link name.dmaus.schxslt.Compiler} class from the {@code schxslt-java} module, using the {@link SchXslt} adapter.
 * The resulting XSLT is returned as a {@link DOMSource} so that callers can compile it into an {@link XsltExecutable}
 * with Saxon.
 * </p>
 *
 * <p>
 * The class is effectively thread-safe: it holds a single, immutable {@link Compiler} instance, which in turn is
 * designed to be used safely from multiple threads.
 * </p>
 */
@Slf4j
public class SchXsltCompiler implements SchematronCompiler {

    public static final String COMPILER_ID = "schxslt";

    private final Compiler compiler;

    public SchXsltCompiler() {
        this.compiler = new Compiler(new SchXslt());
    }

    @Override
    public String getId() {
        return COMPILER_ID;
    }

    @Override
    public Source compileToXslt(URI schematronUri, Function<URI, Source> rawResolver) {
        log.info("Trying to compile Schematron file {} using schxslt-java", schematronUri);
        try {
            Source schSource = rawResolver.apply(schematronUri);
            if (schSource == null) {
                throw new IllegalStateException("No Schematron found for " + schematronUri);
            }

            if (schSource.getSystemId() == null && schSource instanceof StreamSource) {
                schSource.setSystemId(schematronUri.toString());
            }

            Document stylesheetDoc = compiler.compile(schSource, Map.of()); // oder null, falls du keine Optionen
                                                                            // brauchst

            return new DOMSource(stylesheetDoc, stylesheetDoc.getDocumentURI());

        } catch (SchematronException e) {
            throw new IllegalStateException("Error compiling " + schematronUri, e);
        }
    }
}