package org.kosit.validator.impl;

import java.io.InputStream;
import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.kosit.validator.api.SchematronCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

public final class IsoSchematronCompiler implements SchematronCompiler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IsoSchematronCompiler.class);

    public static final String COMPILER_ID = "iso-schematron";

    private static final String CP_BASE = "/schematron/iso-schematron-xslt2/";

    private final Processor processor;

    private final XsltExecutable dsdlInclude;

    private final XsltExecutable abstractExpand;

    private final XsltExecutable svrlForXslt2;

    public IsoSchematronCompiler() {
        this(new Processor(false));
    }

    public IsoSchematronCompiler(final Processor processor) {
        this.processor = Objects.requireNonNull(processor, "processor");
        try {
            final XsltCompiler c = this.processor.newXsltCompiler();
            this.dsdlInclude = c.compile(classpathXsl(CP_BASE + "iso_dsdl_include.xsl"));
            this.abstractExpand = c.compile(classpathXsl(CP_BASE + "iso_abstract_expand.xsl"));
            this.svrlForXslt2 = c.compile(classpathXsl(CP_BASE + "iso_svrl_for_xslt2.xsl"));
        } catch (SaxonApiException e) {
            throw new IllegalStateException("Failed to compile ISO Schematron skeleton meta-stylesheets from classpath", e);
        }
    }

    @Override
    public String getId() {
        return COMPILER_ID;
    }

    @Override
    public Source compileToXslt(final URI schematronUri, final Function<URI, Source> rawResolver) {
        Objects.requireNonNull(schematronUri, "schematronUri");
        Objects.requireNonNull(rawResolver, "rawResolver");
        LOGGER.info("Trying to compile Schematron file {} using ISO Schematron skeleton (classpath-only)", schematronUri);
        try {
            Source schSource = rawResolver.apply(schematronUri);
            if (schSource == null) {
                throw new IllegalStateException("No Schematron found for " + schematronUri);
            }
            if (schSource.getSystemId() == null) {
                schSource.setSystemId(schematronUri.toString());
            }
            final XdmNode schDoc = processor.newDocumentBuilder().build(schSource);
            final XdmNode stage1 = transformToNode(dsdlInclude, schDoc);
            final XdmNode stage2 = transformToNode(abstractExpand, stage1);
            final XdmNode xsltStylesheet = transformToNode(svrlForXslt2, stage2);
            return xsltStylesheet.asSource();
        } catch (SaxonApiException e) {
            throw new IllegalStateException("Error compiling ISO Schematron " + schematronUri, e);
        }
    }

    private XdmNode transformToNode(final XsltExecutable exec, final XdmNode input) throws SaxonApiException {
        final XsltTransformer t = exec.load();
        t.setInitialContextNode(input);
        final XdmDestination dest = new XdmDestination();
        t.setDestination(dest);
        t.transform();
        return dest.getXdmNode();
    }

    private static StreamSource classpathXsl(final String classpathLocation) {
        final InputStream in = IsoSchematronCompiler.class.getResourceAsStream(classpathLocation);
        if (in == null) {
            throw new IllegalStateException("Missing classpath resource: " + classpathLocation);
        }
        final StreamSource s = new StreamSource(in);
        s.setSystemId("classpath:" + classpathLocation);
        return s;
    }
}
