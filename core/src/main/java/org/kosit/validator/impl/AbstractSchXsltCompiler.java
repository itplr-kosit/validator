package org.kosit.validator.impl;

import java.net.URI;
import java.util.Map;
import java.util.function.Function;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.kosit.validator.api.SchematronCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import name.dmaus.schxslt.Compiler;
import name.dmaus.schxslt.SchematronException;

public abstract class AbstractSchXsltCompiler implements SchematronCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSchXsltCompiler.class);

    protected final Compiler compiler;

    protected AbstractSchXsltCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public Source compileToXslt(URI schematronUri, Function<URI, Source> rawResolver) {
        LOGGER.info("Trying to compile Schematron file {} using schxslt-java", schematronUri);
        try {
            Source schSource = rawResolver.apply(schematronUri);
            if (schSource == null) {
                throw new IllegalStateException("No Schematron found for " + schematronUri);
            }
            if (schSource.getSystemId() == null && schSource instanceof StreamSource) {
                schSource.setSystemId(schematronUri.toString());
            }
            // or null, if you don't need any options
            Document stylesheetDoc = compiler.compile(schSource, Map.of());
            return new DOMSource(stylesheetDoc, stylesheetDoc.getDocumentURI());
        } catch (SchematronException e) {
            throw new IllegalStateException("Error compiling " + schematronUri, e);
        }
    }
}
