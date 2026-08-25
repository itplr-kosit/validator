package org.kosit.validator.impl.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Collectors;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.VInputFactory;
import org.kosit.validator.impl.TestHelper;
import org.kosit.validator.impl.TestHelper.Simple;
import org.kosit.validator.impl.TestObjectFactory;
import org.kosit.validator.impl.model.Result;
import org.kosit.validator.model.XMLSyntaxError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Tests various Saxon security settings.
 *
 * @author Andreas Penski
 */
public class SaxonSecurityTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SaxonSecurityTest.class);

    @Test
    public void testEvilStylesheets() throws IOException {
        final Processor p = TestObjectFactory.createProcessor();
        for (int i = 1; i <= 5; i++) {
            try {
                final URL resource = SaxonSecurityTest.class.getResource("/evil/evil" + i + ".xsl");
                final XsltCompiler compiler = p.newXsltCompiler();
                final RelativeUriResolver resolver = new RelativeUriResolver(Simple.REPOSITORY_URI);
                compiler.setURIResolver(resolver);
                final XsltExecutable executable = compiler.compile(new StreamSource(resource.openStream()));
                final XsltTransformer transformer = executable.load();
                final Source document = VInputFactory.read("<root/>".getBytes(), "dummy").getSource();
                // transformer.getUnderlyingController().setUnparsedTextURIResolver(resolver);
                transformer.setURIResolver(resolver);
                transformer.setSource(document);
                final XdmDestination result = new XdmDestination();
                transformer.setDestination(result);
                transformer.transform();

                // if this point is reached, the 'evil' element should at least not be filled with 'evil' content!
                if (StringUtils.isNotBlank(result.getXdmNode().getStringValue())) {
                    fail("Saxon configuration should prevent expansion within " + resource);
                }
            } catch (final SaxonApiException | RuntimeException e) {
                LOGGER.info("Expected exception detected {}", e.getMessage(), e);
            }
        }
    }

    @Test
    public void testXxe() throws URISyntaxException {
        final URL resource = SaxonSecurityTest.class.getResource("/evil/xxe.xml");
        final Result<XdmNode, XMLSyntaxError> result = TestHelper.parseDocument(TestHelper.read(resource.toURI()));
        assertThat(result.isValid()).isFalse();
        assertThat(result.getObject()).isNull();
        assertThat(result.getErrors().stream().map(XMLSyntaxError::getMessage).collect(Collectors.joining()))
                .contains("http://apache.org/xml/features/disallow-doctype-dec");
    }
}
