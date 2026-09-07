package org.kosit.schematron;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.jspecify.annotations.NonNull;
import org.kosit.schematron.compiler.SchematronCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import name.dmaus.schxslt.Compiler;
import name.dmaus.schxslt.SchematronException;

public abstract class AbstractSchXsltCompiler implements SchematronCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSchXsltCompiler.class);

    protected final Compiler compiler;

    private final String compilerName;

    protected AbstractSchXsltCompiler(@NonNull final Compiler compiler, @NonNull final String compilerName) {
        this.compiler = compiler;
        this.compilerName = compilerName;
    }

    @Override
    public DOMSource compileToXslt(final URI schematronUri, final Function<URI, Source> rawResolver) {
        LOGGER.info("Trying to compile Schematron file '" + schematronUri + "' using " + compilerName);
        try {
            final Source schSource = rawResolver.apply(schematronUri);
            if (schSource == null) {
                throw new IllegalStateException("No Schematron found for " + schematronUri);
            }

            if (schSource.getSystemId() == null && schSource instanceof StreamSource) {
                schSource.setSystemId(schematronUri.toString());
            }

            // or null, if you don't need any options
            final Document stylesheetDoc = compiler.compile(schSource, Map.of());
            return new DOMSource(stylesheetDoc, stylesheetDoc.getDocumentURI());
        } catch (final SchematronException e) {
            throw new IllegalStateException("Error compiling '" + schematronUri + "' unsing " + compilerName, e);
        }
    }
}
