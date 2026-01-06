package org.kosit.validator.impl;

import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.s9api.*;
import org.kosit.validator.api.SchematronCompiler;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.function.Function;

@Slf4j
public class SchXsltCompiler implements SchematronCompiler {

    public static final String COMPILER_ID = "schxslt";

    private final Processor processor;

    public SchXsltCompiler(Processor processor) {
        this.processor = processor;
    }

    @Override
    public String getId() {
        return COMPILER_ID;
    }

    @Override
    public Source compileToXslt(URI schematronUri, Function<URI, Source> rawResolver) {
        log.info("Trying to compile Schematron file {}", schematronUri);
        try {
            final XsltCompiler xsltCompiler = processor.newXsltCompiler();

            final Source schSource = rawResolver.apply(schematronUri);
            final DocumentBuilder db = processor.newDocumentBuilder();
            final XdmNode schDoc = db.build(schSource);

            final XdmNode expanded = transform(xsltCompiler, "xslt/2.0/expand.xsl", schDoc);

            final XdmNode xsltDoc = transform(xsltCompiler, "xslt/2.0/compile-for-svrl.xsl", expanded);

            return xsltDoc.asSource();
        } catch (SaxonApiException e) {
            throw new IllegalStateException("Error compiling " + schematronUri, e);
        }
    }

    private XdmNode transform(final XsltCompiler compiler, final String xsltPath, final XdmNode input) throws SaxonApiException {
        final Source xslt = classpathXslt(xsltPath);
        final XsltExecutable exec = compiler.compile(xslt);
        final XsltTransformer t = exec.load();
        t.setInitialContextNode(input);
        final XdmDestination dest = new XdmDestination();
        t.setDestination(dest);
        t.transform();
        return dest.getXdmNode();
    }

    private Source classpathXslt(String path) {
        final URL url = SchXsltCompiler.class.getResource("/" + path);
        if (url == null) {
            throw new IllegalStateException("Cannot find SchXslt stylesheet: " + path);
        }
        try {
            final InputStream in = url.openStream();
            final StreamSource src = new StreamSource(in);
            src.setSystemId(url.toExternalForm());
            return src;
        } catch (IOException e) {
            throw new IllegalStateException("Cannot open " + url, e);
        }
    }
}