/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.stream.Collectors;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.Helper.Simple;

import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.impl.model.Result;
import de.kosit.validationtool.impl.tasks.DocumentParseAction;
import de.kosit.validationtool.model.reportInput.XMLSyntaxError;


import net.sf.saxon.s9api.XdmNode;


/**
 * Testet verschiedene Saxon Security Einstellungen.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class SaxonSecurityTest {

    @Test
    public void testEvilStylesheets() throws IOException {
        final Processor p = ObjectFactory.createProcessor();
        for (int i = 1; i <= 5; i++) {
            try {
                final URL resource = SaxonSecurityTest.class.getResource(String.format("/evil/evil%s.xsl", i));
                final XsltCompiler compiler = p.newXsltCompiler();
                final RelativeUriResolver resolver = new RelativeUriResolver(Simple.REPOSITORY);
                compiler.setURIResolver(resolver);
                final XsltExecutable exetuable = compiler.compile(new StreamSource(resource.openStream()));
                final XsltTransformer transformer = exetuable.load();
                final Document document = ObjectFactory.createDocumentBuilder(false).newDocument();
                document.createElement("root");
                final Document result = ObjectFactory.createDocumentBuilder(false).newDocument();
                transformer.getUnderlyingController().setUnparsedTextURIResolver(resolver);
                transformer.setURIResolver(resolver);
                transformer.setSource(new DOMSource(document));
                transformer.setDestination(new DOMDestination(result));
                transformer.transform();

                // wenn der Punkt erreicht wird, sollte wenigstens, das Element evil nicht mit 'bösen' Inhalten gefüllt sein!
                if (StringUtils.isNotBlank(result.getDocumentElement().getTextContent())) {
                    fail(String.format("Saxon configuration should prevent expansion within %s", resource));
                }

            } catch (final SaxonApiException | RuntimeException e) {
                log.info("Expected exception detected {}", e.getMessage(), e);
            }
        }
    }

    @Test
    public void testXxe() {
        final URL resource = SaxonSecurityTest.class.getResource("/evil/xxe.xml");
        final Result<XdmNode, XMLSyntaxError> result = DocumentParseAction.parseDocument(InputFactory.read(resource));
        assertThat(result.isValid()).isFalse();
        assertThat(result.getObject()).isNull();
        assertThat(result.getErrors().stream().map(XMLSyntaxError::getMessage).collect(Collectors.joining()))
                .contains("http://apache.org/xml/features/disallow-doctype-dec");
    }
}
